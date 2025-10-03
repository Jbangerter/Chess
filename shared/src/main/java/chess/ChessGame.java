package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static chess.ChessPiece.PieceType.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor teamTurn = TeamColor.WHITE;
    ChessBoard gameBoard;

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> posibleMoves = gameBoard.getPiece(startPosition).pieceMoves(gameBoard, startPosition);

        return posibleMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessMove> allMoves = new ArrayList<>();
        ChessPosition kingPosition = getKingPosition(teamColor);
        System.out.println(kingPosition);

        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                var workingPiece = gameBoard.getPiece(new ChessPosition(row, column));
                if (workingPiece != null) {
                    if (workingPiece.getTeamColor() != teamColor) {
                        for (ChessMove move : workingPiece.pieceMoves(gameBoard, new ChessPosition(row, column))) {
//                            System.out.print(workingPiece.getPieceType());
//                            System.out.println(move);

                            System.out.print(move.getEndPosition());
                            System.out.print(kingPosition);
// This is super Hacky need to fix Equals on position
                            if ((move.getEndPosition().getRow() == kingPosition.getRow()) && (move.getEndPosition().getColumn() == kingPosition.getColumn())) {
                                System.out.print("hi");
                                return true;
                            }
                        }
                    }
                }
            }
        }


        return false;
    }


    private ChessPosition getKingPosition(TeamColor colorOfKing) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                if (gameBoard.getPiece(new ChessPosition(row, column)) != null) {
                    if (gameBoard.getPiece(new ChessPosition(row, column)).getPieceType() == KING) {
                        if (gameBoard.getPiece(new ChessPosition(row, column)).getTeamColor() == colorOfKing) {
                            return new ChessPosition(row, column);
                        }
                    }
                }
            }
        }
        throw new RuntimeException("No King Found, board is in invalid board state");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        throw new RuntimeException("Not implemented 29");
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(teamTurn);
    }
}
