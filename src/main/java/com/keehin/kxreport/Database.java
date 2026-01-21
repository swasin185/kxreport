package com.keehin.kxreport;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Database {

    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private static final Properties prop = new Properties();

    public static final String ROOT = "webapps";

    static {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            prop.load(input);
        } catch (IOException e) {
            logger.error("application.properties", e);
        }
    }

    public static String getReportPath() {
        return prop.getProperty("REPORT_PATH");
    }

    public static String getJdbcUri() {
        return prop.getProperty("JDBC_URI");
    }

    public static String getDbConfig() {
        return prop.getProperty("DB_CONFIG");
    }

    private ConcurrentHashMap<String, DataSource> pools = new ConcurrentHashMap<String, DataSource>();

    public Database() {
        try {
            DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
        } catch (SQLException e) {
            logger.error("Database Driver", e);
        }
    }

    public Connection getConnection(String db) throws SQLException {
        Connection conn = null;
        if (pools.get(db) != null) {
            conn = pools.get(db).getConnection();
            if (conn != null)
                return conn;
        }
        String dbURI = Database.getJdbcUri() + db + Database.getDbConfig();
        logger.info("connect {}{}", Database.getJdbcUri(), db);
        pools.put(db, new MariaDbPoolDataSource(dbURI));
        conn = pools.get(db).getConnection();
        return conn;
    }
}