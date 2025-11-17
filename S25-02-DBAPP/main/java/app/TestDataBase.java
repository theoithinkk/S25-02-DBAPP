package app.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Quick test to verify database connection and data
 * Run this as a standalone Java application to test
 */
public class TestDataBase {

    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===");

        try {
            // Test 1: Connection
            Connection conn = DBConnection.getConnection();
            System.out.println("✓ Database connected successfully!");

            // Test 2: Query residents table
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM Residents");

            if (rs.next()) {
                int count = rs.getInt("total");
                System.out.println("✓ Residents table accessible!");
                System.out.println("  Total residents in database: " + count);
            }

            // Test 3: Get actual data
            rs = stmt.executeQuery("SELECT * FROM Residents LIMIT 5");
            System.out.println("\n=== Sample Data ===");

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                System.out.println("Resident #" + rowCount + ": " +
                        rs.getString("first_name") + " " +
                        rs.getString("last_name"));
            }

            if (rowCount == 0) {
                System.out.println("⚠ WARNING: Residents table is EMPTY!");
                System.out.println("  You need to add some residents first.");
            }

            rs.close();
            stmt.close();

            System.out.println("\n✓ All tests passed!");

        } catch (Exception e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}