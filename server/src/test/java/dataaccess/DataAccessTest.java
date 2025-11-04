package dataaccess;

import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;


public class DataAccessTest {

    private static SqlDataAccess dataAccess;
    private static UserData testUser;
    private static AuthData testAuth;

    @BeforeAll
    public static void startDB() {
        dataAccess = new SqlDataAccess();

        testUser = new UserData("username", "email", "passsword");
        testAuth = new AuthData("username", "authToken");
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
    void createUser() throws DataAccessException {
        dataAccess.createUser(testUser);
        UserData retrievedUser = dataAccess.getUser(testUser.username());
        Assertions.assertNotNull(retrievedUser);
        Assertions.assertEquals(testUser.username(), retrievedUser.username());
        Assertions.assertEquals(testUser.email(), retrievedUser.email());
        Assertions.assertNotNull(retrievedUser.password());

    }

    @Test
    void clear() throws DataAccessException {
        dataAccess.createUser(testUser);
        dataAccess.clear();
        UserData retrievedUser = dataAccess.getUser(testUser.username());
        Assertions.assertNull(retrievedUser);
        Assertions.assertFalse(dataAccess.userExists(testUser.username()));
    }

    @Test
    void getUser() throws DataAccessException {
        dataAccess.createUser(testUser);
        UserData retrievedUser = dataAccess.getUser(testUser.username());
        Assertions.assertNotNull(retrievedUser);
    }

    @Test
    void getUserFakeUser() throws DataAccessException {
        UserData retrievedUser = dataAccess.getUser("nonExistentUser");
        Assertions.assertNull(retrievedUser);
    }

    @Test
    void userExists() throws DataAccessException {
        dataAccess.createUser(testUser);
        boolean exists = dataAccess.userExists(testUser.username());
        Assertions.assertTrue(exists);
    }

    @Test
    void userDoesNotExist() throws DataAccessException {
        boolean exists = dataAccess.userExists("nonExistentUser");
        Assertions.assertFalse(exists);
    }


    //Validate Password

    @Test
    void userHasPassword() throws DataAccessException {
        dataAccess.createUser(testUser);

        Assertions.assertTrue(dataAccess.validatePassword(testUser));

    }

    @Test
    void userHasInvalidPassword() throws DataAccessException {
        dataAccess.createUser(testUser);
        Assertions.assertFalse(dataAccess.validatePassword(new UserData(testUser.username(), testUser.email(), "fakePassword")));
    }


    @Test
    void invalidUserHasValidPassword() throws DataAccessException {
        dataAccess.createUser(testUser);
        Assertions.assertFalse(dataAccess.validatePassword(new UserData("Fake User", testUser.email(), testUser.password())));
    }

    //Add Authdata
    @Test
    void addAuthdata() throws DataAccessException {
        dataAccess.addAuth(testAuth);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM authdata WHERE username = ?")) {
            pstmt.setString(1, testUser.username());
            ResultSet rs = pstmt.executeQuery();

            Assertions.assertTrue(rs.next(), "Entry should exist in the database");
            Assertions.assertEquals(testAuth.authToken(), rs.getString("authToken"), "Auth token should match");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to access Authdata DB", e);
        }
    }


    @Test
    void addMultipleAuthdata() throws DataAccessException {
        dataAccess.addAuth(testAuth);
        dataAccess.addAuth(new AuthData(testAuth.username(), "SecoundAUthtoken"));

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM authdata WHERE username = ?")) {
            pstmt.setString(1, testUser.username());
            ResultSet rs = pstmt.executeQuery();

            Assertions.assertTrue(rs.next(), "Entry should exist in the database");
            Assertions.assertTrue(rs.next(), "Secound entry should exist in the database");
            Assertions.assertNotEquals(testAuth.authToken(), rs.getString("authToken"), "Auth tokens should not match");
            Assertions.assertEquals(testAuth.username(), rs.getString("username"), "Usernames should match");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to access Authdata DB", e);
        }
    }

//    boolean validateUserHasAuthdata(AuthData authData) throws DataAccessException;
//
//    AuthData getAuthdataFromAuthtoken(String authToken) throws DataAccessException;
//
//    boolean validateAuthToken(String authToken) throws DataAccessException;
//
//    boolean authTokenExists(String authToken) throws DataAccessException;
//
//    void removeAuth(String authData) throws DataAccessException;
//
//
//    void createGame(GameData game) throws DataAccessException;
//
//    void updateGame(GameData game) throws DataAccessException;
//
//    int numGames() throws DataAccessException;
//
//    boolean gameIDExists(int gameID) throws DataAccessException;
//
//    GameData getGame(int gameID) throws DataAccessException;
//
//    Collection<GameData> listGames() throws DataAccessException;
//
//
}