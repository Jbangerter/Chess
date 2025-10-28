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
    private PieceType type;
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

        var pieceType = board.getPiece(myPosition).getPieceType();

        switch (pieceType) {
            case KING:
                return new KingLogic(board, myPosition).getLegalMoves();
            case QUEEN:
                return new QueenLogic(board, myPosition).getLegalMoves();
            case BISHOP:
                return new BishopLogic(board, myPosition).getLegalMoves();
            case KNIGHT:
                return new KnightLogic(board, myPosition).getLegalMoves();
            case ROOK:
                return new RookLogic(board, myPosition).getLegalMoves();
            case PAWN:
                return new PawnLogic(board, myPosition).getLegalMoves();
            default:
                throw new IllegalArgumentException("Invalid piece type");
        }


    }


    private abstract static class ChessLogic {
        protected ChessBoard board;
        protected ChessPosition myPosition;
        protected ChessPiece piece;
        protected ChessGame.TeamColor pieceColor;
        protected boolean canPromote;
        protected boolean canIterate;

        protected Collection<ChessMove> legalMoves = new ArrayList<>();

        public ChessLogic(ChessBoard board, ChessPosition myPosition) {
            this.board = board;
            this.myPosition = myPosition;

            piece = board.getPiece(myPosition);
            pieceColor = board.getPiece(myPosition).getTeamColor();
        }

        protected Collection<ChessMove> findLegalMoves(boolean canPromote,
                                                       boolean canIterate,
                                                       int[][] validMoves,
                                                       ChessPosition piecePosition,
                                                       ChessBoard board,
                                                       ChessGame.TeamColor pieceColor) {
            Collection<ChessMove> moves = new ArrayList<>(List.of());
            ChessMove nextMove;

            for (int[] validMove : validMoves) {
                exploreLegalMoves(canIterate, piecePosition, piecePosition, validMove[0], validMove[1], board, pieceColor, moves);

            }
            return moves;
        }

        private void exploreLegalMoves(boolean canIterate,
                                       ChessPosition initialPosition,
                                       ChessPosition position,
                                       int deltaRow,
                                       int deltaCol,
                                       ChessBoard board,
                                       ChessGame.TeamColor pieceColor,
                                       Collection<ChessMove> moves) {

            if (moveInBounds(position, deltaRow, deltaCol)) {
                ChessPosition nextPosition = new ChessPosition((position.getRow() + deltaRow), (position.getColumn() + deltaCol));
                ChessMove nextMove = new ChessMove(initialPosition, nextPosition, null);
                ChessPiece nextSquare = board.getPiece(nextPosition);


                if (nextSquare == null) {
                    moves.add(nextMove);
                    if (canIterate) {
                        exploreLegalMoves(canIterate, initialPosition, nextPosition, deltaRow, deltaCol, board, pieceColor, moves);
                    }
                } else if (nextSquare.getTeamColor() != pieceColor) {
                    moves.add(nextMove);
                } else {
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

    private class BishopLogic extends ChessLogic {

        public BishopLogic(ChessBoard board, ChessPosition myPosition) {
            super(board, myPosition);

            canPromote = false;
            canIterate = true;

            int[][] validMoves = {
                    {1, 1},
                    {-1, 1},
                    {-1, -1},
                    {1, -1}
            };

            legalMoves = findLegalMoves(canPromote, canIterate, validMoves, myPosition, board, pieceColor);
        }


        public Collection<ChessMove> getLegalMoves() {
            return legalMoves;
        }

    }

    private class KingLogic extends ChessLogic {

        public KingLogic(ChessBoard board, ChessPosition myPosition) {
            super(board, myPosition);

            canPromote = false;
            canIterate = false;

            int[][] validMoves = {

                    {1, 1},
                    {-1, 1},
                    {-1, -1},
                    {1, -1},
                    {0, 1},
                    {0, -1},
                    {1, 0},
                    {-1, 0}

            };
            legalMoves = findLegalMoves(canPromote, canIterate, validMoves, myPosition, board, pieceColor);
        }

        public Collection<ChessMove> getLegalMoves() {
            return legalMoves;
        }
    }

    private class QueenLogic extends ChessLogic {

        public QueenLogic(ChessBoard board, ChessPosition myPosition) {
            super(board, myPosition);

            canPromote = false;
            canIterate = true;

            int[][] validMoves = {
                    {0, 1},
                    {0, -1},
                    {1, 0},
                    {-1, 0},
                    {1, 1},
                    {-1, 1},
                    {-1, -1},
                    {1, -1}
            };
            legalMoves = findLegalMoves(canPromote, canIterate, validMoves, myPosition, board, pieceColor);
        }

        public Collection<ChessMove> getLegalMoves() {
            return legalMoves;
        }
    }

    private class KnightLogic extends ChessLogic {

        public KnightLogic(ChessBoard board, ChessPosition myPosition) {
            super(board, myPosition);

            canPromote = false;
            canIterate = false;

            int[][] validMoves = {
                    {2, 1},
                    {2, -1},
                    {-2, 1},
                    {-2, -1},
                    {1, 2},
                    {1, -2},
                    {-1, 2},
                    {-1, -2},
            };

            legalMoves = findLegalMoves(canPromote, canIterate, validMoves, myPosition, board, pieceColor);
        }


        public Collection<ChessMove> getLegalMoves() {
            return legalMoves;
        }

    }

    private class RookLogic extends ChessLogic {

        public RookLogic(ChessBoard board, ChessPosition myPosition) {
            super(board, myPosition);

            canPromote = false;
            canIterate = true;

            int[][] validMoves = {
                    {0, 1},
                    {0, -1},
                    {1, 0},
                    {-1, 0},
            };

            legalMoves = findLegalMoves(canPromote, canIterate, validMoves, myPosition, board, pieceColor);
        }


        public Collection<ChessMove> getLegalMoves() {
            return legalMoves;
        }

    }

    private class PawnLogic extends ChessLogic {

        public PawnLogic(ChessBoard board, ChessPosition myPosition) {
            super(board, myPosition);

            canPromote = true;
            canIterate = false;

            int[][] validMoves;

            if (pieceColor == ChessGame.TeamColor.BLACK) {
                validMoves = new int[][]{{-1, 0}};
            } else if (pieceColor == ChessGame.TeamColor.WHITE) {
                validMoves = new int[][]{{1, 0}};
            } else {
                throw new RuntimeException("Pawn Color is invalid");
            }

            legalMoves = findLegalMoves(canPromote, canIterate, validMoves, myPosition, board, pieceColor);
        }


        @Override
        protected Collection<ChessMove> findLegalMoves(boolean canPromote,
                                                       boolean canIterate,
                                                       int[][] validMoves,
                                                       ChessPosition piecePosition,
                                                       ChessBoard board,
                                                       ChessGame.TeamColor pieceColor) {
            Collection<ChessMove> moves = new ArrayList<>();


            if (piecePosition.getRow() == 2 || piecePosition.getRow() == 7) {
                if (board.getPiece(new ChessPosition(piecePosition.getRow() + validMoves[0][0], piecePosition.getColumn())) == null) {
                    tryMove(piecePosition, validMoves[0][0] + validMoves[0][0], 0, pieceColor, false, moves);
                }
            }

            if (piecePosition.getColumn() == 8) {
                findBlackPawnPosistions(validMoves, piecePosition, board, pieceColor, moves);

            } else if (piecePosition.getColumn() == 1) {
                findWhitePawnPosition(validMoves, piecePosition, board, pieceColor, moves);
            } else {

                findBlackPawnPosistions(validMoves, piecePosition, board, pieceColor, moves);

                findWhitePawnPosition(validMoves, piecePosition, board, pieceColor, moves);
            }

            tryMove(piecePosition, validMoves[0][0], 0, pieceColor, false, moves);


            return moves;
        }

        private void findWhitePawnPosition(int[][] validMoves,
                                           ChessPosition piecePosition,
                                           ChessBoard board,
                                           ChessGame.TeamColor pieceColor,
                                           Collection<ChessMove> moves) {
            int nextRow = piecePosition.getRow() + validMoves[0][0];
            int nextCol = piecePosition.getColumn() + 1;
            ChessPosition newPosition = new ChessPosition(nextRow, nextCol);

            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece != null && targetPiece.getTeamColor() != pieceColor) {
                tryMove(piecePosition, validMoves[0][0], 1, pieceColor, true, moves);
            }
        }

        private void findBlackPawnPosistions(int[][] validMoves,
                                             ChessPosition piecePosition,
                                             ChessBoard board,
                                             ChessGame.TeamColor pieceColor,
                                             Collection<ChessMove> moves) {
            int nextRow = piecePosition.getRow() + validMoves[0][0];
            int nextCol = piecePosition.getColumn() - 1;
            ChessPosition newPosition = new ChessPosition(nextRow, nextCol);

            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece != null && targetPiece.getTeamColor() != pieceColor) {
                tryMove(piecePosition, validMoves[0][0], -1, pieceColor, true, moves);
            }
        }

        public Collection<ChessMove> getLegalMoves() {
            return legalMoves;
        }

        private void tryMove(ChessPosition startPosition, int deltaRow, int deltaCol, ChessGame.TeamColor pieceColor, boolean isAttcking, Collection<ChessMove> moves) {


            if (((startPosition.getRow() + deltaRow) > 8) || ((startPosition.getRow() + deltaRow) < 1)) {
                return;
            } else if (((startPosition.getColumn() + deltaCol) > 8) || ((startPosition.getColumn() + deltaCol) < 1)) {
                return;
            }

            ChessPosition nextPosition = new ChessPosition((startPosition.getRow() + deltaRow), (startPosition.getColumn() + deltaCol));
            ChessPiece nextSquare = board.getPiece(nextPosition);

            if (nextSquare == null) {
                addMove(startPosition, nextPosition, moves);
            } else if (nextSquare.getTeamColor() != pieceColor && isAttcking) {
                addMove(startPosition, nextPosition, moves);
            } else {
                return;
            }
        }

        private void addMove(ChessPosition oldPosition, ChessPosition newPosition, Collection<ChessMove> moves) {
            if (newPosition.getRow() == 1 || newPosition.getRow() == 8) {
                for (ChessPiece.PieceType promotionPiece : ChessPiece.PieceType.values()) {
                    if (promotionPiece != PieceType.PAWN && promotionPiece != PieceType.KING) {
                        moves.add(new ChessMove(oldPosition, newPosition, promotionPiece));
                    }
                }
            } else {
                moves.add(new ChessMove(oldPosition, newPosition, null));
            }
        }

    }

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
        //If you need intialized pieces
        //return String.format("[%s%s]", pieceColor.toString().charAt(0), type.toString().charAt(0));
        return String.format("[%s %s]", pieceColor, type);
    }

    public ChessPiece deepCopy() {
        return new ChessPiece(this.pieceColor, this.type);
    }
}
