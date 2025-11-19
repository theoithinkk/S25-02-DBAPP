package app.dao;

import app.model.InventoryMovement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryMovementDAO {

    /**
     * Insert a new inventory movement transaction.
     */
    public void insert(InventoryMovement movement) throws SQLException {  // ← Add "throws SQLException"
        String sql = "INSERT INTO inventorymovement " +
                "(item_id, movement_type, quantity, actor_id, resident_id, movement_date, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movement.getItemId());
            stmt.setString(2, movement.getMovementType());
            stmt.setInt(3, movement.getQuantity());
            stmt.setInt(4, movement.getActorId());

            if (movement.getResidentId() != null) {
                stmt.setInt(5, movement.getResidentId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            if (movement.getMovementDate() != null) {
                stmt.setTimestamp(6, movement.getMovementDate());
            } else {
                stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }

            stmt.setString(7, movement.getRemarks());
            stmt.executeUpdate();

        }
    }

    /**
     * Convenience insert method for RESTOCK movements.
     */
    public boolean insertRestockMovement(int itemId, int quantity, int actorId, String remarks) {
        InventoryMovement movement = new InventoryMovement();
        movement.setItemId(itemId);
        movement.setMovementType("RESTOCK");
        movement.setQuantity(quantity);
        movement.setActorId(actorId);
        movement.setResidentId(null);
        movement.setMovementDate(new Timestamp(System.currentTimeMillis()));
        movement.setRemarks(remarks);

        try {
            insert(movement);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convenience insert method for ISSUE movements (requires resident).
     */
    public boolean insertIssueMovement(int itemId, int quantity, int actorId, int residentId, String remarks, Timestamp movementDate) {
        InventoryMovement movement = new InventoryMovement();
        movement.setItemId(itemId);
        movement.setMovementType("ISSUE");
        movement.setQuantity(quantity);
        movement.setActorId(actorId);
        movement.setResidentId(residentId);
        movement.setMovementDate(movementDate != null ? movementDate : new Timestamp(System.currentTimeMillis()));
        movement.setRemarks(remarks);

        try {
            insert(movement);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convenience insert method for SERVICE movements (requires resident).
     */
    public boolean insertServiceMovement(int itemId, int quantity, int actorId, int residentId, String remarks) {
        InventoryMovement movement = new InventoryMovement();
        movement.setItemId(itemId);
        movement.setMovementType("SERVICE");
        movement.setQuantity(quantity);
        movement.setActorId(actorId);
        movement.setResidentId(residentId);
        movement.setMovementDate(new Timestamp(System.currentTimeMillis()));
        movement.setRemarks(remarks);

        try {
            insert(movement);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load all inventory movements ordered by most recent.
     */
    public List<InventoryMovement> getAll() {
        List<InventoryMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM inventorymovement ORDER BY movement_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                InventoryMovement movement = new InventoryMovement(
                        rs.getInt("movement_id"),
                        rs.getInt("item_id"),
                        rs.getString("movement_type"),
                        rs.getInt("quantity"),
                        rs.getInt("actor_id"),
                        rs.getObject("resident_id", Integer.class),
                        rs.getTimestamp("movement_date"),
                        rs.getString("remarks")
                );
                list.add(movement);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get all movements for a specific item.
     */
    public List<InventoryMovement> getByItemId(int itemId) {
        List<InventoryMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM inventorymovement WHERE item_id = ? ORDER BY movement_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                InventoryMovement movement = new InventoryMovement(
                        rs.getInt("movement_id"),
                        rs.getInt("item_id"),
                        rs.getString("movement_type"),
                        rs.getInt("quantity"),
                        rs.getInt("actor_id"),
                        rs.getObject("resident_id", Integer.class),
                        rs.getTimestamp("movement_date"),
                        rs.getString("remarks")
                );
                list.add(movement);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get all movements by type (RESTOCK, ISSUE, SERVICE).
     */
    public List<InventoryMovement> getByMovementType(String movementType) {
        List<InventoryMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM inventorymovement WHERE movement_type = ? ORDER BY movement_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movementType);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                InventoryMovement movement = new InventoryMovement(
                        rs.getInt("movement_id"),
                        rs.getInt("item_id"),
                        rs.getString("movement_type"),
                        rs.getInt("quantity"),
                        rs.getInt("actor_id"),
                        rs.getObject("resident_id", Integer.class),
                        rs.getTimestamp("movement_date"),
                        rs.getString("remarks")
                );
                list.add(movement);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get movements by resident.
     */
    public List<InventoryMovement> getByResidentId(int residentId) {
        List<InventoryMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM inventorymovement WHERE resident_id = ? ORDER BY movement_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, residentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                InventoryMovement movement = new InventoryMovement(
                        rs.getInt("movement_id"),
                        rs.getInt("item_id"),
                        rs.getString("movement_type"),
                        rs.getInt("quantity"),
                        rs.getInt("actor_id"),
                        rs.getObject("resident_id", Integer.class),
                        rs.getTimestamp("movement_date"),
                        rs.getString("remarks")
                );
                list.add(movement);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Log user action to audit log
     */
    public void logAction(int userId, String action) {
        String sql = "INSERT INTO AuditLog (user_id, action) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Warning: Could not log action: " + e.getMessage());
        }
    }
}
