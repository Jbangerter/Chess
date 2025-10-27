package UserService;

import Exceptions.*;
import dataaccess.*;
import org.junit.jupiter.api.*;

import service.UserService;

import model.UserData;
import model.AuthData;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserService Tests")
public class UserServiceTests {

    private MemoryDataAccess dataAccess;
    private UserService userService;
    private UserData testUser;
    private UserData existingUser;
    private AuthData existingUserAuth;


    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        dataAccess.clear();
        userService = new UserService(dataAccess);

        existingUser = new UserData("Exisitingusername", "Exisitingemail", "Exisitingpasssword");
        testUser = new UserData("username", "email", "passsword");

        existingUserAuth = userService.register(existingUser);
    }


    //REGISTER TESTS

    @Test
    void registerNewValidUser() {

        AuthData authData = userService.register(testUser);

        assertNotNull(authData, "AuthData should not be null");
        assertNotNull(authData.authToken(), "Auth token should be generated");
        assertEquals(testUser.username(), authData.username(), "AuthData username should match input");
    }

    @Test
    void registerMissingUsernameThrowsBadRequestException() {
        UserData invalidUser = new UserData(null, "password", "email");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.register(invalidUser));
        assertEquals("Error: bad request", exception.getMessage());

    }

    @Test
    void registerMissingEmailThrowsBadRequestException() {
        UserData invalidUser = new UserData("user", "password", null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.register(invalidUser));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void registerMssingPasswordThrowsBadRequestException() {
        UserData invalidUser = new UserData("user", null, "email");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.register(invalidUser));
        assertEquals("Error: bad request", exception.getMessage());
    }


    @Test
    void registerExistingUserThrowsAlreadyTakenException() {
        userService.register(testUser);

        AlreadyTakenException exception = assertThrows(AlreadyTakenException.class, () -> userService.register(testUser));
        assertEquals("Error: already taken", exception.getMessage());

    }


    //LOGIN TESTS

    @Test
    void loginValidUser() {

        AuthData authData = userService.login(existingUser);

        assertNotNull(authData, "AuthData should not be null");
        assertNotNull(authData.authToken(), "Auth token should be generated");
        assertEquals(existingUser.username(), authData.username(), "AuthData username should match input");
    }

    @Test
    void loginInvalidPassword() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> userService.login(new UserData(existingUser.username(), null, "nottherightpassword")));
        assertEquals("Error: unauthorized", exception.getMessage());

    }

    @Test
    void loginNonregisteredUser() {

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> userService.login(testUser));
        assertEquals("Error: unauthorized", exception.getMessage());


    }

    @Test
    void registerMissingFieldsThrowsBadRequestException() {
        UserData invalidUser = new UserData(null, null, "password");

        UserData finalInvalidUser = invalidUser;
        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.login(finalInvalidUser));
        assertEquals("Error: bad request", exception.getMessage());


        invalidUser = new UserData("user", "email", null);

        UserData finalInvalidUser2 = invalidUser;
        exception = assertThrows(BadRequestException.class, () -> userService.register(finalInvalidUser2));
        assertEquals("Error: bad request", exception.getMessage());
    }

    //LOGOUT TESTS

    @Test
    public void logoutSuccess() {

        userService.logout(existingUserAuth.authToken());

        assertFalse(dataAccess.authTokenExists(existingUserAuth.authToken()));
        assertFalse(dataAccess.validateUserHasAuthdata(existingUserAuth));
    }

    @Test
    public void logoutInvalid() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> userService.logout("HiIamInvalidUUID"));
        assertEquals("Error: unauthorized", exception.getMessage());


    }
}