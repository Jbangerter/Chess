package client;

import chess.ChessBoard;
import chess.ChessGame;
import serverfacade.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private String menuTextColorPrimary = SET_TEXT_COLOR_BLUE;
    private String menuTextColorSecoundary = SET_TEXT_COLOR_LIGHT_GREY;

    private String user = "";
    private ChessGame.TeamColor userColor = null;
    private ChessBoard gameBoard = null;

    private boolean isLoggedIn = false;


    private String[] preLogginHelp = {
            menuTextColorPrimary + "Login <USERNAME> <PASSWORD>" + menuTextColorSecoundary + " - Login existing user",
            menuTextColorPrimary + "Register <USERNAME> <EMAIL> <PASSWORD>" + menuTextColorSecoundary + " - Create new user",
            menuTextColorPrimary + "Help" + menuTextColorSecoundary + " - Display this menu",
            menuTextColorPrimary + "Quit" + menuTextColorSecoundary + " - Quit Chess"

    };

    private String[] postLogginHelp = {
            menuTextColorPrimary + "Create <NAME>" + menuTextColorSecoundary + " - Create Game",
            menuTextColorPrimary + "List" + menuTextColorSecoundary + " - List availible games",

            menuTextColorPrimary + "Join <ID> <WHITE|BLACK>" + menuTextColorSecoundary + " - Join chosen game",
            menuTextColorPrimary + "Observe <ID>" + menuTextColorSecoundary + " - Observe chosen game",

            menuTextColorPrimary + "Logout" + menuTextColorSecoundary + " - Logout",
            menuTextColorPrimary + "Help" + menuTextColorSecoundary + " - Display this menu",
            menuTextColorPrimary + "Quit" + menuTextColorSecoundary + " - Quit Chess"

    };

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);

    }

    public void run() {
        System.out.println("Welcome to 240 Chess\nSign in to star or type help for help.");
        //hprintMenu(preLogginHelp);

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {

            String input = scanner.nextLine();

            try {
                result = evaluate(input);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println("Exiting...");
    }

    private void printScreen(String[] entries) {
        if (gameBoard != null) {
            printBoard(gameBoard);
        }

        for (String entry : entries) {
            System.out.println(entry);
        }
    }

    private String stringMenu(String[] entries) {
        return String.join("\n", entries);
    }


    private String help() {
        if (isLoggedIn) {
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
            case "quit" -> "quit";
            default -> help();
        };
    }

    private String register(String[] inputs) {
    }

    private String createGame(String[] inputs) {

    }

    private String listGames() {
        return "List of Games";
    }

    private String joinGame(String[] inputs) {
        userColor = ChessGame.TeamColor.valueOf(inputs[1].toUpperCase());
        return (user + "joined game: " + inputs[0] + " as " + userColor);
    }

    private String observeGame(String[] inputs) {

    }

    private String logout() {
    }


    private String clearScreen() {
        System.out.println(ERASE_SCREEN);
        return "hi";
    }

    public String login(String... inputs) {
        isLoggedIn = true;
        return (inputs[0] + " logged in with " + inputs[1]);

    }

    private void printBoard(ChessBoard board) {
        System.out.println("Pretend Im a Chess Board");
    }

}
