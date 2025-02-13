package chess.chess;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Map;

public class ChessGame {
    private static final int TILE_SIZE = 80;
    private final GridPane grid;
    private ChessPiece selectedPiece = null;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private StackPane highlightedTile = null;

    private boolean isWhiteTurn = true;

    private final Map<String, String> pieceImages = Map.ofEntries(
            Map.entry("P", "/images/pawn_white.png"),
            Map.entry("p", "/images/pawn_black.png"),
            Map.entry("R", "/images/rook_white.png"),
            Map.entry("r", "/images/rook_black.png"),
            Map.entry("N", "/images/knight_white.png"),
            Map.entry("n", "/images/knight_black.png"),
            Map.entry("B", "/images/bishop_white.png"),
            Map.entry("b", "/images/bishop_black.png"),
            Map.entry("Q", "/images/queen_white.png"),
            Map.entry("q", "/images/queen_black.png"),
            Map.entry("K", "/images/king_white.png"),
            Map.entry("k", "/images/king_black.png")
    );

    public ChessGame(GridPane grid) {
        this.grid = grid;
    }

    public void refreshBoard() {
        grid.getChildren().clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                if (row == selectedRow && col == selectedCol) {
                    tile.setFill(Color.YELLOW);
                } else {
                    tile.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.BROWN);
                }

                StackPane stack = new StackPane(tile);
                stack.setId("tile-" + row + "-" + col);
                ChessPiece piece = GameBoard.board[row][col];

                if (piece != null) {
                    String pieceSymbol = piece.getType();
                    if (pieceImages.containsKey(pieceSymbol)) {
                        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(pieceImages.get(pieceSymbol))));
                        imageView.setFitWidth(TILE_SIZE * 0.8);
                        imageView.setFitHeight(TILE_SIZE * 0.8);
                        stack.getChildren().add(imageView);
                    }
                }

                int finalRow = row;
                int finalCol = col;
                stack.setOnMouseClicked(event -> handleMove(finalRow, finalCol));

                grid.add(stack, col, row);
            }
        }
    }

    private void handleMove(int row, int col) {
        ChessPiece piece = GameBoard.board[row][col];

        if (selectedPiece == null) {
            if (piece != null && piece.isWhite() == isWhiteTurn) {
                selectedPiece = piece;
                selectedRow = row;
                selectedCol = col;
                refreshBoard();
            }
        } else {
            if (selectedPiece.isValidMove(selectedRow, selectedCol, row, col, GameBoard.board)) {
                GameBoard.board[row][col] = selectedPiece;
                GameBoard.board[selectedRow][selectedCol] = null;
                isWhiteTurn = !isWhiteTurn;
            }
            selectedPiece = null;
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
        }
    }
}