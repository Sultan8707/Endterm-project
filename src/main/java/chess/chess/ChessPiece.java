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
}