package chess.chess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;

    public static void main(String[] args) {
        GameBoard.initializeBoard();
        DatabaseManager.initializeDatabase();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        ChessGame game = new ChessGame(grid);
        game.refreshBoard();

        Scene scene = new Scene(grid, TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}