package com.hubbox.demo.repository;

import com.hubbox.demo.config.DatabaseConnectionManager;
import com.hubbox.demo.entities.DeviceCategory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceCategoryRepository {
    private static final String TABLE_NAME = "device_categories";

    public Long create(DeviceCategory category) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (category_name, description) VALUES (?, ?)";

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                conn.commit();
                return id;
            }

            throw new SQLException("Failed to create category, no ID obtained.");
        }
    }

    public Optional<DeviceCategory> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToCategory(rs));
            }

            return Optional.empty();
        }
    }

    public List<DeviceCategory> findAll() throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME;
        List<DeviceCategory> categories = new ArrayList<>();

        try (Connection conn = DatabaseConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(mapToCategory(rs));
            }
        }

        return categories;
    }

    public void update(Long id, DeviceCategory category) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET category_name = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setLong(3, id);

            stmt.executeUpdate();
            conn.commit();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
            conn.commit();
        }
    }

    private DeviceCategory mapToCategory(ResultSet rs) throws SQLException {
        return DeviceCategory.builder()
            .id(rs.getLong("id"))
            .categoryName(rs.getString("category_name"))
            .description(rs.getString("description"))
            .build();
    }
}
