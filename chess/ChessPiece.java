package chess.chess;

public class ChessPiece {
    private final String type;
    private final boolean isWhite;

    public ChessPiece(String type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
    }

    public String getType() {
        return type;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] board) {
        if (toRow < 0 || toRow >= 8 || toCol < 0 || toCol >= 8) {
            return false;
        }

        ChessPiece targetPiece = board[toRow][toCol];
        if (targetPiece != null && targetPiece.isWhite() == this.isWhite) {
            return false;
        }

        switch (type) {
            case "P": case "p":
                return isValidPawnMove(fromRow, fromCol, toRow, toCol, board);
            case "R": case "r":
                return isStraightMove(fromRow, fromCol, toRow, toCol, board);
            case "N": case "n":
                return isKnightMove(fromRow, fromCol, toRow, toCol);
            case "B": case "b":
                return isDiagonalMove(fromRow, fromCol, toRow, toCol, board);
            case "Q": case "q":
                return isStraightMove(fromRow, fromCol, toRow, toCol, board) || isDiagonalMove(fromRow, fromCol, toRow, toCol, board);
            case "K": case "k":
                return isKingMove(fromRow, fromCol, toRow, toCol);
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] board) {
        int direction = isWhite ? -1 : 1;

        if (fromCol == toCol && board[toRow][toCol] == null) {
            if (toRow == fromRow + direction) return true;
            if ((fromRow == 1 && isWhite) || (fromRow == 6 && !isWhite))
                return toRow == fromRow + 2 * direction && board[fromRow + direction][toCol] == null;
        }

        if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction && board[toRow][toCol] != null) {
            return true;
        }

        return false;
    }

    private boolean isStraightMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] board) {
        if (fromRow == toRow) {
            int step = (toCol > fromCol) ? 1 : -1;
            for (int col = fromCol + step; col != toCol; col += step)
                if (board[fromRow][col] != null) return false;
            return true;
        }

        if (fromCol == toCol) {
            int step = (toRow > fromRow) ? 1 : -1;
            for (int row = fromRow + step; row != toRow; row += step)
                if (board[row][fromCol] != null) return false;
            return true;
        }

        return false;
    }

    private boolean isKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        return (Math.abs(fromRow - toRow) == 2 && Math.abs(fromCol - toCol) == 1) ||
                (Math.abs(fromRow - toRow) == 1 && Math.abs(fromCol - toCol) == 2);
    }

    private boolean isDiagonalMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] board) {
        if (Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
            int rowStep = (toRow > fromRow) ? 1 : -1;
            int colStep = (toCol > fromCol) ? 1 : -1;
            int row = fromRow + rowStep, col = fromCol + colStep;
            while (row != toRow && col != toCol) {
                if (board[row][col] != null) return false;
                row += rowStep;
                col += colStep;
            }
            return true;
        }
        return false;
    }

    private boolean isKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(fromRow - toRow) <= 1 && Math.abs(fromCol - toCol) <= 1;
    }
}