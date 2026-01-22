package com.keehin.kxreport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Database {

    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    public static final String ROOT = "webapps";

    // Inject configuration properties from application.properties
    @Value("${kxreport.report.path}")
    private String reportPath;

    @Value("${kxreport.db.uri}")
    private String jdbcUri;

    @Value("${kxreport.db.config}")
    private String dbConfig;

    @Value("${kxreport.db.config.ssl}")
    private String dbConfigSsl;

    private ConcurrentHashMap<String, DataSource> pools = new ConcurrentHashMap<String, DataSource>();

    public Database() {
        try {
            DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
        } catch (SQLException e) {
            logger.error("Database Driver", e);
        }
    }

    // Getter methods for injected properties
    public String getReportPath() {
        return reportPath;
    }

    public Connection getConnection(String db) throws SQLException {
        logger.info("Database getConnection {} {}", dbConfig, reportPath);
        Connection conn = null;
        if (pools.get(db) == null) {
            String dbURI = jdbcUri + db + dbConfig;
            logger.info("connect {}{}", jdbcUri, db);
            pools.put(db, new MariaDbPoolDataSource(dbURI));
        }
        conn = pools.get(db).getConnection();
        return conn;
    }
}