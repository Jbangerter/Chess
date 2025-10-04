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
        Collection<ChessMove> possibleMoves = gameBoard.getPiece(startPosition).pieceMoves(gameBoard, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();
        ChessPiece targetPiece = gameBoard.getPiece(startPosition);

        for (ChessMove move : possibleMoves) {
            ChessBoard futureBoard = gameBoard;
            if (move.getEndPosition() == null) {
                futureBoard.addPiece(move.getEndPosition(), targetPiece);
                futureBoard.removePiece(move.getStartPosition());
            } else {
                targetPiece.promote(move.getPromotionPiece());
                futureBoard.addPiece(move.getEndPosition(), targetPiece);
                futureBoard.removePiece(move.getStartPosition());
            }
        }

        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece targetPiece = gameBoard.getPiece(move.getStartPosition());
        chess.ChessPiece.PieceType targetPromotion = move.getPromotionPiece();


    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessMove> allMoves = new ArrayList<>();
        ChessPosition kingPosition = getKingPosition(gameBoard, teamColor);
        System.out.println(kingPosition);

        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                var workingPiece = gameBoard.getPiece(new ChessPosition(row, column));
                if (workingPiece != null) {
                    if (workingPiece.getTeamColor() != teamColor) {
                        for (ChessMove move : workingPiece.pieceMoves(gameBoard, new ChessPosition(row, column))) {
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


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
         var kingLocation = getKingPosition(gameBoard, teamColor);
         Collection<ChessMove> possibleKingMoves = new ChessPiece(teamColor, KING).pieceMoves(gameBoard, kingLocation);
         for(ChessMove move : possibleKingMoves){
             if(! positionIsAttacked(gameBoard, move.getEndPosition(), ))
         }
    }


    private ChessPosition getKingPosition(ChessBoard board, TeamColor colorOfKing) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                if (board.getPiece(new ChessPosition(row, column)) != null) {
                    if (board.getPiece(new ChessPosition(row, column)).getPieceType() == KING) {
                        if (board.getPiece(new ChessPosition(row, column)).getTeamColor() == colorOfKing) {
                            return new ChessPosition(row, column);
                        }
                    }
                }
            }
        }
        throw new RuntimeException("No King Found, board is in invalid board state");
    }

    private boolean positionIsAttacked(ChessBoard board, ChessPosition target, TeamColor attackers) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPiece attackingPiece = board.getPiece(new ChessPosition(row, column));
                if (attackingPiece != null){
                    if(attackingPiece.getTeamColor() == attackers) {
                        for (ChessMove move : attackingPiece.pieceMoves(board, new ChessPosition(row, column))) {
                            if(move.getEndPosition() == target){
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
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
