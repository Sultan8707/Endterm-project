package chess.chess;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.StackPane;

public class ChessGame {
    private static final int TILE_SIZE = 80;
    private final GridPane grid;
    private int selectedRow = -1;
    private int selectedCol = -1;

    private boolean isWhiteTurn = true;

    public ChessGame(GridPane grid) {
        this.grid = grid;
    }

    public void refreshBoard() {
        grid.getChildren().clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.BROWN);

                StackPane stack = new StackPane(tile);

                grid.add(stack, col, row);
            }
        }
    }
}