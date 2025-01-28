package com.hubbox.demo.repository;

import com.hubbox.demo.entities.DeviceCommandEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Singleton
public class DeviceCommandRepository extends BaseCrudRepository<DeviceCommandEntity, Long> {

    @Inject
    public DeviceCommandRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getTableName() {
        return "device_commands";
    }

    @Override
    protected DeviceCommandEntity mapToEntity(ResultSet rs) throws SQLException {
        DeviceCommandEntity command = new DeviceCommandEntity();
        command.setId(rs.getLong("id"));
        command.setModelId(rs.getString("model_id"));
        command.setCommandName(rs.getString("command_name"));
        command.setCommandTemplate(rs.getString("command_template"));
        command.setDescription(rs.getString("description"));
        return command;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, DeviceCommandEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, DeviceCommandEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected String getInsertColumns() {
        return " (model_id, command_name, command_template, description)";
    }

    @Override
    protected String getInsertPlaceholders() {
        return " (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateColumns() {
        return " model_id = ?, command_name = ?, command_template = ?, description = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 4;
    }

    public List<DeviceCommandEntity> findByModelId(String modelId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE model_id = ?";
        List<DeviceCommandEntity> commands = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, modelId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                commands.add(mapToEntity(rs));
            }
        }

        return commands;
    }

    private void addParameters(PreparedStatement stmt, DeviceCommandEntity entity) throws SQLException {
        stmt.setString(1, entity.getModelId());
        stmt.setString(2, entity.getCommandName());
        stmt.setString(3, entity.getCommandTemplate());
        stmt.setString(4, entity.getDescription());
    }
}
