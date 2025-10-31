package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
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
            CREATE TABLE IF NOT EXISTS Games (
                gameID INT AUTO_INCREMENT PRIMARY KEY,
                whiteUsername VARCHAR(50),
                blackUsername VARCHAR(50),
                gameName VARCHAR(100) NOT NULL,
                gameState LONGTEXT NOT NULL
            )
            """


    };


    @Override
    public void clear() {

    }

    @Override
    public void createUser(UserData user) {

    }

    @Override
    public UserData getUser(String userID) {
        return null;
    }

    @Override
    public boolean userExists(String userID) {
        return false;
    }

    @Override
    public boolean validatePassword(UserData user) {
        return false;
    }

    @Override
    public void addAuth(AuthData authData) {

    }

    @Override
    public boolean validateUserHasAuthdata(AuthData authData) {
        return false;
    }

    @Override
    public AuthData getAuthdataFromAuthtoken(String authToken) {
        return null;
    }

    @Override
    public boolean validateAuthToken(String authToken) {
        return false;
    }

    @Override
    public boolean authTokenExists(String authToken) {
        return false;
    }

    @Override
    public void removeAuth(String authData) {

    }

    @Override
    public void createGame(GameData game) {

    }

    @Override
    public void updateGame(GameData game) {

    }

    @Override
    public int numGames() {
        return 0;
    }

    @Override
    public boolean gameIDExists(int gameID) {
        return false;
    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return List.of();
    }

    private void configureDatabase() throws ResponseException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}