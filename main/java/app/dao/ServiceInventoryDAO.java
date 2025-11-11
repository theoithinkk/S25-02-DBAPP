package app.dao;

import app.model.ServiceInventory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for the ServiceInventory table.
 * Used for tracking which items were used for specific services.
 */
public class ServiceInventoryDAO {

    /**
     * Returns all service inventory records joined with item and service names.
     */
    public List<ServiceInventory> getAllServiceInventory() {
        List<ServiceInventory> inventoryList = new ArrayList<>();

        String query = """
            SELECT si.inventory_id, si.quantity, si.expiration,
                   ci.item_name, hs.service_type, st.date_provided
            FROM ServiceInventory si
            LEFT JOIN ClinicInventory ci ON si.item_id = ci.item_id
            LEFT JOIN HealthServices hs ON si.service_id = hs.service_id
            LEFT JOIN ServiceTransactions st ON si.transaction_id = st.transaction_id
            ORDER BY si.expiration ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ServiceInventory si = new ServiceInventory();
                si.setInventoryId(rs.getInt("inventory_id"));
                si.setQuantity(rs.getInt("quantity"));
                si.setExpiration(rs.getDate("expiration"));
                si.setItemName(rs.getString("item_name"));
                si.setServiceType(rs.getString("service_type"));
                si.setDateProvided(rs.getDate("date_provided"));
                inventoryList.add(si);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inventoryList;
    }

    /**
     * Adds a new inventory usage record linked to a service transaction.
     */
    public boolean addServiceInventory(ServiceInventory si) {
        String query = """
            INSERT INTO ServiceInventory (item_id, service_id, transaction_id, quantity, expiration)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, si.getItemId());
            ps.setInt(2, si.getServiceId());
            ps.setInt(3, si.getTransactionId());
            ps.setInt(4, si.getQuantity());
            ps.setDate(5, si.getExpiration());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
