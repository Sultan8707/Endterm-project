package com.example.demo1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChessGameGUI extends Application {

    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;
    private static final String[][] board = new String[BOARD_SIZE][BOARD_SIZE];

    private static final Map<String, Image> pieceImages = new HashMap<>();
    private int selectedRow = -1;
    private int selectedCol = -1;

    private static final List<String> moveHistory = new ArrayList<>();

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";

    public ChessGameGUI() {
        // Default constructor required by JavaFX
    }

    public static void main(String[] args) {
        loadPieceImages();
        initializeBoard();
        initializeDatabase();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.BROWN);

                StackPane stack = new StackPane(tile);
                String piece = board[row][col];
                if (!piece.isEmpty()) {
                    ImageView pieceImage = new ImageView(pieceImages.get(piece));
                    pieceImage.setFitWidth(TILE_SIZE - 10);
                    pieceImage.setFitHeight(TILE_SIZE - 10);
                    stack.getChildren().add(pieceImage);
                }

                int finalRow = row;
                int finalCol = col;
                stack.setOnMouseClicked(event -> handleMouseClick(finalRow, finalCol, grid));

                grid.add(stack, col, row);
            }
        }

        Scene scene = new Scene(grid, TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        primaryStage.setTitle("Chess Game with Move History");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleMouseClick(int row, int col, GridPane grid) {
        if (selectedRow == -1 && selectedCol == -1) {
            if (!board[row][col].isEmpty()) {
                selectedRow = row;
                selectedCol = col;
                highlightTile(grid, row, col, Color.YELLOW);
            }
        } else {
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                String targetPiece = board[row][col];
                String movingPiece = board[selectedRow][selectedCol];

                if (targetPiece.equals("k")) {
                    System.out.println("White wins!");
                    System.exit(0);
                } else if (targetPiece.equals("K")) {
                    System.out.println("Black wins!");
                    System.exit(0);
                }

                board[row][col] = movingPiece;
                board[selectedRow][selectedCol] = "";

                String move = String.format("%s from (%d, %d) to (%d, %d)", movingPiece, selectedRow, selectedCol, row, col);
                moveHistory.add(move);
                saveMoveToDatabase(movingPiece, selectedRow, selectedCol, row, col);
            }

            selectedRow = -1;
            selectedCol = -1;
            refreshBoard(grid);
        }
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        Set<String> validMoves = getValidMoves(fromRow, fromCol, piece);
        return validMoves.contains(toRow + "," + toCol);
    }

    private void saveMoveToDatabase(String piece, int fromRow, int fromCol, int toRow, int toCol) {
        try (Connection connection = DriverManager.getConnection(DB_URL, "postgres", "postgres");
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO move_history (piece, from_row, from_col, to_row, to_col) VALUES (?, ?, ?, ?, ?)")
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
                             "piece TEXT, " +
                             "from_row INTEGER, " +
                             "from_col INTEGER, " +
                             "to_row INTEGER, " +
                             "to_col INTEGER" +
                             ")")
        ) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Set<String> getValidMoves(int row, int col, String piece) {
        Set<String> moves = new HashSet<>();
        boolean isWhite = Character.isUpperCase(piece.charAt(0));
        switch (piece.toLowerCase()) {
            case "p":
                int direction = isWhite ? -1 : 1;
                if (isInBounds(row + direction, col) && board[row + direction][col].isEmpty()) {
                    moves.add((row + direction) + "," + col);
                }
                for (int dc : new int[]{-1, 1}) {
                    if (isInBounds(row + direction, col + dc)) {
                        String target = board[row + direction][col + dc];
                        if (!target.isEmpty() && Character.isUpperCase(target.charAt(0)) != isWhite) {
                            moves.add((row + direction) + "," + (col + dc));
                        }
                    }
                }
                break;
            case "r":
                addLinearMoves(moves, row, col, isWhite);
                break;
            case "n":
                addKnightMoves(moves, row, col, isWhite);
                break;
            case "b":
                addDiagonalMoves(moves, row, col, isWhite);
                break;
            case "q":
                addLinearMoves(moves, row, col, isWhite);
                addDiagonalMoves(moves, row, col, isWhite);
                break;
            case "k":
                addKingMoves(moves, row, col, isWhite);
                break;
        }
        return moves;
    }

    private void addLinearMoves(Set<String> moves, int row, int col, boolean isWhite) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if ((i == 0 || j == 0) && (i != j)) {
                    int newRow = row + i;
                    int newCol = col + j;
                    while (isInBounds(newRow, newCol)) {
                        String target = board[newRow][newCol];
                        if (target.isEmpty()) {
                            moves.add(newRow + "," + newCol);
                        } else {
                            if (Character.isUpperCase(target.charAt(0)) != isWhite) {
                                moves.add(newRow + "," + newCol);
                            }
                            break;
                        }
                        newRow += i;
                        newCol += j;
                    }
                }
            }
        }
    }

    private void addDiagonalMoves(Set<String> moves, int row, int col, boolean isWhite) {
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int newRow = row + i;
                int newCol = col + j;
                while (isInBounds(newRow, newCol)) {
                    String target = board[newRow][newCol];
                    if (target.isEmpty()) {
                        moves.add(newRow + "," + newCol);
                    } else {
                        if (Character.isUpperCase(target.charAt(0)) != isWhite) {
                            moves.add(newRow + "," + newCol);
                        }
                        break;
                    }
                    newRow += i;
                    newCol += j;
                }
            }
        }
    }

    private void addKnightMoves(Set<String> moves, int row, int col, boolean isWhite) {
        int[][] deltas = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {-1, -2}, {1, -2}, {-1, 2}, {1, 2}};
        for (int[] delta : deltas) {
            int newRow = row + delta[0];
            int newCol = col + delta[1];
            if (isInBounds(newRow, newCol)) {
                String target = board[newRow][newCol];
                if (target.isEmpty() || Character.isUpperCase(target.charAt(0)) != isWhite) {
                    moves.add(newRow + "," + newCol);
                }
            }
        }
    }

    private void addKingMoves(Set<String> moves, int row, int col, boolean isWhite) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    int newRow = row + i;
                    int newCol = col + j;
                    if (isInBounds(newRow, newCol)) {
                        String target = board[newRow][newCol];
                        if (target.isEmpty() || Character.isUpperCase(target.charAt(0)) != isWhite) {
                            moves.add(newRow + "," + newCol);
                        }
                    }
                }
            }
        }
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void highlightTile(GridPane grid, int row, int col, Color color) {
        StackPane stack = (StackPane) getNodeFromGridPane(grid, col, row);
        if (stack != null) {
            Rectangle tile = (Rectangle) stack.getChildren().get(0);
            tile.setFill(color);
        }
    }

    private void refreshBoard(GridPane grid) {
        grid.getChildren().clear();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.BROWN);

                String piece = board[row][col];
                StackPane stack = new StackPane(tile);
                if (!piece.isEmpty()) {
                    ImageView pieceImage = new ImageView(pieceImages.get(piece));
                    pieceImage.setFitWidth(TILE_SIZE - 10);
                    pieceImage.setFitHeight(TILE_SIZE - 10);
                    stack.getChildren().add(pieceImage);
                }

                int finalRow = row;
                int finalCol = col;
                stack.setOnMouseClicked(event -> handleMouseClick(finalRow, finalCol, grid));

                grid.add(stack, col, row);
            }
        }
    }

    private Object getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private static void loadPieceImages() {
        pieceImages.put("p", new Image(ChessGameGUI.class.getResource("/images/Chess_pdt60.png").toExternalForm()));
        pieceImages.put("r", new Image(ChessGameGUI.class.getResource("/images/Chess_rdt60.png").toExternalForm()));
        pieceImages.put("n", new Image(ChessGameGUI.class.getResource("/images/Chess_ndt60.png").toExternalForm()));
        pieceImages.put("b", new Image(ChessGameGUI.class.getResource("/images/Chess_bdt60.png").toExternalForm()));
        pieceImages.put("q", new Image(ChessGameGUI.class.getResource("/images/Chess_qdt60.png").toExternalForm()));
        pieceImages.put("k", new Image(ChessGameGUI.class.getResource("/images/Chess_kdt60.png").toExternalForm()));

        pieceImages.put("P", new Image(ChessGameGUI.class.getResource("/images/Chess_plt60.png").toExternalForm()));
        pieceImages.put("R", new Image(ChessGameGUI.class.getResource("/images/Chess_rlt60.png").toExternalForm()));
        pieceImages.put("N", new Image(ChessGameGUI.class.getResource("/images/Chess_nlt60.png").toExternalForm()));
        pieceImages.put("B", new Image(ChessGameGUI.class.getResource("/images/Chess_blt60.png").toExternalForm()));
        pieceImages.put("Q", new Image(ChessGameGUI.class.getResource("/images/Chess_qlt60.png").toExternalForm()));
        pieceImages.put("K", new Image(ChessGameGUI.class.getResource("/images/Chess_klt60.png").toExternalForm()));
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