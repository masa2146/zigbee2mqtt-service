package com.hubbox.demo.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.RuleCondition;
import com.hubbox.demo.entities.DeviceRuleEntity;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Singleton
public class DeviceRuleRepository extends BaseCrudRepository<DeviceRuleEntity, Long> {

    private final ObjectMapper objectMapper;

    @Inject
    public DeviceRuleRepository(DataSource dataSource, ObjectMapper objectMapper) {
        super(dataSource);
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getTableName() {
        return "device_rules";
    }

    @Override
    protected DeviceRuleEntity mapToEntity(ResultSet rs) throws SQLException {
        try {
            DeviceRuleEntity rule = new DeviceRuleEntity();
            rule.setId(rs.getLong("id"));
            rule.setName(rs.getString("name"));
            rule.setDescription(rs.getString("description"));
            rule.setCondition(objectMapper.readValue(rs.getString("condition_json"), RuleCondition.class));
            rule.setAction(objectMapper.readValue(rs.getString("action_json"), RuleAction.class));
            return rule;
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON", e);
        }
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, DeviceRuleEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, DeviceRuleEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected String getInsertColumns() {
        return " (name, description, condition_json, action_json)";
    }

    @Override
    protected String getInsertPlaceholders() {
        return " (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateColumns() {
        return " name = ?, description = ?, condition_json = ?, action_json = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 4;
    }

    private void addParameters(PreparedStatement stmt, DeviceRuleEntity entity) throws SQLException {
        try {
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getDescription());
            stmt.setString(3, objectMapper.writeValueAsString(entity.getCondition()));
            stmt.setString(4, objectMapper.writeValueAsString(entity.getAction()));
        } catch (JsonProcessingException e) {
            throw new SQLException("Error serializing JSON", e);
        }
    }
}
