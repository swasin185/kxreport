package com.keehin.kxreport;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;
import org.mariadb.jdbc.MariaDbPoolDataSource;

public class Database {
    private static final String OUTPUT_PATH = "OUTPUT_PATH";
    private static final String REPORT_PATH = "REPORT_PATH";
    private static final String JDBC_URI = "JDBC_URI";
    private static final String DB_CONFIG = "DB_CONFIG";
    private static final Properties prop = new Properties();
    static {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getOutputPath() {
        return prop.getProperty(OUTPUT_PATH);
    }

    public static String getReportPath() {
        return prop.getProperty(REPORT_PATH);
    }

    public static String getJdbcUri() {
        return prop.getProperty(JDBC_URI);
    }

    public static String getDbConfig() {
        return prop.getProperty(DB_CONFIG);
    }

    private HashMap<String, DataSource> pools = new HashMap<String, DataSource>();

    public Database() throws SQLException {
        DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
    }

    public Connection getConnection(String db) {
        Connection conn = null;
        if (db == null)
            db = "kxtest";
        try {
            if (pools.get(db) != null) {
                conn = pools.get(db).getConnection();
                if (conn != null)
                    return conn;
            }
            pools.put(db, new MariaDbPoolDataSource(
                    Database.getJdbcUri() + db + Database.getDbConfig()));
            conn = pools.get(db).getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}