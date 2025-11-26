package com.micro.util;

import com.micro.listener.AppContextListener;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Helper to access pooled connections from the servlet context.
 */
public final class DbUtil {

    private DbUtil() {
    }

    public static Connection getConnection(ServletContext context) throws SQLException {
        Objects.requireNonNull(context, "context");
        DataSource dataSource = AppContextListener.getDataSource(context);
        return dataSource.getConnection();
    }
}
