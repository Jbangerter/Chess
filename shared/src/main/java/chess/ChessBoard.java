package chess;

import java.util.Arrays;
import java.util.Objects;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static chess.ChessPiece.PieceType.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    public void removePiece(ChessPosition position) {
        squares[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */

    public void resetBoard() {
        squares = new ChessPiece[8][8];

        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                //places WHITE rear row
                if (row == 0) {
                    fillBackRow(row, col, WHITE);
                }
                //places WHITE PAWNS
                if (row == 1) {
                    squares[row][col] = new ChessPiece(WHITE, PAWN);
                }

                // places BLACK PAWNS
                if (row == 6) {
                    squares[row][col] = new ChessPiece(BLACK, PAWN);
                }
                // places BLACK rear row
                if (row == 7) {
                    fillBackRow(row, col, BLACK);
                }
            }
        }
    }

    private void fillBackRow(int row, int col, ChessGame.TeamColor teamColor) {
        if (col == 0 || col == 7) {
            squares[row][col] = new ChessPiece(teamColor, ROOK);
        } else if (col == 1 || col == 6) {
            squares[row][col] = new ChessPiece(teamColor, KNIGHT);
        } else if (col == 2 || col == 5) {
            squares[row][col] = new ChessPiece(teamColor, BISHOP);
        } else if (col == 3) {
            squares[row][col] = new ChessPiece(teamColor, QUEEN);
        } else if (col == 4) {
            squares[row][col] = new ChessPiece(teamColor, KING);
        }
    }

    public String boardAsString() {
        String board = "";

        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                board = board.concat(squares[row][col] + " ");
            }
            board = board.concat("\n");

        }
        return board;
    }

    public char[][] boardAsArray() {
        char[][] board = new char[8][8];

        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                ChessPiece piece = squares[row][col];
                if (piece != null) {
                    board[row][col] = squares[row][col].getPieceSymbol();
                } else {
                    board[row][col] = ' ';
                }
            }
        }
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    @Override
    public String toString() {
        return boardAsString();
    }

    public ChessBoard deepCopy() {
        ChessBoard newBoard = new ChessBoard();

        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                ChessPiece piece = this.squares[row][col];
                if (piece != null) {
                    newBoard.squares[row][col] = piece.deepCopy();
                } else {
                    newBoard.squares[row][col] = null;
                }
            }
        }
        return newBoard;
    }
}