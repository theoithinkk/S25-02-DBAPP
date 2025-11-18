package app.dao;

import app.model.ClinicInventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for ClinicInventory table
 */
public class ClinicInventoryDAO {

    /** -----------------------------------------
     * CREATE
     * ------------------------------------------ */
    public boolean addItem(ClinicInventory item) {
        String sql = "INSERT INTO clinicinventory (item_name, category, quantity, expiration_date) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());

            if (item.getExpirationDate() != null) {
                ps.setDate(4, new java.sql.Date(item.getExpirationDate().getTime()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** -----------------------------------------
     * READ - GET ALL
     * ------------------------------------------ */
    public List<ClinicInventory> getAllItems() {
        List<ClinicInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** -----------------------------------------
     * READ - GET BY ID
     * ------------------------------------------ */
    public ClinicInventory getItemById(int id) {
        String sql = "SELECT * FROM clinicinventory WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** -----------------------------------------
     * UPDATE
     * ------------------------------------------ */
    public boolean updateItem(ClinicInventory item) {
        String sql = "UPDATE clinicinventory SET item_name=?, category=?, quantity=?, expiration_date=? " +
                "WHERE item_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());

            if (item.getExpirationDate() != null) {
                ps.setDate(4, new java.sql.Date(item.getExpirationDate().getTime()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setInt(5, item.getItemId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** -----------------------------------------
     * DELETE
     * ------------------------------------------ */
    public boolean deleteItem(int id) {
        String sql = "DELETE FROM clinicinventory WHERE item_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** -----------------------------------------
     * STOCK ADJUSTMENTS
     * ------------------------------------------ */

    // Decrease quantity (checking for sufficient stock)
    public boolean deductQuantity(int itemId, int amount) {
        String sql = "UPDATE clinicinventory SET quantity = quantity - ? WHERE item_id = ? AND quantity >= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, amount);
            ps.setInt(2, itemId);
            ps.setInt(3, amount);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Increase quantity
    public boolean addQuantity(int itemId, int amount) {
        String sql = "UPDATE clinicinventory SET quantity = quantity + ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, amount);
            ps.setInt(2, itemId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** -----------------------------------------
     * SPECIAL QUERIES
     * ------------------------------------------ */

    // Low stock
    public List<ClinicInventory> getLowStockItems(int threshold) {
        List<ClinicInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory WHERE quantity <= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, threshold);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Expired
    public List<ClinicInventory> getExpiredItems() {
        List<ClinicInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory WHERE expiration_date < CURDATE()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Expiring soon (30 days)
    public List<ClinicInventory> getExpiringSoonItems() {
        List<ClinicInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory " +
                "WHERE expiration_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Search by name
    public List<ClinicInventory> searchItemsByName(String name) {
        List<ClinicInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory WHERE item_name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** -----------------------------------------
     * Helper: Convert ResultSet → Model
     * ------------------------------------------ */
    private ClinicInventory map(ResultSet rs) throws SQLException {
        ClinicInventory item = new ClinicInventory();
        item.setItemId(rs.getInt("item_id"));
        item.setItemName(rs.getString("item_name"));
        item.setCategory(rs.getString("category"));
        item.setQuantity(rs.getInt("quantity"));
        item.setExpirationDate(rs.getDate("expiration_date"));
        return item;
    }

    /** -----------------------------------------
     * Metrics
     * ------------------------------------------ */
    public int getTotalItemCount() {
        String sql = "SELECT COUNT(*) FROM clinicinventory";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getTotalQuantity() {
        String sql = "SELECT SUM(quantity) FROM clinicinventory";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
