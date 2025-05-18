package spacewar;

import java.sql.*;

public class DatabaseConnection {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "spacewar";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection connection;
    
    public DatabaseConnection() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // First try to connect without database
            String baseURL = "jdbc:mysql://" + HOST + ":" + PORT;
            connection = DriverManager.getConnection(baseURL, USER, PASSWORD);
            
            // Create database if it doesn't exist
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE);
            stmt.close();
            
            // Close connection and reconnect to the specific database
            connection.close();
            connection = DriverManager.getConnection(baseURL + "/" + DATABASE, USER, PASSWORD);
            
            // Create tables
            createTables();
            
            System.out.println("Database connection successful!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Please check if the driver is in the classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed. Please check if MySQL server is running.");
            System.err.println("Error: " + e.getMessage());
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
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addHighScore(String playerName, int score) {
        if (connection == null) {
            System.err.println("Cannot add high score: No database connection");
            return;
        }
        
        try {
            String query = "INSERT INTO high_scores (player_name, score) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, playerName);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error adding high score: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public ResultSet getHighScores() {
        if (connection == null) {
            System.err.println("Cannot get high scores: No database connection");
            return null;
        }
        
        try {
            String query = "SELECT player_name, score, date FROM high_scores ORDER BY score DESC LIMIT 10";
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.println("Error getting high scores: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
