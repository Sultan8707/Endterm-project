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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChessGameGUI extends Application {

    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;
    private static final String[][] board = new String[BOARD_SIZE][BOARD_SIZE];

    // Map to store piece images
    private static final Map<String, Image> pieceImages = new HashMap<>();

    private int selectedRow = -1;
    private int selectedCol = -1;

    public ChessGameGUI() {
        // Default constructor required by JavaFX
    }

    public static void main(String[] args) {
        loadPieceImages();
        initializeBoard();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Create a tile
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.BROWN);

                // Create a piece if present
                StackPane stack = new StackPane(tile);
                String piece = board[row][col];
                if (!piece.isEmpty()) {
                    ImageView pieceImage = new ImageView(pieceImages.get(piece));
                    pieceImage.setFitWidth(TILE_SIZE - 10);
                    pieceImage.setFitHeight(TILE_SIZE - 10);
                    stack.getChildren().add(pieceImage);
                }

                // Add mouse click handler
                int finalRow = row;
                int finalCol = col;
                stack.setOnMouseClicked(event -> handleMouseClick(finalRow, finalCol, grid));

                grid.add(stack, col, row);
            }
        }

        Scene scene = new Scene(grid, TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        primaryStage.setTitle("Chess Game with Capture");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleMouseClick(int row, int col, GridPane grid) {
        if (selectedRow == -1 && selectedCol == -1) {
            // First click: select a piece
            if (!board[row][col].isEmpty()) {
                selectedRow = row;
                selectedCol = col;
                highlightTile(grid, row, col, Color.YELLOW);
            }
        } else {
            // Second click: attempt to move the piece
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                String targetPiece = board[row][col];
                String movingPiece = board[selectedRow][selectedCol];

                // Check for king capture
                if (targetPiece.equals("k")) {
                    System.out.println("White wins!");
                    System.exit(0);
                } else if (targetPiece.equals("K")) {
                    System.out.println("Black wins!");
                    System.exit(0);
                }

                // Move or capture
                board[row][col] = movingPiece;
                board[selectedRow][selectedCol] = "";
            }

            selectedRow = -1;
            selectedCol = -1;

            // Refresh the board display
            refreshBoard(grid);
        }
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        Set<String> validMoves = getValidMoves(fromRow, fromCol, piece);
        return validMoves.contains(toRow + "," + toCol);
    }

    private Set<String> getValidMoves(int row, int col, String piece) {
        Set<String> moves = new HashSet<>();
        boolean isWhite = Character.isUpperCase(piece.charAt(0));
        switch (piece.toLowerCase()) {
            case "p": // Pawn
                int direction = isWhite ? -1 : 1; // White moves up, Black moves down
                // Move forward
                if (isInBounds(row + direction, col) && board[row + direction][col].isEmpty()) {
                    moves.add((row + direction) + "," + col);
                }
                // Capture diagonally
                for (int dc : new int[]{-1, 1}) {
                    if (isInBounds(row + direction, col + dc)) {
                        String target = board[row + direction][col + dc];
                        if (!target.isEmpty() && Character.isUpperCase(target.charAt(0)) != isWhite) {
                            moves.add((row + direction) + "," + (col + dc));
                        }
                    }
                }
                break;
            case "r": // Rook
                addLinearMoves(moves, row, col, isWhite);
                break;
            case "n": // Knight
                addKnightMoves(moves, row, col, isWhite);
                break;
            case "b": // Bishop
                addDiagonalMoves(moves, row, col, isWhite);
                break;
            case "q": // Queen
                addLinearMoves(moves, row, col, isWhite);
                addDiagonalMoves(moves, row, col, isWhite);
                break;
            case "k": // King
                addKingMoves(moves, row, col, isWhite);
                break;
        }
        return moves;
    }

    private void addLinearMoves(Set<String> moves, int row, int col, boolean isWhite) {
        // Vertical and horizontal moves
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
        // Diagonal moves
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
        // Knight moves
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
        // King moves
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
        // Load images into the map
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
        // Black pieces
        String[] blackPieces = {"r", "n", "b", "q", "k", "b", "n", "r"};
        String[] whitePieces = {"R", "N", "B", "Q", "K", "B", "N", "R"};

        // Set pieces
        board[0] = blackPieces;
        board[1] = fillRow("p"); // Black pawns
        board[6] = fillRow("P"); // White pawns
        board[7] = whitePieces;

        // Empty cells
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