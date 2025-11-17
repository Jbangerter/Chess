package chess;

import exceptions.InvalidMoveException;

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

    TeamColor teamTurn;
    ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
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
        Collection<ChessMove> possibleMoves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();
        ChessPiece targetPiece = board.getPiece(startPosition);
        TeamColor team = targetPiece.getTeamColor();

        if (isInStalemate(team)) {
            return new ArrayList<>();
        }

        for (ChessMove move : possibleMoves) {
            ChessBoard simulatedBoard = board.deepCopy();
            movePiece(simulatedBoard, move);

            if (!isInCheck(simulatedBoard, team)) {
                if (!isInCheckmate(simulatedBoard, team)) {
                    legalMoves.add(move);
                }
            }
        }


        System.out.println(legalMoves);
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());
        if (movingPiece == null) {
            throw new InvalidMoveException(move + " Targets an empty space.");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (!legalMoves.contains(move)) {
            throw new InvalidMoveException(move + " Is not a legal move.");
        }

        if (movingPiece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException(move + " Targets a " + movingPiece.getTeamColor() + " piece.");
        }

        teamTurn = getOpposingTeam(teamTurn);
        movePiece(board, move);

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return checkForCheck(board, teamColor);
    }

    private boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        return checkForCheck(board, teamColor);
    }

    private boolean checkForCheck(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(board, teamColor);

        ArrayList<ChessPosition> attackingPieces = getPieces(board, getOpposingTeam(teamColor));
        for (ChessPosition workingPosition : attackingPieces) {
            var workingPiece = board.getPiece(workingPosition);
            for (ChessMove move : workingPiece.pieceMoves(board, workingPosition)) {
                if (move.getEndPosition().equals(kingPosition)) {
                    //System.out.println(workingPiece);
                    return true;
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
        return checkmateChecker(board, teamColor);
    }

    public boolean isInCheckmate(ChessBoard board, TeamColor teamColor) {
        return checkmateChecker(board, teamColor);
    }

    private boolean checkmateChecker(ChessBoard board, TeamColor teamColor) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ArrayList<ChessPosition> friendlyPieces = getPieces(board, teamColor);

        return checkAllPiecesForSafeMoves(board, teamColor, possibleMoves, friendlyPieces);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ArrayList<ChessPosition> friendlyPieces = getPieces(board, teamColor);

        if (isInCheck(board, teamColor)) {
            return false;
        }

        return checkAllPiecesForSafeMoves(board, teamColor, possibleMoves, friendlyPieces);

    }


    private boolean checkAllPiecesForSafeMoves(ChessBoard board,
                                               TeamColor teamColor,
                                               Collection<ChessMove> possibleMoves,
                                               ArrayList<ChessPosition> friendlyPieces) {
        for (ChessPosition piece : friendlyPieces) {
            possibleMoves.addAll(board.getPiece(piece).pieceMoves(board, piece));
        }

        for (ChessMove move : possibleMoves) {
            ChessBoard simulatedBoard = board.deepCopy();
            movePiece(simulatedBoard, move);

            if (!isInCheck(simulatedBoard, teamColor)) {
                return false;
            }
        }
        return true;
    }

    private ChessPosition getKingPosition(ChessBoard board, TeamColor colorOfKing) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition positionOfKing = findKingPosition(board, colorOfKing, row, column);
                if (positionOfKing != null) {
                    return positionOfKing;
                }
            }
        }
        throw new RuntimeException("No King Found, board is in invalid board state");
    }

    private static ChessPosition findKingPosition(ChessBoard board, TeamColor colorOfKing, int row, int column) {
        if (board.getPiece(new ChessPosition(row, column)) != null) {
            if (board.getPiece(new ChessPosition(row, column)).getPieceType() == KING) {
                if (board.getPiece(new ChessPosition(row, column)).getTeamColor() == colorOfKing) {
                    return new ChessPosition(row, column);
                }
            }
        }
        return null;
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


    private void movePiece(ChessBoard board, ChessMove move) {
        var piece = board.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        board.removePiece(move.getStartPosition());
        board.addPiece(move.getEndPosition(), piece);
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
