package UserService;

import Exceptions.*;
import dataaccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import service.UserService;

import model.UserData;
import model.AuthData;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserService Tests")
public class UserServiceTests {

    private MemoryDataAccess dataAccess;
    private UserService userService;
    private UserData testUser;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        dataAccess.clear();
        userService = new UserService(dataAccess);

        testUser = new UserData("username", "email", "passsword");
    }

    @Test
    @DisplayName("register: should successfully register a new user and return AuthData")
    void register_newValidUser_returnsAuthData() {
        // Arrange: The TestDataAccess is empty by default.

        // Act: Perform the registration
        AuthData authData = userService.register(testUser);

        // Assert: Verify the outcome and the interactions with the stub
        assertNotNull(authData, "AuthData should not be null");
        assertNotNull(authData.authToken(), "Auth token should be generated");
        assertEquals(testUser.username(), authData.username(), "AuthData username should match input");
    }

    @Test
    @DisplayName("register: should throw BadRequestException for user with missing username")
    void register_missingUsername_throwsBadRequestException() {
        UserData invalidUser = new UserData(null, "password", "email");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.register(invalidUser));
        assertEquals("Error: bad request", exception.getMessage());

    }

    @Test
    @DisplayName("register: should throw BadRequestException for user with missing email")
    void register_missingEmail_throwsBadRequestException() {
        UserData invalidUser = new UserData("user", "password", null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.register(invalidUser));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    @DisplayName("register: should throw BadRequestException for user with missing password")
    void register_missingPassword_throwsBadRequestException() {
        UserData invalidUser = new UserData("user", null, "email");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.register(invalidUser));
        assertEquals("Error: bad request", exception.getMessage());
    }


    @Test
    @DisplayName("register: should throw AlreadyTakenException if username already exists")
    void register_existingUser_throwsAlreadyTakenException() {
        userService.register(testUser);

        AlreadyTakenException exception = assertThrows(AlreadyTakenException.class, () -> userService.register(testUser));
        assertEquals("Error: already taken", exception.getMessage());

    }
}