package com.hubbox.demo.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.entities.DeviceEntity;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

@Singleton
public class DeviceRepository extends BaseCrudRepository<DeviceEntity, Long> {

    @Inject
    public DeviceRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getTableName() {
        return "devices";
    }

    @Override
    protected DeviceEntity mapToEntity(ResultSet rs) throws SQLException {
        DeviceEntity device = new DeviceEntity();
        device.setId(rs.getLong("id"));
        device.setDisabled(rs.getBoolean("disabled"));
        device.setFriendlyName(rs.getString("friendly_name"));
        device.setModelId(rs.getString("model_id"));
        return device;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, DeviceEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, DeviceEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected String getInsertColumns() {
        return " (disabled, friendly_name, model_id)";
    }

    @Override
    protected String getInsertPlaceholders() {
        return " (?, ?, ?)";
    }

    @Override
    protected String getUpdateColumns() {
        return " disabled = ?, friendly_name = ?, model_id = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 3;
    }

    private void addParameters(PreparedStatement stmt, DeviceEntity entity) throws SQLException {
        stmt.setBoolean(1, entity.getDisabled());
        stmt.setString(2, entity.getFriendlyName());
        stmt.setString(3, entity.getModelId());
    }
}
