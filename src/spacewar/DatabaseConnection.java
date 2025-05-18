package spacewar;

import java.sql.*;



public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/spacewar";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection connection;
    
    public DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        try {
            Statement stmt = connection.createStatement();
            
            // Create high scores table
            String createHighScoresTable = "CREATE TABLE IF NOT EXISTS high_scores (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "player_name VARCHAR(50) NOT NULL," +
                    "score INT NOT NULL," +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            
            stmt.execute(createHighScoresTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void addHighScore(String playerName, int score) {
        try {
            String query = "INSERT INTO high_scores (player_name, score) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, playerName);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet getHighScores() {
        try {
            String query = "SELECT player_name, score, date FROM high_scores ORDER BY score DESC LIMIT 10";
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 
