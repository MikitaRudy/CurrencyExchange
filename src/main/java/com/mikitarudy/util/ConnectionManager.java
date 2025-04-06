package com.mikitarudy.util;

import com.mikitarudy.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionManager {
    private static final String DB_URL = "jdbc:sqlite::resource:identifier.sqlite";

    private static final HikariDataSource HIKARI_DATA_SOURCE;
    private static final HikariConfig HIKARI_CONFIG;

    static {
        HIKARI_CONFIG = new HikariConfig();
        HIKARI_CONFIG.setJdbcUrl(DB_URL);
        HIKARI_CONFIG.setDriverClassName("org.sqlite.JDBC");
        HIKARI_DATA_SOURCE = new HikariDataSource(HIKARI_CONFIG);
    }

    private ConnectionManager(){}

    public static Connection getConnection(){
        try {
            return HIKARI_DATA_SOURCE.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Database is unavailable");
        }
    }

    public static void closeConnection(){
        if (!HIKARI_DATA_SOURCE.isClosed()) {
            HIKARI_DATA_SOURCE.close();
        }
    }
}
