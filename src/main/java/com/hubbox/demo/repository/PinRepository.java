package com.hubbox.demo.repository;

import com.hubbox.demo.entities.PinEntity;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Singleton
public class PinRepository extends BaseCrudRepository<PinEntity, Long> {

    @Inject
    public PinRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getTableName() {
        return "pin_table";
    }

    @Override
    protected PinEntity mapToEntity(ResultSet rs) throws SQLException {
        PinEntity pin = new PinEntity();
        pin.setId(rs.getLong("id"));
        pin.setPinNumber(rs.getString("pin_number"));
        return pin;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, PinEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, PinEntity entity) throws SQLException {
        addParameters(stmt, entity);
    }

    @Override
    protected String getInsertColumns() {
        return " (pin_number)";
    }

    @Override
    protected String getInsertPlaceholders() {
        return " (?)";
    }

    @Override
    protected String getUpdateColumns() {
        return " pin_number = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 1;
    }

    private void addParameters(PreparedStatement stmt, PinEntity entity) throws SQLException {
        stmt.setString(1, entity.getPinNumber());
    }

    public Optional<PinEntity> findByPinNumber(String pinNumber) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE pin_number = ?";
        try (PreparedStatement stmt = dataSource.getConnection().prepareStatement(sql)) {
            stmt.setString(1, pinNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapToEntity(rs));
            }
            return Optional.empty();
        }
    }
}
