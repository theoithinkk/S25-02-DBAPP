package app.dao;

import app.model.ClinicInventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClinicInventoryDAO {

    public boolean addItem(ClinicInventory item) {
        String sql = """
            INSERT INTO clinicinventory (item_name, category, quantity, expiration_date)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());

            if (item.getExpirationDate() != null)
                ps.setDate(4, new java.sql.Date(item.getExpirationDate().getTime()));
            else
                ps.setNull(4, Types.DATE);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateItem(ClinicInventory item) {
        String sql = """
            UPDATE clinicinventory
            SET item_name=?, category=?, quantity=?, expiration_date=?
            WHERE item_id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());

            if (item.getExpirationDate() != null)
                ps.setDate(4, new java.sql.Date(item.getExpirationDate().getTime()));
            else
                ps.setNull(4, Types.DATE);

            ps.setInt(5, item.getItemId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM clinicinventory WHERE item_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ClinicInventory> getAllItems() {
        List<ClinicInventory> list = new ArrayList<>();

        String sql = """
            SELECT item_id, item_name, category, quantity, expiration_date
            FROM clinicinventory
            ORDER BY item_name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                java.util.Date exp = null;
                java.sql.Date sqlDate = rs.getDate("expiration_date");
                if (sqlDate != null) exp = new java.util.Date(sqlDate.getTime());

                list.add(new ClinicInventory(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        exp
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean deductQuantity(int itemId, int quantityToDeduct) {
        String sql = """
        UPDATE clinicinventory
        SET quantity = quantity - ?
        WHERE item_id = ? AND quantity >= ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantityToDeduct);
            ps.setInt(2, itemId);
            ps.setInt(3, quantityToDeduct);

            // returns >0 only if quantity was enough AND update succeeded
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ClinicInventory> searchItemsByName(String name) {
        List<ClinicInventory> list = new ArrayList<>();

        String sql = """
        SELECT item_id, item_name, category, quantity, expiration_date
        FROM clinicinventory
        WHERE item_name LIKE ?
        ORDER BY item_name
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                java.util.Date exp = null;
                java.sql.Date sqlDate = rs.getDate("expiration_date");
                if (sqlDate != null) exp = new java.util.Date(sqlDate.getTime());

                list.add(new ClinicInventory(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        exp
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean addQuantity(int itemId, int quantityToAdd) {
        String sql = """
        UPDATE clinicinventory
        SET quantity = quantity + ?
        WHERE item_id = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantityToAdd);
            ps.setInt(2, itemId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
