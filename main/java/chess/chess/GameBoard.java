package chess.chess;

public class GameBoard {
    private static final int BOARD_SIZE = 8;
    public static ChessPiece[][] board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];

    public static void initializeBoard() {
        board[0] = new ChessPiece[] {
                new ChessPiece("r", false), new ChessPiece("n", false), new ChessPiece("b", false),
                new ChessPiece("q", false), new ChessPiece("k", false), new ChessPiece("b", false),
                new ChessPiece("n", false), new ChessPiece("r", false)
        };
        board[1] = fillRow("p", false);
        board[6] = fillRow("P", true);
        board[7] = new ChessPiece[] {
                new ChessPiece("R", true), new ChessPiece("N", true), new ChessPiece("B", true),
                new ChessPiece("Q", true), new ChessPiece("K", true), new ChessPiece("B", true),
                new ChessPiece("N", true), new ChessPiece("R", true)
        };

        for (int i = 2; i < 6; i++) {
            board[i] = new ChessPiece[8]; // Пустые клетки
        }
    }

    private static ChessPiece[] fillRow(String type, boolean isWhite) {
        ChessPiece[] row = new ChessPiece[8];
        for (int i = 0; i < 8; i++) {
            row[i] = new ChessPiece(type, isWhite);
        }
        return row;
    }
}