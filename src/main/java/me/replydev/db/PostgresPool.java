package me.replydev.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgresPool {

    public static HikariDataSource getHikarDatasource() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("QuboPool");
        config.setConnectionTestQuery("SELECT 1");
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.addDataSourceProperty("serverName", "db");
        config.addDataSourceProperty("portNumber", "5432");
        config.addDataSourceProperty("databaseName", "server_db");
        config.addDataSourceProperty("user", "root");
        config.addDataSourceProperty("password", "root");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        config.setMaximumPoolSize(50);
        HikariDataSource dataSource = new HikariDataSource(config);
        try (Connection conn = dataSource.getConnection()) {
            conn.isValid(5 * 1000);
        } catch (SQLException e) {
            System.exit(1);
        }
        return dataSource;
    }

}
