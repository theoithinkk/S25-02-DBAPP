package app.dao;

import app.model.ClinicInventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClinicInventoryDAO {

    // Get all items from inventory
    public List<ClinicInventory> getAllItems() {
        List<ClinicInventory> items = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory";

        System.out.println("DAO DEBUG: Executing SQL: " + sql);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("DAO DEBUG: Connection successful");
            int count = 0;

            while (rs.next()) {
                ClinicInventory item = mapResultSetToClinicInventory(rs);
                items.add(item);
                count++;
                System.out.println("DAO DEBUG: Loaded - ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("item_name") +
                        ", Category: " + rs.getString("category") +
                        ", Qty: " + rs.getInt("quantity"));
            }

            System.out.println("DAO DEBUG: Total items loaded from DB: " + count);

        } catch (SQLException e) {
            System.err.println("DAO ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    // Get item by ID
    public ClinicInventory getItemById(int itemId) {
        String sql = "SELECT * FROM clinicinventory WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToClinicInventory(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Add new item to inventory - UPDATED TO INCLUDE CATEGORY
    public boolean addItem(ClinicInventory item) {
        String sql = "INSERT INTO clinicinventory (item_name, category, quantity, expiration_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory()); // ADDED CATEGORY
            pstmt.setInt(3, item.getQuantity());
            if (item.getExpirationDate() != null) {
                pstmt.setDate(4, new java.sql.Date(item.getExpirationDate().getTime()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update existing item - UPDATED TO INCLUDE CATEGORY
    public boolean updateItem(ClinicInventory item) {
        String sql = "UPDATE clinicinventory SET item_name = ?, category = ?, quantity = ?, expiration_date = ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory()); // ADDED CATEGORY
            pstmt.setInt(3, item.getQuantity());
            if (item.getExpirationDate() != null) {
                pstmt.setDate(4, new java.sql.Date(item.getExpirationDate().getTime()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.setInt(5, item.getItemId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete item from inventory
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM clinicinventory WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // DEDUCT QUANTITY METHOD
    public boolean deductQuantity(int itemId, int quantityToDeduct) {
        String sql = "UPDATE clinicinventory SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantityToDeduct);
            pstmt.setInt(2, itemId);
            pstmt.setInt(3, quantityToDeduct);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add quantity to inventory
    public boolean addQuantity(int itemId, int quantityToAdd) {
        String sql = "UPDATE clinicinventory SET quantity = quantity + ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantityToAdd);
            pstmt.setInt(2, itemId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get low stock items (less than or equal to threshold)
    public List<ClinicInventory> getLowStockItems(int threshold) {
        List<ClinicInventory> items = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory WHERE quantity <= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, threshold);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ClinicInventory item = mapResultSetToClinicInventory(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Get expired items
    public List<ClinicInventory> getExpiredItems() {
        List<ClinicInventory> items = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory WHERE expiration_date < CURDATE()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ClinicInventory item = mapResultSetToClinicInventory(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Get items expiring soon (within next 30 days)
    public List<ClinicInventory> getExpiringSoonItems() {
        List<ClinicInventory> items = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory WHERE expiration_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ClinicInventory item = mapResultSetToClinicInventory(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Search items by name
    public List<ClinicInventory> searchItemsByName(String searchTerm) {
        List<ClinicInventory> items = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory WHERE item_name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ClinicInventory item = mapResultSetToClinicInventory(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // UPDATED: Helper method to map ResultSet to ClinicInventory object - INCLUDES CATEGORY
    private ClinicInventory mapResultSetToClinicInventory(ResultSet rs) throws SQLException {
        ClinicInventory item = new ClinicInventory();
        item.setItemId(rs.getInt("id"));
        item.setItemName(rs.getString("item_name"));
        item.setCategory(rs.getString("category")); // ADDED THIS LINE
        item.setQuantity(rs.getInt("quantity"));
        item.setExpirationDate(rs.getDate("expiration_date"));
        return item;
    }

    // Get total item count
    public int getTotalItemCount() {
        String sql = "SELECT COUNT(*) as count FROM clinicinventory";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get total quantity of all items
    public int getTotalQuantity() {
        String sql = "SELECT SUM(quantity) as total FROM clinicinventory";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean testConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("✅ Database connection successful!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}