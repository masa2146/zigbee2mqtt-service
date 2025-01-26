package com.hubbox.demo.repository;

import com.hubbox.demo.config.DatabaseConnectionManager;
import com.hubbox.demo.entities.DeviceModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceModelRepository {
    private static final String TABLE_NAME = "device_models";

    public Long create(DeviceModel model) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (category_id, model_id, vendor, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, model.getCategoryId());
            stmt.setString(2, model.getModelId());
            stmt.setString(3, model.getVendor());
            stmt.setString(4, model.getDescription());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                conn.commit();
                return id;
            }

            throw new SQLException("Failed to create model, no ID obtained.");
        }
    }

    public Optional<DeviceModel> findByModelId(String modelId) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE model_id = ?";

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, modelId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToModel(rs));
            }

            return Optional.empty();
        }
    }

    private DeviceModel mapToModel(ResultSet rs) throws SQLException {
        return DeviceModel.builder()
            .id(rs.getLong("id"))
            .categoryId(rs.getLong("category_id"))
            .modelId(rs.getString("model_id"))
            .vendor(rs.getString("vendor"))
            .description(rs.getString("description"))
            .build();
    }
}
