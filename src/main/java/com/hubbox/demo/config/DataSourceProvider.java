package com.hubbox.demo.config;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DataSourceProvider implements Provider<DataSource> {

    private final DatabaseConfig databaseConfig;

    @Inject
    public DataSourceProvider(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public DataSource get() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseConfig.url());
        config.setUsername(databaseConfig.username());
        config.setPassword(databaseConfig.password());
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }
}
