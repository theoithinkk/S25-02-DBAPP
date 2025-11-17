package app.dao;

import app.model.ClinicInventory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for ClinicInventory table
 */
public class ClinicInventoryDAO {

    /** CREATE **/
    public boolean addItem(ClinicInventory item) {
        String sql = "INSERT INTO ClinicInventory (item_name, category, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** READ **/
    public List<ClinicInventory> getAllItems() {
        List<ClinicInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM ClinicInventory";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ClinicInventory item = new ClinicInventory();
                item.setItemId(rs.getInt("item_id"));
                item.setItemName(rs.getString("item_name"));
                item.setCategory(rs.getString("category"));
                item.setQuantity(rs.getInt("quantity"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** UPDATE **/
    public boolean updateItem(ClinicInventory item) {
        String sql = "UPDATE ClinicInventory SET item_name=?, category=?, quantity=? WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());
            ps.setInt(4, item.getItemId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** DELETE **/
    public boolean deleteItem(int id) {
        String sql = "DELETE FROM ClinicInventory WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**For logging restock transactions**/
    public void insertRestockRecord(int itemId, int quantityAdded, Integer restockedBy, String remarks) {
        String sql = "INSERT INTO restock_inventory (item_id, quantity_added, restocked_by, remarks) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            stmt.setInt(2, quantityAdded);

            if (restockedBy != null) stmt.setInt(3, restockedBy);
            else stmt.setNull(3, java.sql.Types.INTEGER);

            stmt.setString(4, remarks);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**For updating stock transactions**/
    public void updateStock(int itemId, int quantityToAdd) {
        String sql = "UPDATE clinicinventory SET quantity = quantity + ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantityToAdd);
            stmt.setInt(2, itemId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** For the search-bar**/
    public List<ClinicInventory> searchItemsByName(String name) {
        List<ClinicInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM clinicinventory WHERE item_name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ClinicInventory item = new ClinicInventory(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getInt("quantity")
                );
                list.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}