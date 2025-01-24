package com.example.demo1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChessGameSimplified extends Application {

    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;
    private static final String[][] board = new String[BOARD_SIZE][BOARD_SIZE];

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";

    private String selectedPiece = null;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public static void main(String[] args) {
        initializeBoard();
        initializeDatabase();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        refreshBoard(grid);

        Scene scene = new Scene(grid, TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        primaryStage.setTitle("Simplified Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshBoard(GridPane grid) {
        grid.getChildren().clear();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.BROWN);

                StackPane stack = new StackPane(tile);
                String piece = board[row][col];
                if (!piece.isEmpty()) {
                    Text pieceText = new Text(piece);
                    pieceText.setFill(Color.BLACK);
                    pieceText.setStyle("-fx-opacity: 0.7; -fx-font-size: 24px;");
                    stack.getChildren().add(pieceText);
                }

                int finalRow = row;
                int finalCol = col;
                stack.setOnMouseClicked(event -> {
                    if (selectedPiece == null) {
                        // Select a piece if present
                        if (!board[finalRow][finalCol].isEmpty()) {
                            selectedPiece = board[finalRow][finalCol];
                            selectedRow = finalRow;
                            selectedCol = finalCol;
                        }
                    } else {
                        // Move the selected piece
                        board[selectedRow][selectedCol] = "";
                        board[finalRow][finalCol] = selectedPiece;
                        saveMoveToDatabase(selectedPiece, selectedRow, selectedCol, finalRow, finalCol);
                        selectedPiece = null;
                        selectedRow = -1;
                        selectedCol = -1;
                    }
                    refreshBoard(grid);
                });

                grid.add(stack, col, row);
            }
        }
    }

    private void saveMoveToDatabase(String piece, int fromRow, int fromCol, int toRow, int toCol) {
        try (Connection connection = DriverManager.getConnection(DB_URL, "postgres", "postgres");
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO move_history (piece, \"row\", col, to_row, to_col) VALUES (?, ?, ?, ?, ?)")
        ) {
            statement.setString(1, piece);
            statement.setInt(2, fromRow);
            statement.setInt(3, fromCol);
            statement.setInt(4, toRow);
            statement.setInt(5, toCol);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, "postgres", "postgres");
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS move_history (" +
                             "id SERIAL PRIMARY KEY, " +
                             "piece TEXT, " + // Добавлен столбец piece
                             "\"row\" INTEGER, " +
                             "col INTEGER, " +
                             "to_row INTEGER, " +
                             "to_col INTEGER)"
             )
        ) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeBoard() {
        String[] blackPieces = {"r", "n", "b", "q", "k", "b", "n", "r"};
        String[] whitePieces = {"R", "N", "B", "Q", "K", "B", "N", "R"};

        board[0] = blackPieces;
        board[1] = fillRow("p");
        board[6] = fillRow("P");
        board[7] = whitePieces;

        for (int i = 2; i < 6; i++) {
            board[i] = fillRow("");
        }
    }

    private static String[] fillRow(String piece) {
        String[] row = new String[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            row[i] = piece;
        }
        return row;
    }
}
