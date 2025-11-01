package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;


import com.google.gson.Gson;

import java.util.Collection;
import java.sql.*;
import java.util.List;

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
                username VARCHAR(255) NOT NULL UNIQUE,
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
    public void clear() {
        clearUsers();
        clearGames();
        clearAuthdata();
    }

    public void clearUsers() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "TRUNCATE TABLE users";

            stmt.executeUpdate(sql);

            System.out.println("Table users truncated successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to truncate table users");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public void clearGames() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "TRUNCATE TABLE games";

            stmt.executeUpdate(sql);

            System.out.println("Table games truncated successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to truncate table games");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public void clearAuthdata() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "TRUNCATE TABLE authdata";

            stmt.executeUpdate(sql);

            System.out.println("Table authdata truncated successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to truncate table authdata");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void createUser(UserData user) {
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
            System.err.println("Error adding user to database: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserData getUser(String userID) {

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
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public boolean userExists(String userID) {
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
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        return false;

    }

    @Override
    public boolean validatePassword(UserData user) {
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
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    @Override
    public void addAuth(AuthData authData) {
        String sql = "INSERT INTO authdata (username, authToken) VALUES (?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 3. Set the values for the placeholders
            pstmt.setString(1, authData.username());
            pstmt.setString(2, authData.authToken());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validateUserHasAuthdata(AuthData authData) {
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
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    @Override
    public AuthData getAuthdataFromAuthtoken(String authToken) {
//
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
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public boolean validateAuthToken(String authToken) {
        return authTokenExists(authToken);
    }

    @Override
    public boolean authTokenExists(String authToken) {
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
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public void removeAuth(String authData) {
        String sql = "DELETE FROM authdata WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, authData);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createGame(GameData game) {
        Gson gson = new Gson();
        String sql = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, gameState) VALUES (?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 3. Set the values for the placeholders
            pstmt.setInt(1, game.gameID());
            pstmt.setString(2, game.whiteUsername());
            pstmt.setString(3, game.blackUsername());
            pstmt.setString(4, game.gameName());
            pstmt.setString(5, gson.toJson(game.game()));


            int rowsAffected = pstmt.executeUpdate();

            System.out.println("User added successfully. Rows affected: " + rowsAffected);

        } catch (SQLException e) {
            System.err.println("Error adding user to database: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateGame(GameData game) {
//        String sql = "";
//
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, );
//
//            int rowsAffected = pstmt.executeUpdate();
//
//        } catch (SQLException e) {
//            System.err.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        } catch (DataAccessException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public int numGames() {
        String sql = "SELECT COUNT(*) AS total_count FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return (int) rs.getLong("total_count");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    @Override
    public boolean gameIDExists(int gameID) {
//        String sql = "";
//
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, );
//
//            int rowsAffected = pstmt.executeUpdate();
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//
//                    return;
//                }
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        } catch (DataAccessException e) {
//            throw new RuntimeException(e);
//        }
        return false;
    }

    @Override
    public GameData getGame(int gameID) {
//        String sql = "";
//
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, );
//
//            int rowsAffected = pstmt.executeUpdate();
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//
//                    return;
//                }
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        } catch (DataAccessException e) {
//            throw new RuntimeException(e);
//        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
//        String sql = "";
//
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, );
//
//            int rowsAffected = pstmt.executeUpdate();
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//
//                    return;
//                }
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        } catch (DataAccessException e) {
//            throw new RuntimeException(e);
//        }
        return null;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean verifyHash(String hashedPassword, String ClearTextPassword) {
        return BCrypt.checkpw(ClearTextPassword, hashedPassword);
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