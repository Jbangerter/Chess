package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;

public class SqlDataAccess implements DataAccess {

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(255) NOT NULL UNIQUE,
                email VARCHAR(255) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL
            )
            """,

            """
            CREATE TABLE IF NOT EXISTS authdata (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                authToken VARCHAR(255) NOT NULL UNIQUE
            )
            """,

            """
            CREATE TABLE IF NOT EXISTS games (
                gameID INT AUTO_INCREMENT PRIMARY KEY,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                gameState LONGTEXT NOT NULL
            )
            """
    };

    public SqlDataAccess() {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        clearUsers();
        clearGames();
        clearAuthdata();
    }

    public void clearUsers() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "TRUNCATE TABLE users";

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to trunccate users", e);
        }

    }

    public void clearGames() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "TRUNCATE TABLE games";

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to trunccate games", e);
        }

    }

    public void clearAuthdata() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "TRUNCATE TABLE authdata";

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to trunccate authdata", e);
        }

    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String passwordHash = hashPassword(user.password());

        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 3. Set the values for the placeholders
            pstmt.setString(1, user.username());
            pstmt.setString(2, user.email());
            pstmt.setString(3, passwordHash);

            int rowsAffected = pstmt.executeUpdate();

            System.out.println("User added successfully. Rows affected: " + rowsAffected);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user:" + user.username(), e);
        }
    }

    @Override
    public UserData getUser(String userID) throws DataAccessException {

        String sql = "SELECT username, email, password_hash FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {

                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String passwordHash = rs.getString("password_hash");

                    return new UserData(username, email, passwordHash);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user:" + userID, e);
        }

        return null;
    }

    @Override
    public boolean userExists(String userID) throws DataAccessException {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE username = ? LIMIT 1)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user:" + userID, e);
        }
        return false;

    }

    @Override
    public boolean validatePassword(UserData user) throws DataAccessException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.username());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("password_hash");

                    if (verifyHash(passwordHash, user.password())) {
                        return true;
                    }

                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to validate password for user:" + user.username(), e);
        }

        return false;
    }

    @Override
    public void addAuth(AuthData authData) throws DataAccessException {
        String sql = """
                INSERT INTO authdata (username, authToken) VALUES (?,?)
                 ON DUPLICATE KEY UPDATE
                            authToken = VALUES(authToken),
                            username = VALUES(username)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            pstmt.setString(1, authData.username());
            pstmt.setString(2, authData.authToken());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to add authentication for:" + authData.username(), e);
        }
    }

    @Override
    public boolean validateUserHasAuthdata(AuthData authData) throws DataAccessException {
        String sql = "SELECT username, authToken FROM authdata WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, authData.authToken());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {

                    String username = rs.getString("username");
                    String newAuthToken = rs.getString("authToken");


                    var newAuthdata = new AuthData(username, newAuthToken);

                    if (newAuthdata.equals(authData)) {
                        return true;
                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to validate user:" + authData.username(), e);
        }

        return false;
    }

    @Override
    public AuthData getAuthdataFromAuthtoken(String authToken) throws DataAccessException {

        String sql = "SELECT username, authToken FROM authdata WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, authToken);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {

                    String username = rs.getString("username");
                    String newAuthToken = rs.getString("authToken");


                    return new AuthData(username, newAuthToken);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get Authdata:", e);
        }

        return null;
    }

    @Override
    public boolean validateAuthToken(String authToken) throws DataAccessException {
        return authTokenExists(authToken);
    }

    @Override
    public boolean authTokenExists(String authToken) throws DataAccessException {
        String sql = "SELECT EXISTS(SELECT 1 FROM authdata WHERE authToken = ? LIMIT 1)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, authToken);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get authdata:", e);
        }
        return false;
    }

    @Override
    public void removeAuth(String authData) throws DataAccessException {
        String sql = "DELETE FROM authdata WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, authData);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete authdata:", e);
        }
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        Gson gson = new Gson();
        String sql = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, gameState) VALUES (?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, game.gameID());
            pstmt.setString(2, game.whiteUsername());
            pstmt.setString(3, game.blackUsername());
            pstmt.setString(4, game.gameName());
            pstmt.setString(5, gson.toJson(game.game()));

            int rowsAffected = pstmt.executeUpdate();

            System.out.println("User added successfully. Rows affected: " + rowsAffected);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game:" + game.gameName(), e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ? ,gameName = ? ,gameState = ? WHERE gameID = ?";

        Gson gson = new Gson();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, game.whiteUsername());
            pstmt.setString(2, game.blackUsername());
            pstmt.setString(3, game.gameName());
            pstmt.setString(4, gson.toJson(game.game()));

            pstmt.setInt(5, game.gameID());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game:" + game.gameName(), e);
        }
    }

    @Override
    public int numGames() throws DataAccessException {
        String sql = "SELECT COUNT(*) AS total_count FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return (int) rs.getLong("total_count");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to count games", e);
        }
        return -1;
    }

    @Override
    public boolean gameIDExists(int gameID) throws DataAccessException {
        String sql = "SELECT EXISTS(SELECT 1 FROM games WHERE gameID = ? LIMIT 1)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game:" + gameID, e);
        }
        return false;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {

        String sql = "SELECT gameID, whiteUsername , blackUsername,gameName,gameState FROM games WHERE gameID = ?";
        Gson gson = new Gson();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int dbGameID = rs.getInt("gameID");
                    String white = rs.getString("whiteUsername");
                    String black = rs.getString("blackUsername");
                    String name = rs.getString("gameName");
                    String gameJson = rs.getString("gameState");

                    ChessGame game = gson.fromJson(gameJson, ChessGame.class);

                    return new GameData(dbGameID, white, black, name, game);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game:" + gameID, e);
        }

        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {

        String sql = "SELECT gameID, whiteUsername , blackUsername,gameName,gameState FROM games";
        Gson gson = new Gson();
        Collection<GameData> gameList = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {

                    int dbGameID = rs.getInt("gameID");
                    String white = rs.getString("whiteUsername");
                    String black = rs.getString("blackUsername");
                    String name = rs.getString("gameName");
                    String gameJson = rs.getString("gameState");

                    ChessGame game = gson.fromJson(gameJson, ChessGame.class);

                    gameList.add(new GameData(dbGameID, white, black, name, game));
                }
            }

            return gameList;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list Games:", e);
        }

    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean verifyHash(String hashedPassword, String clearTextPassword) {
        return BCrypt.checkpw(clearTextPassword, hashedPassword);
    }

    private void configureDatabase() throws RuntimeException {

        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(String.format("Unable to configure database: %s", ex.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}