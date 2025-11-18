package client;

import chess.ChessGame;
import model.gameservicerecords.*;
import org.junit.jupiter.api.*;
import server.Server;


import exceptions.*;
import dataaccess.*;
import org.junit.jupiter.api.*;

import serverfacade.ServerFacade;
import service.UserService;

import model.UserData;
import model.AuthData;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    private SqlDataAccess dataAccess;
    private UserService userService;
    private UserData testUser;
    private UserData existingUser;
    private AuthData existingUserAuth;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        String serverUrl = "http://localhost:" + port;
        facade = new ServerFacade(serverUrl);
    }

    @BeforeEach
    public void prep() {
        facade.deleteAll();


        existingUser = new UserData("Exisitingusername", "Exisitingemail", "Exisitingpasssword");
        testUser = new UserData("username", "email", "passsword");

        existingUserAuth = facade.registerUser(existingUser);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    // REGISTER TESTS

    @Test
    void registerNewValidUser() throws HttpResponseException {
        AuthData authData = facade.registerUser(testUser);

        assertNotNull(authData, "AuthData should not be null");
        assertNotNull(authData.authToken(), "Auth token should be generated");
        assertEquals(testUser.username(), authData.username(), "AuthData username should match input");
    }

    @Test
    void registerMissingUsernameThrowsBadRequest() {
        UserData invalidUser = new UserData(null, "password", "email");

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> facade.registerUser(invalidUser));
        assertEquals(400, exception.getStatusCode(), "Should return 400 Bad Request");
        assertTrue(exception.getMessage().contains("Error: bad request"));
    }

    @Test
    void registerExistingUserThrowsAlreadyTaken() {
        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> facade.registerUser(existingUser));
        assertEquals(403, exception.getStatusCode(), "Should return 403 Forbidden/Already Taken");
        assertTrue(exception.getMessage().contains("Error: already taken"));
    }

    // LOGIN TESTS

    @Test
    void loginValidUser() throws HttpResponseException {
        AuthData authData = facade.loginUser(existingUser);

        assertNotNull(authData, "AuthData should not be null");
        assertNotNull(authData.authToken(), "Auth token should be generated");
        assertEquals(existingUser.username(), authData.username(), "AuthData username should match input");
    }

    @Test
    void loginInvalidPassword() {
        var userWithBadPass = new UserData(existingUser.username(), null, "WrongPassword");

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> facade.loginUser(userWithBadPass));
        assertEquals(401, exception.getStatusCode(), "Should return 401 Unauthorized");
        assertTrue(exception.getMessage().contains("Error: unauthorized"));
    }

    @Test
    void loginNonregisteredUser() {
        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> facade.loginUser(testUser));
        assertEquals(401, exception.getStatusCode(), "Should return 401 Unauthorized");
        assertTrue(exception.getMessage().contains("Error: unauthorized"));
    }

    // LOGOUT TESTS

    @Test
    public void logoutSuccess() throws HttpResponseException {
        facade.logoutUser(existingUserAuth.authToken());

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> facade.logoutUser(existingUserAuth.authToken()));
        assertEquals(401, exception.getStatusCode(), "Logged out user should get 401 on reuse of token");
    }

    @Test
    public void logoutInvalid() {
        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> facade.logoutUser("HiIamInvalidUUID"));
        assertEquals(401, exception.getStatusCode(), "Invalid token should return 401 Unauthorized");
        assertTrue(exception.getMessage().contains("Error: unauthorized"));
    }


// CREATE GAME TESTS

    @Test
    public void createGameSuccess() throws HttpResponseException {
        // The facade returns Map<String, Double> based on your implementation return type Map.class
        Map<String, Double> response = facade.createGame(existingUserAuth.authToken(), new CreateGameInput("exampleGame"));

        assertNotNull(response, "Didnt return Game ID");
        assertTrue(response.containsKey("gameID"));
        assertTrue(response.get("gameID") > 0, "Result returned bad game ID");
    }

    @Test
    public void unauthorizedCreateGame() {
        HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                facade.createGame("InvalidAuth", new CreateGameInput("exampleGame"))
        );
        assertEquals(401, exception.getStatusCode(), "Should return 401 Unauthorized");
        assertTrue(exception.getMessage().contains("Error: unauthorized"));
    }

    @Test
    public void createGameBadData() {
        // Passing a null gameName should result in a 400 Bad Request
        HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                facade.createGame(existingUserAuth.authToken(), new CreateGameInput(null))
        );
        assertEquals(400, exception.getStatusCode(), "Should return 400 Bad Request");
        assertTrue(exception.getMessage().contains("Error: bad request"));
    }

    // JOIN GAME TESTS

    @Test
    public void joinGameSuccess() throws HttpResponseException {
        Map<String, Double> createResponse = facade.createGame(existingUserAuth.authToken(), new CreateGameInput("exampleGame"));
        int gameId = createResponse.get("gameID").intValue();

        facade.joinGame(existingUserAuth.authToken(), new JoinGameInput(ChessGame.TeamColor.BLACK, gameId));

        List<ShortenedGameData> games = facade.listGames(existingUserAuth.authToken());
        ShortenedGameData game = games.stream().filter(g -> g.gameID() == gameId).findFirst().orElse(null);

        assertNotNull(game);
        assertEquals(game.blackUsername(), existingUser.username());
        assertNull(game.whiteUsername());
    }


}
