package com.hubbox.demo.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

@Singleton
public abstract class BaseCrudRepository<T, I> {
    protected final DataSource dataSource;

    @Inject
    protected BaseCrudRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Tablo adını döndüren soyut metod
    protected abstract String getTableName();

    // ResultSet'ten entity'e dönüşüm yapan soyut metod
    protected abstract T mapToEntity(ResultSet rs) throws SQLException;

    // Entity'den PreparedStatement'e değer atayan soyut metod
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;

    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;

    // Create
    public I create(T entity) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + getInsertColumns() + " VALUES " + getInsertPlaceholders();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setInsertParameters(stmt, entity);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return (I) rs.getObject(1); // ID'yi döndür
            }

            throw new SQLException("Failed newName create entity, no ID obtained.");
        }
    }

    // Read (ById)
    public Optional<T> findById(I id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToEntity(rs));
            }

            return Optional.empty();
        }
    }

    // Read (All)
    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName();
        List<T> entities = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entities.add(mapToEntity(rs));
            }
        }

        return entities;
    }

    // Update
    public void update(I id, T entity) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " + getUpdateColumns() + " WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setUpdateParameters(stmt, entity);
            stmt.setObject(getUpdateParametersCount() + 1, id); // ID'yi son parametre olarak ekle
            stmt.executeUpdate();
        }
    }

    // Delete
    public void delete(I id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }

    // Yardımcı metodlar
    protected abstract String getInsertColumns();

    protected abstract String getInsertPlaceholders();

    protected abstract String getUpdateColumns();

    protected abstract int getUpdateParametersCount();
}
