package app.dao;

import app.model.RestockInventory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestockInventoryDAO {

    // Insert a new restock entry using a RestockInventory object
    public void insert(RestockInventory r) {
        String sql = "INSERT INTO restock_inventory (item_id, quantity_added, restocked_by, remarks) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, r.getItemId());
            stmt.setInt(2, r.getQuantityAdded());

            if (r.getRestockedBy() != null) {
                stmt.setInt(3, r.getRestockedBy());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setString(4, r.getRemarks());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Convenience method: directly log restock from controller
    public boolean insertRestockRecord(int itemId, int quantity, int restockedBy, String remarks) {
        RestockInventory r = new RestockInventory();
        r.setItemId(itemId);
        r.setQuantityAdded(quantity);
        r.setRestockedBy(restockedBy);
        r.setRemarks(remarks);

        try {
            insert(r);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Load all restock history
    public List<RestockInventory> getAll() {
        List<RestockInventory> list = new ArrayList<>();
        String sql = "SELECT * FROM restock_inventory ORDER BY restock_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RestockInventory r = new RestockInventory(
                        rs.getInt("restock_id"),
                        rs.getInt("item_id"),
                        rs.getInt("quantity_added"),
                        rs.getObject("restocked_by", Integer.class),
                        rs.getTimestamp("restock_date"),
                        rs.getString("remarks")
                );
                list.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}