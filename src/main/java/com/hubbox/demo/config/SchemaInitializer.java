package com.hubbox.demo.config;

import com.hubbox.demo.exceptions.BaseRuntimeException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class SchemaInitializer {

    private final DataSource dataSource;

    @Inject
    public SchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initializeSchema() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder sqlScript = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sqlScript.append(line).append("\n");
            }

            stmt.execute(sqlScript.toString());
            conn.commit();
            log.info("Database schema initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize database schema: {}", e.getMessage());
            throw new BaseRuntimeException("Schema initialization failed", e);
        }
    }
}
