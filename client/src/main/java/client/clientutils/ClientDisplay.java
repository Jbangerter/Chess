package client.clientutils;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;

import static chess.EscapeSequences.*;
import static chess.EscapeSequences.SET_BG_COLOR_DARK_GREEN;

public class ClientDisplay {


    private final String textColorPrimary = SET_TEXT_COLOR_BLUE;
    private final String textColorSecoundary = SET_TEXT_COLOR_LIGHT_GREY;

    private final String boardEdgeColor = "\u001b[48;5;236m";
    private final String boardWhiteSquare = "\u001b[48;5;121m";
    private final String boardBlackSquare = SET_BG_COLOR_DARK_GREEN;
    private String currentUser;
    private ChessGame.TeamColor userColor;
    private ChessBoard gameBoard;


    public ChessPosition stringToPos(String stringPos) {
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


    public String screenFormater(String currentUser, ChessGame.TeamColor userColor, ChessBoard gameBoard, boolean loggedIn, boolean inGame, boolean observing, String message) {
        this.currentUser = currentUser;
        this.userColor = userColor;
        this.gameBoard = gameBoard;
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


    public String screenFormater(String currentUser, ChessGame.TeamColor userColor, ChessBoard gameBoard, boolean loggedIn, boolean inGame, boolean observing) {
        return screenFormater(currentUser, userColor, gameBoard, loggedIn, inGame, observing, "");
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


    public String[] getPreLogginHelp() {
        return preLogginHelp;
    }

    public String[] getPostLogginHelp() {
        return postLogginHelp;
    }

    public String[] getInGameHelp() {
        return inGameHelp;
    }

    public String[] getObservingHelp() {
        return observingHelp;
    }
}


