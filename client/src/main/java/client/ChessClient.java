package client;

import chess.*;
import client.clientutils.ClientDisplay;
import client.websocket.WebSocketFacade;
import com.google.gson.Gson;
import exceptions.HttpResponseException;
import model.AuthData;
import model.UserData;
import model.gameservicerecords.CreateGameInput;
import model.gameservicerecords.JoinGameInput;
import model.gameservicerecords.ShortenedGameData;
import client.websocket.MessageObserver;
import serverfacade.ServerFacade;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static chess.EscapeSequences.*;

public class ChessClient implements MessageObserver {
    private final ServerFacade server;
    private final WebSocketFacade webSocket;
    private final ClientDisplay display;

    private String currentUser = null;
    private AuthData currentAuthData;
    private int currentGameID = -1;
    private ChessGame.TeamColor userColor = null;
    private ChessBoard gameBoard = new ChessBoard();


    private boolean loggedIn = false;
    private boolean inGame = false;
    private boolean observing = false;
    private boolean quitLoop = false;


    public ChessClient(String serverUrl, String webSocketUri) throws Exception {
        server = new ServerFacade(serverUrl);
        webSocket = new WebSocketFacade(webSocketUri, this);
        display = new ClientDisplay();
        gameBoard.resetBoard();
    }

