package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private ChessBoard board;
    private ChessPosition myPosition;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //section that outputs our moves.
        //return List.of(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 6), null));

        var pieceType = board.getPiece(myPosition).getPieceType();

        switch (pieceType) {
            case KING:
                return List.of(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 6), null));
            case QUEEN:
                return List.of(new ChessMove(new ChessPosition(2, 5), new ChessPosition(1, 6), null));
            case BISHOP:
                bishopLogic logic = new bishopLogic(board, myPosition);
                return logic.getLegalMoves();
            case KNIGHT:
                return List.of(new ChessMove(new ChessPosition(4, 5), new ChessPosition(1, 6), null));
            case ROOK:
                return List.of(new ChessMove(new ChessPosition(5, 5), new ChessPosition(1, 6), null));
            case PAWN:
                return List.of(new ChessMove(new ChessPosition(6, 5), new ChessPosition(1, 6), null));
            default:
                throw new IllegalArgumentException("Invalid piece type");
        }

    }


    private abstract static class chessLogic {
        protected ChessBoard board;
        protected ChessPosition myPosition;
        protected ChessPiece piece;
        protected ChessGame.TeamColor pieceColor;
        protected boolean canPromote;
        protected boolean canIterate;

        protected Collection<ChessMove> legalMoves = new ArrayList<>();

        public chessLogic(ChessBoard board, ChessPosition myPosition) {
            this.board = board;
            this.myPosition = myPosition;

            piece = board.getPiece(myPosition);
            pieceColor = board.getPiece(myPosition).getTeamColor();
        }

        protected Collection<ChessMove> findLegalMoves(boolean canPromote, boolean canIterate, int[][] validMoves, ChessPosition piecePosition, ChessBoard board, ChessGame.TeamColor pieceColor) {
            Collection<ChessMove> moves = new ArrayList<>(List.of());
            ChessMove nextMove;

            for (int[] validMove : validMoves) {
                exploreLegalMoves(canIterate, piecePosition, piecePosition, validMove[0], validMove[1], board, pieceColor, moves);

            }
            return moves;
        }

        private void exploreLegalMoves(boolean canIterate, ChessPosition initialPosition, ChessPosition position, int deltaRow, int deltaCol, ChessBoard board, ChessGame.TeamColor pieceColor, Collection<ChessMove> moves) {

            if (moveInBounds(position, deltaRow, deltaCol)) {
                ChessPosition nextPosition = new ChessPosition((position.getRow() + deltaRow), (position.getColumn() + deltaCol));
                ChessMove nextMove = new ChessMove(initialPosition, nextPosition, null);
                ChessPiece nextSquare = board.getPiece(nextPosition);

                System.out.println(nextPosition);


                if (nextSquare == null) {
                    //System.out.println("a");
                    moves.add(nextMove);
                    // System.out.println("a.1");
                    exploreLegalMoves(canIterate, initialPosition, nextPosition, deltaRow, deltaCol, board, pieceColor, moves);
                } else if (nextSquare.getTeamColor() != pieceColor) {
                    //System.out.println("b");
                    moves.add(nextMove);
                } else {
                    //System.out.println("c");
                    return;
                }
            }
        }


        private boolean moveInBounds(ChessPosition start, int deltaRow, int deltaCol) {
            if (((start.getRow() + deltaRow) > 8) || ((start.getRow() + deltaRow) < 1)) {
                return false;
            } else if (((start.getColumn() + deltaCol) > 8) || ((start.getColumn() + deltaCol) < 1)) {
                return false;
            } else {
                return true;
            }
        }
    }

    private class bishopLogic extends chessLogic {

        public bishopLogic(ChessBoard board, ChessPosition myPosition) {
            super(board, myPosition);

            canPromote = false;
            canIterate = true;

            int[][] validMoves = {
                    {1, 1},
                    {-1, 1},
                    {-1, -1},
                    {1, -1}
            };

//            System.out.println(Arrays.deepToString(validMoves));
//            System.out.println("Can Promote: " + canPromote);
//            System.out.println("Can Iterate: " + canIterate);
//            System.out.println("Piece Type: " + piece.getPieceType());
//            System.out.println("Piece Color: " + pieceColor);
//            System.out.println("Piece Position: " + myPosition);

            legalMoves = findLegalMoves(canPromote, canIterate, validMoves, myPosition, board, pieceColor);
        }


        public Collection<ChessMove> getLegalMoves() {
            return legalMoves;
        }

    }


//    private Collection<ChessMove> kingLogic(ChessBoard board, ChessPosition myPosition) {
//        this.board = board;
//        this.myPosition = myPosition;
//
//        var piece = board.getPiece(myPosition);
//        var pieceColor = board.getPiece(myPosition).getTeamColor();
//
//        Collection<ChessMove> legalMoves = List.of();
//        List<ChessMove> validMoves = new ArrayList<>();
//        validMoves.add(ChessMove())
//
//
//        System.out.println(myPosition);
//
//        return legalMoves;
//    }
//


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return String.format("[%s %s]", pieceColor, type);
    }
}
