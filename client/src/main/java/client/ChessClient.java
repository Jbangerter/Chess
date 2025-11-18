package client;

import chess.ChessBoard;
import chess.ChessGame;
import exceptions.HttpResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import model.gameservicerecords.CreateGameInput;
import model.gameservicerecords.ShortenedGameData;
import serverfacade.ServerFacade;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static chess.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private String textColorPrimary = SET_TEXT_COLOR_BLUE;
    private String textColorSecoundary = SET_TEXT_COLOR_LIGHT_GREY;

    private String boardEdgeColor = "\u001b[48;5;236m";
    private String boardWhiteSquare = "\u001b[48;5;121m";
    private String boardBlackSquare = SET_BG_COLOR_DARK_GREEN;

    private String currentUser = null;
    private AuthData currentAuthData;
    private ChessGame.TeamColor userColor = null;
    private ChessBoard gameBoard = new ChessBoard();


    private boolean loggedIn = false;
    private boolean isInGame = false;
    private boolean quitLoop = false;


    private String[] preLogginHelp = {
            textColorPrimary + "Login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + " - Login existing user",
            textColorPrimary + "Register <USERNAME> <EMAIL> <PASSWORD>" + RESET_TEXT_COLOR + " - Create new user",
            textColorPrimary + "Help" + RESET_TEXT_COLOR + " - Display this menu",
            textColorPrimary + "Quit" + RESET_TEXT_COLOR + " - Quit Chess"

    };

    private String[] postLogginHelp = {
            textColorPrimary + "Create <NAME>" + RESET_TEXT_COLOR + " - Create Game",
            textColorPrimary + "List" + RESET_TEXT_COLOR + " - List availible games",

            textColorPrimary + "Join <ID> <WHITE|BLACK>" + RESET_TEXT_COLOR + " - Join chosen game",
            textColorPrimary + "Observe <ID>" + RESET_TEXT_COLOR + " - Observe chosen game",

            textColorPrimary + "Logout" + RESET_TEXT_COLOR + " - Logout",
            textColorPrimary + "Help" + RESET_TEXT_COLOR + " - Display this menu",
            textColorPrimary + "Quit" + RESET_TEXT_COLOR + " - Quit Chess"

    };

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
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
        if (loggedIn) {
            return stringMenu(postLogginHelp);
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
            case "login" -> login(inputs);
            case "register" -> register(inputs);
            case "create" -> createGame(inputs);
            case "list" -> listGames();
            case "join" -> joinGame(inputs);
            case "observe" -> observeGame(inputs);
            case "logout" -> logout();
            case "help" -> help();
            case "clear" -> clearScreen();
            case "quit" -> quit();
            default -> "Invalid Command, try one of these:\n" + help();
        };
    }

    public String login(String... inputs) {
        if (inputs.length != 2) {
            return "Error: Expected exactly two arguments: <username> <password>";
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
            return String.format("Registration failed: %s", e.getMessage());
        } catch (Exception e) {
            this.loggedIn = false;
            return String.format("An unexpected error occurred: %s", e.getMessage());
        }
    }


    public String logout() {
        if (!loggedIn || currentAuthData == null) {
            return "Error: You are not currently logged in.";
        }

        try {
            server.logoutUser(currentAuthData.authToken());
            this.currentAuthData = null;
            this.loggedIn = false;
            this.currentUser = null;


            return "Successfully logged out.";
        } catch (HttpResponseException e) {
            return String.format("Logout failed: %s", e.getMessage());
        } catch (Exception e) {
            return String.format("An unexpected error occurred during logout: %s", e.getMessage());
        }
    }

    public String quit() {
        logout();
        quitLoop = true;
        return "";
    }

    public String createGame(String... inputs) {
        if (!loggedIn || currentAuthData == null) {
            return "Error: You must be logged in to create a game.";
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

        String authToken = currentAuthData.authToken();

        try {
            // The facade returns a List<GameData>
            List<ShortenedGameData> games = server.listGames(authToken);

            if (games == null || games.isEmpty()) {
                return "No games found.";
            }

            // Format the list into a readable string
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


    private String joinGame(String[] inputs) {
        isInGame = true;
        userColor = ChessGame.TeamColor.valueOf(inputs[1].toUpperCase());
        return (currentUser + " joined game: " + inputs[0] + " as " + userColor);
    }

    private String observeGame(String[] inputs) {
        isInGame = true;
        return (currentUser + " observes game: " + inputs[0]);
    }

    private String clearScreen() {
        return "";
    }

    private String screenFormater(String message) {
        String board = "";
        if (gameBoard != null && isInGame == true) {
            board = ERASE_SCREEN + stringBoard(gameBoard) + "\n\n";
        }


        String contentsCheckedForNull = "";
        if (message != null && !message.isEmpty()) {
            contentsCheckedForNull = message + "\n\n";
        }


        String outputIndicator;
        if (loggedIn) {
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
        String stringBoard[][] = new String[10][10];

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


        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {

                System.out.print(stringBoard[row][col]);
            }
            System.out.println();
        }

        return "Pretend Im a Chess Board";
    }

    public static void flipBoard(String[][] board) {
        int N = board.length; // Number of rows/columns (assumed even)
        for (int k = 0; k < (N * N) / 2; k++) {
            int i = k / N;
            int j = k % N;

            int r2 = N - 1 - i;
            int c2 = N - 1 - j;

            flip(board, i, j, r2, c2);
        }
    }

    private static void flip(String[][] board, int r1, int c1, int r2, int c2) {
        String temp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = temp;

    }
}
