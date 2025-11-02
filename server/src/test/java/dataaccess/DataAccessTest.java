package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataAccessTest {

    private static SqlDataAccess dataAccess;
    private static UserData testUser;

    @BeforeAll
    public static void startDB() {
        dataAccess = new SqlDataAccess();

        testUser = new UserData("username", "email", "passsword");
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


}