package chess.chess;

public class ChessPiece {
    private final String type;  // Тип фигуры (P, R, N, B, Q, K)
    private final boolean isWhite; // Цвет фигуры (true - белая, false - черная)

    public ChessPiece(String type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
    }

    public String getType() {
        return type;
    }
}