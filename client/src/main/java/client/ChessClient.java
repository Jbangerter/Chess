package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.WebSocketFacade;
import exceptions.HttpResponseException;
import model.AuthData;
import model.UserData;
import model.gameservicerecords.CreateGameInput;
import model.gameservicerecords.JoinGameInput;
import model.gameservicerecords.ShortenedGameData;
import server.websocket.MessageObserver;
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

    private final String textColorPrimary = SET_TEXT_COLOR_BLUE;
    private final String textColorSecoundary = SET_TEXT_COLOR_LIGHT_GREY;

    private final String boardEdgeColor = "\u001b[48;5;236m";
    private final String boardWhiteSquare = "\u001b[48;5;121m";
    private final String boardBlackSquare = SET_BG_COLOR_DARK_GREEN;

    private String currentUser = null;
    private AuthData currentAuthData;
    private ChessGame.TeamColor userColor = null;
    private ChessBoard gameBoard = new ChessBoard();


    private boolean loggedIn = false;
    private boolean inGame = false;
    private boolean observing = false;
    private boolean quitLoop = false;


    private final String[] preLogginHelp = {
            textColorPrimary + "Login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + " - Login existing user",
            textColorPrimary + "Register <USERNAME> <EMAIL> <PASSWORD>" + RESET_TEXT_COLOR + " - Create new user",
            textColorPrimary + "Help" + RESET_TEXT_COLOR + " - Display this menu",
            textColorPrimary + "Quit" + RESET_TEXT_COLOR + " - Quit Chess"

    };

    private final String[] postLogginHelp = {
            textColorPrimary + "Create <NAME>" + RESET_TEXT_COLOR + " - Create Game",
            textColorPrimary + "List" + RESET_TEXT_COLOR + " - List availible games",

            textColorPrimary + "Join <ID> <WHITE|BLACK>" + RESET_TEXT_COLOR + " - Join chosen game",
            textColorPrimary + "Observe <ID>" + RESET_TEXT_COLOR + " - Observe chosen game",

            textColorPrimary + "Logout" + RESET_TEXT_COLOR + " - Logout",
            textColorPrimary + "Help" + RESET_TEXT_COLOR + " - Display this menu",
            textColorPrimary + "Quit" + RESET_TEXT_COLOR + " - Quit Chess"

    };

    private final String[] inGameHelp = {
            textColorPrimary + "Help" + RESET_TEXT_COLOR + " - Display this menu",
            textColorPrimary + "Move <START> <END> [Promo]" + RESET_TEXT_COLOR + " - Make a move (e.g., 'move e2 e4')",
            textColorPrimary + "Highlight <PIECE_LOC>" + RESET_TEXT_COLOR + " - Highlight legal moves (e.g., 'highlight e2')",
            textColorPrimary + "Redraw" + RESET_TEXT_COLOR + " - Redraw the chess board",
            textColorPrimary + "Resign" + RESET_TEXT_COLOR + " - Forfeit the game",
            textColorPrimary + "Leave" + RESET_TEXT_COLOR + " - Leave current game",
    };

    private final String[] observingHelp = {
            textColorPrimary + "Help" + RESET_TEXT_COLOR + " - Display this menu",
            textColorPrimary + "Highlight <PIECE_LOC>" + RESET_TEXT_COLOR + " - Highlight legal moves (e.g., 'highlight e2')",
            textColorPrimary + "Redraw" + RESET_TEXT_COLOR + " - Redraw the chess board",
            textColorPrimary + "Leave" + RESET_TEXT_COLOR + " - Leave current game",
    };

    public ChessClient(String serverUrl, String webSocketUri) throws Exception {
        server = new ServerFacade(serverUrl);
        webSocket = new WebSocketFacade(webSocketUri, this);
        gameBoard.resetBoard();

    }

    public void run() {
        System.out.println("Welcome to 240 Chess\nSign in to star or type help for help.");
        System.out.print(screenFormater());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!quitLoop) {

            String input = scanner.nextLine();

            try {
                result = evaluate(input);
                System.out.print(screenFormater(result));
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
            return stringMenu(postLogginHelp);
        } else if (inGame && !observing) {
            return stringMenu(inGameHelp);
        } else if (!inGame && observing) {
            return stringMenu(observingHelp);
        } else {
            return stringMenu(preLogginHelp);
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
            case "ping" -> ping(inputs);
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame();
            case "move" -> makeMove(inputs);
            case "resign" -> resign();
            case "highlight" -> highlightLegalMoves(inputs);
            default -> "Invalid Command, try one of these:\n" + help();
        };
    }


    public String ping(String... inputs) {
        if (inputs.length != 1) {
            return "Error: Expected exactly one argument: <message>";
        }

        try {
            //webSocket.ping(inputs[0]);

            return "";

        } catch (Exception e) {
            this.loggedIn = false;
            return String.format("An unexpected error occurred: %s", e.getMessage());
        }

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

            return String.format("Successfully joined game %d as the %s Player.", gameId, playerColor);

        } catch (HttpResponseException e) {
            gameBoard = new ChessBoard();
            inGame = false;
            userColor = null;
            return String.format("Joining game failed: %s", e.getStatusMessage());
        } catch (Exception e) {
            gameBoard = new ChessBoard();
            inGame = false;
            userColor = null;
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
        return "";
    }

    public String leaveGame() {
        if (!inGame && !observing) {
            return "Error: You are not currently in a game.";
        }

        try {

            //TODO: make me work

            this.inGame = false;
            this.observing = false;
            this.gameBoard = null; // Clean up local state

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

        ChessPosition startPos = stringToPos(inputs[0]);
        ChessPosition endPos = stringToPos(inputs[1]);
        ChessPiece targetPiece = gameBoard.getPiece(startPos);

        if (targetPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (endPos.getRow() == 8 || endPos.getRow() == 1) {
                if (inputs.length != 3) {
                    return "Error: Your move requires a promotion piece (e.g., 'move " + inputs[0] + " " + inputs[1] + "QUEEN";
                }
            }
        }


        //TODO: make me work

        try {
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
            //TODO: make me work
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

        //TODO: make me work

        return "Highlighting moves...";
    }

    private String clearScreen() {
        return "";
    }

    private ChessPosition stringToPos(String stringPos) {
        if (stringPos == null || stringPos.length() != 2) {
            throw new IllegalArgumentException("Invalid square notation format: " + stringPos);
        }

        char colChar = stringPos.charAt(0);
        char rowChar = stringPos.charAt(1);

        int col = colChar - 'a' + 1;

        int rowValue = Character.getNumericValue(rowChar);
        int row = rowValue;

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Coordinates out of board range: " + stringPos);
        }

        return new ChessPosition(row, col);
    }


    private String screenFormater(String message) {
        String board = "";
        if (gameBoard != null && (inGame || observing)) {
            board = ERASE_SCREEN + stringBoard(gameBoard) + "\n\n";
        }


        String contentsCheckedForNull = "";
        if (message != null && !message.isEmpty()) {
            contentsCheckedForNull = message + "\n\n";
        }


        String outputIndicator;
        if (loggedIn && inGame) {
            outputIndicator = "[" + SET_TEXT_COLOR_GREEN + currentUser + RESET_TEXT_COLOR + ": " + userColor + "] >>> ";
        } else if (loggedIn && observing) {
            outputIndicator = "[" + SET_TEXT_COLOR_GREEN + currentUser + RESET_TEXT_COLOR + ": " + "Observer" + "] >>> ";
        } else if (loggedIn) {
            outputIndicator = "[" + SET_TEXT_COLOR_GREEN + currentUser + RESET_TEXT_COLOR + "] >>> ";
        } else {
            outputIndicator = "[" + SET_TEXT_COLOR_RED + "LOGGED_OUT" + RESET_TEXT_COLOR + "] >>> ";
        }

        return board + contentsCheckedForNull + outputIndicator;
    }


    private String screenFormater() {
        return screenFormater("");
    }

    private String stringBoard(ChessBoard board) {
        var boardArray = board.boardAsArray();
        String[][] stringBoard = new String[10][10];

        String[] columnLabels = {"   ", " a ", " b ", " c ", " d ", " e ", " f ", " g ", " h ", "   "};
        String[] rowLabels = {"   ", " 8 ", " 7 ", " 6 ", " 5 ", " 4 ", " 3 ", " 2 ", " 1 ", "   "};

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (row == 0 || row == 9) {
                    stringBoard[row][col] = boardEdgeColor + columnLabels[col] + RESET_BG_COLOR;
                } else if (col == 0 || col == 9) {
                    stringBoard[row][col] = boardEdgeColor + rowLabels[row] + RESET_BG_COLOR;
                } else if ((row + col) % 2 == 1) {
                    stringBoard[row][col] = boardBlackSquare + boardArray[row - 1][col - 1] + RESET_BG_COLOR;
                } else {
                    stringBoard[row][col] = boardWhiteSquare + boardArray[row - 1][col - 1] + RESET_BG_COLOR;
                }

            }
        }

        if (userColor == ChessGame.TeamColor.BLACK) {
            flipBoard(stringBoard);
        }

        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                sb.append(stringBoard[row][col]);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static void flipBoard(String[][] board) {
        int n = board.length; // Number of rows/columns (assumed even)
        for (int k = 0; k < (n * n) / 2; k++) {
            int i = k / n;
            int j = k % n;

            int r2 = n - 1 - i;
            int c2 = n - 1 - j;

            flip(board, i, j, r2, c2);
        }
    }

    private static void flip(String[][] board, int r1, int c1, int r2, int c2) {
        String temp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = temp;

    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                break;
            case ERROR:
                break;
            case NOTIFICATION:
                break;
        }
    }
}
