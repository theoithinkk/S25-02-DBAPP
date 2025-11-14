package app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/barangayhealthdb";
    private static final String USER = "root";
    private static final String PASSWORD = "12345"; // ← Change this if needed

    /**
     * Gets a connection to the database
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create connection
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("✓ Database connected successfully!");
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL Driver not found!");
            System.err.println("   Make sure mysql-connector-java.jar is in your classpath");
            throw new SQLException("MySQL Driver not found", e);

        } catch (SQLException e) {
            System.err.println("✗ Failed to connect to database!");
            System.err.println("   URL: " + URL);
            System.err.println("   User: " + USER);
            System.err.println("   Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Test the connection
     */
    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===\n");

        try {
            Connection conn = getConnection();
            System.out.println("\n✓ Connection test successful!");
            System.out.println("  Database: " + conn.getCatalog());
            conn.close();

        } catch (SQLException e) {
            System.err.println("\n✗ Connection test FAILED!");
            System.err.println("\nPossible fixes:");
            System.err.println("1. Make sure MySQL server is running");
            System.err.println("2. Check username and password");
            System.err.println("3. Verify database 'barangayhealthdb' exists");
            System.err.println("4. Add MySQL connector JAR to project");
        }
    }
}