package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataAccessTest {

    private MemoryDataAccess dataAccess;
    private UserData testUser;

    @BeforeEach
    public void setUp() {
        dataAccess = new MemoryDataAccess();
        dataAccess.clear();

        testUser = new UserData("username", "email", "passsword");
    }

    @Test
    void createUser() {
        dataAccess.createUser(testUser);
        UserData retrievedUser = dataAccess.getUser(testUser.username());
        Assertions.assertNotNull(retrievedUser);
        Assertions.assertEquals(testUser, retrievedUser);

    }

    @Test
    void clear() {
        dataAccess.createUser(testUser);
        dataAccess.clear();
        UserData retrievedUser = dataAccess.getUser(testUser.username());
        Assertions.assertNull(retrievedUser);
        Assertions.assertFalse(dataAccess.userExists(testUser.username()));
    }

    @Test
    void getUser() {
        dataAccess.createUser(testUser);
        UserData retrievedUser = dataAccess.getUser(testUser.username());
        Assertions.assertNotNull(retrievedUser);
    }

    @Test
    void getUserFakeUser() {
        UserData retrievedUser = dataAccess.getUser("nonExistentUser");
        Assertions.assertNull(retrievedUser);
    }

    @Test
    void userExists() {
        dataAccess.createUser(testUser);
        boolean exists = dataAccess.userExists(testUser.username());
        Assertions.assertTrue(exists);
    }

    @Test
    void userDoesNotExist() {
        boolean exists = dataAccess.userExists("nonExistentUser");
        Assertions.assertFalse(exists);
    }


}