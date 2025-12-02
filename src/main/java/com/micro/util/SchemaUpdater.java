package com.micro.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaUpdater.class);

    public static void checkAndUpdate(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            
            // Check if banner_path column exists in users table
            if (!columnExists(meta, "users", "banner_path")) {
                LOGGER.info("Column 'banner_path' not found in 'users' table. Adding it...");
                addColumn(conn, "users", "banner_path", "VARCHAR(255) AFTER avatar_path");
            } else {
                LOGGER.info("Column 'banner_path' already exists in 'users' table.");
            }

            // Add BLOB columns for avatar and banner
            if (!columnExists(meta, "users", "avatar_data")) {
                LOGGER.info("Column 'avatar_data' not found. Adding it...");
                addColumn(conn, "users", "avatar_data", "LONGBLOB");
            }
            if (!columnExists(meta, "users", "avatar_type")) {
                LOGGER.info("Column 'avatar_type' not found. Adding it...");
                addColumn(conn, "users", "avatar_type", "VARCHAR(50)");
            }
            if (!columnExists(meta, "users", "banner_data")) {
                LOGGER.info("Column 'banner_data' not found. Adding it...");
                addColumn(conn, "users", "banner_data", "LONGBLOB");
            }
            if (!columnExists(meta, "users", "banner_type")) {
                LOGGER.info("Column 'banner_type' not found. Adding it...");
                addColumn(conn, "users", "banner_type", "VARCHAR(50)");
            }

        } catch (SQLException e) {
            LOGGER.error("Failed to check or update database schema", e);
        }
    }

    private static boolean columnExists(DatabaseMetaData meta, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static void addColumn(Connection conn, String tableName, String columnName, String columnDefinition) throws SQLException {
        String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDefinition);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info("Successfully added column {} to table {}", columnName, tableName);
        }
    }
}
