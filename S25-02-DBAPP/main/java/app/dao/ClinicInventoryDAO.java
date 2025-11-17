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
}
