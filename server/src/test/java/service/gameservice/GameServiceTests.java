package service.gameservice;

import exceptions.*;
import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.*;

import service.GameService;
import service.UserService;

import model.UserData;
import model.AuthData;

import java.util.Collection;

import static chess.ChessGame.TeamColor.BLACK;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameService Tests")
public class GameServiceTests {


    private SqlDataAccess dataAccess;
    private UserService userService;
    private GameService gameService;

    private UserData testUser;
    private UserData existingUser;
    private AuthData existingUserAuth;


    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new SqlDataAccess();
        dataAccess.clear();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);

        existingUser = new UserData("Exisitingusername", "Exisitingemail", "Exisitingpasssword");
        testUser = new UserData("username", "email", "passsword");

        existingUserAuth = userService.register(existingUser);

    }

    @Test
    public void createGame() throws DataAccessException {
        int gameId = gameService.createGame(existingUserAuth.authToken(), "exampleGame");

        System.out.println(gameId);
        assertNotNull(gameId, "Didnt return Game ID");
        assertTrue(gameId > 0, "Result returned bad game ID");
    }

    @Test
    public void unauthorizedCreateGame() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> gameService.createGame("InvalidAuth", "exampleGame"));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void createGameBadData() {

        BadRequestException exception = assertThrows(BadRequestException.class, () -> gameService.createGame(existingUserAuth.authToken(), null));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        int gameId = gameService.createGame(existingUserAuth.authToken(), "exampleGame");


        gameService.joinGame(existingUserAuth.authToken(), BLACK, gameId);

        GameData game = dataAccess.getGame(gameId);

        assertEquals(game.blackUsername(), existingUser.username());
        assertNull(game.whiteUsername());

    }

    @Test
    public void unauthorizedJoinGame() throws DataAccessException {
        int gameId = gameService.createGame(existingUserAuth.authToken(), "exampleGame");

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> gameService.joinGame("asdf", BLACK, gameId));
        assertEquals("Error: unauthorized", exception.getMessage());

        GameData game = dataAccess.getGame(gameId);

        assertNull(game.blackUsername());
        assertNull(game.whiteUsername());
    }

    @Test
    public void joinGameColorTaken() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> gameService.createGame(existingUserAuth.authToken(), null));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void listNoGames() throws DataAccessException {
        Collection<GameData> gameList = dataAccess.listGames();

        assertEquals(0, gameList.size());
    }

    @Test
    public void listGames() throws DataAccessException {
        gameService.createGame(existingUserAuth.authToken(), "exampleGame1");
        gameService.createGame(existingUserAuth.authToken(), "exampleGame2");
        gameService.createGame(existingUserAuth.authToken(), "exampleGame3");
        gameService.createGame(existingUserAuth.authToken(), "exampleGame4");

        Collection<GameData> gameList = dataAccess.listGames();

        assertEquals(4, gameList.size());
    }

}
