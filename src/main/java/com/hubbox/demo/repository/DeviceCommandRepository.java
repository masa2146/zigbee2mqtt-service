package com.hubbox.demo.repository;

import com.google.inject.Singleton;
import com.hubbox.demo.config.DatabaseConnectionManager;
import com.hubbox.demo.entities.DeviceCommandEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeviceCommandRepository {
    private static final String TABLE_NAME = "device_commands";

    public Long create(DeviceCommandEntity command) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (model_id, command_name, command_template, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, command.getModelId());
            stmt.setString(2, command.getCommandName());
            stmt.setString(3, command.getCommandTemplate());
            stmt.setString(4, command.getDescription());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                conn.commit();
                return id;
            }

            throw new SQLException("Failed to create command, no ID obtained.");
        }
    }

    public List<DeviceCommandEntity> findByModelId(String modelId) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE model_id = ?";
        List<DeviceCommandEntity> commands = new ArrayList<>();

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, modelId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                commands.add(mapToCommand(rs));
            }
        }

        return commands;
    }

    private DeviceCommandEntity mapToCommand(ResultSet rs) throws SQLException {
        DeviceCommandEntity command = new DeviceCommandEntity();
        command.setId(rs.getLong("id"));
        command.setModelId(rs.getString("model_id"));
        command.setCommandName(rs.getString("command_name"));
        command.setCommandTemplate(rs.getString("command_template"));
        command.setDescription(rs.getString("description"));
        return command;
    }
}
