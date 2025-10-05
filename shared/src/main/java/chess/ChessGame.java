package chess;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
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
        return checkForCheck(gameBoard, teamColor);
    }

    private boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        return checkForCheck(board, teamColor);
    }

    private boolean checkForCheck(ChessBoard board, TeamColor teamColor) {
        Collection<ChessMove> allMoves = new ArrayList<>();
        ChessPosition kingPosition = getKingPosition(board, teamColor);

        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                var workingPiece = board.getPiece(new ChessPosition(row, column));
                if (workingPiece != null) {
                    if (workingPiece.getTeamColor() != teamColor) {
                        for (ChessMove move : workingPiece.pieceMoves(board, new ChessPosition(row, column))) {
                            if (move.getEndPosition().equals(kingPosition)) {
                                System.out.println(workingPiece);
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
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ArrayList<ChessPosition> friendlyPieces = getPieces(gameBoard, teamColor);

        for (ChessPosition piece : friendlyPieces) {
            possibleMoves.addAll(gameBoard.getPiece(piece).pieceMoves(gameBoard, piece));
        }


        for (ChessMove move : possibleMoves) {
            ChessBoard simulatedBoard = gameBoard;
            simulateMove(simulatedBoard, move);

            if (!isInCheck(simulatedBoard, teamColor)) {
                return false;
            }

        }
        return true;
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
                if (attackingPiece != null) {
                    System.out.println(attackingPiece.getPieceType() + " " + row + "," + column);

                    if (attackingPiece.getTeamColor() == attackers) {
                        ArrayList<ChessMove> attacks = new ArrayList<>(attackingPiece.pieceMoves(board, new ChessPosition(row, column)));
                        if (attackingPiece.getPieceType() == PAWN) {
                            ChessMove pawnReference = attacks.getLast();
                            //attacks.add(new ChessMove(pawnReference.getStartPosition(), new ChessPosition(pawnReference.row, pawnReference.col +1)))
                        }
                        for (ChessMove move : attacks) {
                            if (move.getEndPosition().equals(target)) {
                                //System.out.println(move);
                                return true;
                            }
                        }

                    }
                }
            }
        }
        return false;
    }

    private ArrayList<ChessPosition> getPieces(ChessBoard board, TeamColor teamColor) {
        ArrayList<ChessPosition> pieces = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, column));
                if (piece != null) {
                    if (piece.getTeamColor() == teamColor) {
                        pieces.add(new ChessPosition(row, column));
                    }
                }
            }
        }

        return pieces;
    }


    private TeamColor getOpposingTeam(TeamColor team) {
        if (team == TeamColor.BLACK) {
            return TeamColor.WHITE;
        } else {
            return TeamColor.BLACK;
        }
    }


    private void simulateMove(ChessBoard board, ChessMove move) {
        var simulatedPiece = board.getPiece(move.getStartPosition());

        board.removePiece(move.getStartPosition());
        board.addPiece(move.getEndPosition(), simulatedPiece);
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