    public void run() {
        System.out.println("Welcome to 240 Chess\nSign in to star or type help for help.");
        System.out.print(display.screenFormater(currentUser, userColor, gameBoard, loggedIn, inGame, observing));

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!quitLoop) {

            String input = scanner.nextLine();

            try {
                result = evaluate(input);
                System.out.print(display.screenFormater(currentUser, userColor, gameBoard, loggedIn, inGame, observing, result));
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println("Exiting...");
    }

    private String stringMenu(String[] entries) {
        return String.join("\n", entries);
    }


    private String help() {
        if (loggedIn && !inGame && !observing) {
            return stringMenu(display.getPostLogginHelp());
        } else if (inGame && !observing) {
            return stringMenu(display.getInGameHelp());
        } else if (!inGame && observing) {
            return stringMenu(display.getObservingHelp());
        } else {
            return stringMenu(display.getPreLogginHelp());
        }
    }

    public String evaluate(String input) {
        String[] tokens = input.toLowerCase().split(" ");
        String command;
        if (tokens == null || tokens.length == 0) {
            System.out.println(SET_BG_COLOR_RED + "Invalid Command");
            command = "invalid";
        } else {
            command = tokens[0];
        }
        String[] inputs = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (command) {

            //Always Avalible
            case "clear" -> clearScreen();
            case "help" -> help();
            case "quit" -> quit();

            //preLogin
            case "login" -> login(inputs);
            case "register" -> register(inputs);

            //post login
            case "create" -> createGame(inputs);
            case "list" -> listGames();
            case "join" -> joinGame(inputs);
            case "observe" -> observeGame(inputs);
            case "logout" -> logout();

            //Websocket/in game
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame();
            case "move" -> makeMove(inputs);
            case "resign" -> resign();
            case "highlight" -> highlightLegalMoves(inputs);
            default -> "Invalid Command, try one of these:\n" + help();
        };
    }

    public String login(String... inputs) {
        if (inputs.length != 2) {
            return "Error: Expected exactly two arguments: <username> <password>";
        }

        if (inGame) {
            return "Warning: You are currently in a game. Please leave the game then try again.";
        }

        if (loggedIn) {
            logout();
        }

        String username = inputs[0];
        String password = inputs[1];
        UserData user = new UserData(username, null, password);

        try {
            AuthData authData = server.loginUser(user);
            this.currentAuthData = authData;
            this.loggedIn = true;
            this.currentUser = username;

            return String.format("Successfully logged in as '%s'.", authData.username());

        } catch (HttpResponseException e) {
            this.loggedIn = false;
            return String.format("Login failed: %s", e.getStatusMessage());
        } catch (Exception e) {
            this.loggedIn = false;
            return String.format("An unexpected error occurred: %s", e.getMessage());
        }
    }

    public String register(String... inputs) {
        if (inputs.length != 3) {
            return "Error: Expected three arguments: <username> <password> <email>";
        }
        if (inGame) {
            return "Warning: You are currently in a game. Please leave the game then try again.";
        }
        if (loggedIn) {
            logout();
        }

        String username = inputs[0];
        String email = inputs[1];
        String password = inputs[2];
        UserData user = new UserData(username, email, password);

        try {
            AuthData authData = server.registerUser(user);
            this.currentAuthData = authData;
            this.loggedIn = true;
            this.currentUser = username;

            return String.format("Successfully registered and logged in as '%s'.", authData.username());

        } catch (HttpResponseException e) {
            this.loggedIn = false;
            return String.format("Registration failed: %s", e.getStatusMessage());
        } catch (Exception e) {
            this.loggedIn = false;
            return String.format("An unexpected error occurred: %s", e.getMessage());
        }
    }


    public String logout() {
        if (!loggedIn || currentAuthData == null) {
            return "Error: You are not currently logged in.";
        }
        if (inGame) {
            return "Warning: You are currently in a game. Please leave the game then try again.";
        }
        try {
            server.logoutUser(currentAuthData.authToken());
            this.currentAuthData = null;
            this.loggedIn = false;
            this.currentUser = null;
            this.userColor = null;
            this.inGame = false;
            this.observing = false;


            return "Successfully logged out.";
        } catch (HttpResponseException e) {
            return String.format("Logout failed: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("An unexpected error occurred during logout: %s", e.getMessage());
        }
    }

    public String quit() {
        if (inGame) {
            return "Warning: You are currently in a game. Please leave the game then try again.";
        }

        logout();
        quitLoop = true;
        return "";
    }

    public String createGame(String... inputs) {
        if (!loggedIn || currentAuthData == null) {
            return "Error: You must be logged in to create a game.";
        }

        if (inGame) {
            return "Warning: You are currently in a game. Please leave the game then try again.";
        }

        if (inputs.length != 1) {
            return "Error: Expected one argument: <gameName>";
        }

        String gameName = inputs[0];
        String authToken = currentAuthData.authToken();
        CreateGameInput newGame = new CreateGameInput(gameName);

        try {
            Map<String, Double> response = server.createGame(authToken, newGame);
            double gameId = response.get("gameID");

            return String.format("Game '%s' created successfully with ID: %d.", gameName, (int) gameId);

        } catch (HttpResponseException e) {
            return String.format("Create game failed: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("An unexpected error occurred: %s", e.getMessage());
        }
    }

    public String listGames() {
        if (!loggedIn || currentAuthData == null) {
            return "Error: You must be logged in to list games.";
        }
        if (inGame) {
            return "Warning: You are currently in a game. Please leave the game then try again.";
        }
        String authToken = currentAuthData.authToken();

        try {
            List<ShortenedGameData> games = server.listGames(authToken);

            if (games == null || games.isEmpty()) {
                return "No games found.";
            }

            return games.stream()
                    .map(game -> String.format("ID: %-5d | Name: %-20s | White Player: %-15s | Black Player: %-15s",
                            game.gameID(),
                            game.gameName(),
                            game.whiteUsername() != null ? game.whiteUsername() : "None",
                            game.blackUsername() != null ? game.blackUsername() : "None"))
                    .collect(Collectors.joining("\n"));

        } catch (HttpResponseException e) {
            return String.format("List games failed: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("An unexpected error occurred: %s", e.getMessage());
        }
    }

    public String joinGame(String... inputs) {
        if (!loggedIn || currentAuthData == null) {
            return "Error: You must be logged in to join a game.";
        }
        if (inGame) {
            return "Warning: You are currently in a game. Please leave the game then try again.";
        }
        if (inputs.length != 2) {
            return "Error: Expected two arguments: <gameID> <WHITE|BLACK>";
        }

        int gameId;
        try {
            gameId = Integer.parseInt(inputs[0]);
        } catch (NumberFormatException e) {
            return "Error: Game ID must be a valid number.";
        }

        String playerColor = inputs[1].toUpperCase();

        ChessGame.TeamColor playerTeamColor;

        if ("WHITE".equals(playerColor)) {
            playerTeamColor = ChessGame.TeamColor.WHITE;
        } else if ("BLACK".equals(playerColor)) {
            playerTeamColor = ChessGame.TeamColor.BLACK;
        } else {
            return "Error: Invalid color specified. Use WHITE or BLACK.";
        }

        String authToken = currentAuthData.authToken();
        JoinGameInput gameRequest = new JoinGameInput(playerTeamColor, gameId);
        try {
            ChessGame gameState = server.joinGame(authToken, gameRequest).game();
            gameBoard = gameState.getBoard();
            inGame = true;
            observing = false;
            userColor = playerTeamColor;
            currentGameID = gameId;

            webSocket.joinPlayer(currentAuthData.authToken(), currentGameID, userColor);

            return String.format("Successfully joined game %d as the %s Player.", gameId, playerColor);

        } catch (HttpResponseException e) {
            gameBoard = new ChessBoard();
            inGame = false;
            userColor = null;
            currentGameID = -1;
            return String.format("Joining game failed: %s", e.getStatusMessage());
        } catch (Exception e) {
            gameBoard = new ChessBoard();
            inGame = false;
            userColor = null;
            currentGameID = -1;
            return String.format("An unexpected error occurred: %s", e.getMessage());
        }
    }

    public String observeGame(String... inputs) {
        if (!loggedIn || currentAuthData == null) {
            return "Error: You must be logged in to join a game.";
        }
        if (inGame) {
            return "Warning: You are currently in a game. Please leave the game then try again.";
        }
        if (inputs.length != 1) {
            return "Error: Expected one argument: <gameID>";
        }

        int gameId;
        try {
            gameId = Integer.parseInt(inputs[0]);
        } catch (NumberFormatException e) {
            return "Error: Game ID must be a valid number.";
        }

        String authToken = currentAuthData.authToken();
        JoinGameInput gameRequest = new JoinGameInput(ChessGame.TeamColor.WHITE, gameId, true);
        try {
            ChessGame gameState = server.joinGame(authToken, gameRequest).game();
            gameBoard = gameState.getBoard();
            inGame = false;
            observing = true;

            webSocket.joinObserver(currentAuthData.authToken(), currentGameID);

            return String.format("Observing game: %d", gameId);

        } catch (HttpResponseException e) {
            gameBoard = new ChessBoard();
            inGame = false;
            userColor = null;
            observing = false;
            return String.format("Failed to observe game: %s", e.getStatusMessage());
        } catch (Exception e) {
            gameBoard = new ChessBoard();
            inGame = false;
            userColor = null;
            observing = false;
            return String.format("An unexpected error occurred: %s", e.getMessage());
        }
    }

    public String redrawBoard() {
        display.setPrintBoard(true);
        return "";
    }

    public String leaveGame() {
        if (!inGame && !observing) {
            return "Error: You are not currently in a game.";
        }
        try {
            this.inGame = false;
            this.observing = false;
            this.gameBoard = null; // Clean up local state

            webSocket.leave(currentAuthData.authToken(), currentGameID);

            return "Left the game.";
        } catch (Exception e) {
            return String.format("Error leaving game: %s", e.getMessage());
        }
    }

    public String makeMove(String... inputs) {
        if (!inGame || observing) {
            return "Error: You are not currently playing a game.";
        }
        if (inputs.length < 2) {
            return "Error: Expected start and end position (e.g., 'move e2 e4').";
        }

        ChessPosition startPos = display.stringToPos(inputs[0]);
        ChessPosition endPos = display.stringToPos(inputs[1]);
        ChessPiece targetPiece = gameBoard.getPiece(startPos);

        if (targetPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (endPos.getRow() == 8 || endPos.getRow() == 1) {
                if (inputs.length != 3) {
                    return "Error: Your move requires a promotion piece (e.g., 'move " + inputs[0] + " " + inputs[1] + "QUEEN";
                }
                ChessMove move = new ChessMove(startPos, endPos, new ChessPiece(userColor, inputs[2]).getPieceType());

            }
        }

        ChessMove move = new ChessMove(startPos, endPos, null);

        //TODO: make me work

        try {
            webSocket.makeMove(currentAuthData.authToken(), currentGameID, move);
            return "Move sent.";
        } catch (Exception e) {
            return String.format("Error making move: %s", e.getMessage());
        }
    }

    public String resign() {
        if (!inGame || observing) {
            return "Error: You are not currently playing a game.";
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Are you sure you want to resign? (yes/no)");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("yes")) {
            return "Resignation cancelled.";
        }

        try {
            webSocket.resign(currentAuthData.authToken(), currentGameID);
            return "Resigned.";
        } catch (Exception e) {
            return String.format("Error resigning: %s", e.getMessage());
        }
    }

    public String highlightLegalMoves(String... inputs) {
        if (!inGame && !observing) {
            return "Error: You are not viewing a game.";
        }
        if (inputs.length != 1) {
            return "Error: Expected piece position (e.g., 'highlight e2').";
        }

        var game = new ChessGame();
        game.setBoard(gameBoard);
        display.validMoves = game.validMoves(display.stringToPos(inputs[0]));
        display.setPrintBoard(true);
        return "Highlighting moves...";
    }

    private String clearScreen() {
        return "";
    }


    @Override
    public void notify(String message) {
        try {
            Gson gson = new Gson();
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

            switch (serverMessage.getServerMessageType()) {
                case NOTIFICATION:
                    websocket.messages.NotificationMessage notification = gson.fromJson(message, websocket.messages.NotificationMessage.class);
                    System.out.println("\n" + SET_TEXT_COLOR_YELLOW + notification.getMessage() + RESET_TEXT_COLOR);
                    break;

                case LOAD_GAME:
                    websocket.messages.LoadGameMessage loadGame = gson.fromJson(message, websocket.messages.LoadGameMessage.class);
                    this.gameBoard = loadGame.getGame().getBoard();
                    display.setPrintBoard(true);
                    System.out.println(display.screenFormater(currentUser, userColor, gameBoard, loggedIn, inGame, observing, "Board updated by server."));
                    break;

                case ERROR:
                    websocket.messages.ErrorMessage error = gson.fromJson(message, websocket.messages.ErrorMessage.class);
                    System.out.println("\n" + SET_TEXT_COLOR_RED + "ERROR: " + error.getErrorMessage() + RESET_TEXT_COLOR);
                    break;
            }

            System.out.print(display.screenFormater(currentUser, userColor, gameBoard, loggedIn, inGame, observing));

        } catch (Exception e) {
            System.out.println("\n" + SET_TEXT_COLOR_RED + "Failed to parse WebSocket message: " + e.getMessage() + RESET_TEXT_COLOR);
            System.out.print(display.screenFormater(currentUser, userColor, gameBoard, loggedIn, inGame, observing));
        }
    }

}