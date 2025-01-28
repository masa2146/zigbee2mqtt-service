package com.hubbox.demo.repository;

import com.hubbox.demo.entities.DeviceCategoryEntity;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Singleton
public class DeviceCategoryRepository extends BaseCrudRepository<DeviceCategoryEntity, Long> {

    @Inject
    public DeviceCategoryRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getTableName() {
        return "device_categories";
    }

    @Override
    protected DeviceCategoryEntity mapToEntity(ResultSet rs) throws SQLException {
        DeviceCategoryEntity category = new DeviceCategoryEntity();
        category.setId(rs.getLong("id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));
        return category;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, DeviceCategoryEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, DeviceCategoryEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected String getInsertColumns() {
        return " (category_name, description)";
    }

    @Override
    protected String getInsertPlaceholders() {
        return " (?, ?)";
    }

    @Override
    protected String getUpdateColumns() {
        return " category_name = ?, description = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 2;
    }

    private void addParameters(PreparedStatement stmt, DeviceCategoryEntity entity) throws SQLException {
        stmt.setString(1, entity.getCategoryName());
        stmt.setString(2, entity.getDescription());
    }
}
