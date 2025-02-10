package chess.chess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";

    public static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, "postgres", "postgres");
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS move_history (" +
                             "id SERIAL PRIMARY KEY, " +
                             "piece TEXT, " +
                             "row INTEGER, " +
                             "col INTEGER, " +
                             "to_row INTEGER, " +
                             "to_col INTEGER)")
        ) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveMove(String piece, int fromRow, int fromCol, int toRow, int toCol) {
        String query = "INSERT INTO move_history (piece, row, col, to_row, to_col) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, "postgres", "postgres");
             PreparedStatement statement = connection.prepareStatement(query)) {

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
}