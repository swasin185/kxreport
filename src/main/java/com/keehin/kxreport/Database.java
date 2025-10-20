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
    public static final Properties prop = new Properties();
    static {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            prop.load(input);
            if (prop.getProperty("OUTPUT_PATH") == null)
                prop.setProperty("OUTPUT_PATH", "webapps/kxreport/");
            if (prop.getProperty("REPORT_PATH") == null)
                prop.setProperty("REPORT_PATH", "/khgroup/report/");
            if (prop.getProperty("JDBC_URI") == null)
                prop.setProperty("JDBC_URI", "jdbc:mariadb://localhost:3306/");
            if (prop.getProperty("USER") == null)
                prop.setProperty("USER", "kxreport");
            if (prop.getProperty("PASSWORD") == null)
                prop.setProperty("PASSWORD", "kxreport");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getOUTPUT_PATH() {
        return prop.getProperty("OUTPUT_PATH");
    }

    public static String getREPORT_PATH() {
        return prop.getProperty("REPORT_PATH");
    }

    public static String getJDBC_URI() {
        return prop.getProperty("JDBC_URI");
    }

    public static String getUSER() {
        return prop.getProperty("USER");
    }

    public static String getPASSWORD() {
        return prop.getProperty("PASSWORD");
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
            if (pools.get(db) == null) /* If Databse is SSL use &useSSL=true&requireSSL=false */
                pools.put(db,
                        new MariaDbPoolDataSource(
                                getJDBC_URI() + db + "?user=" + getUSER() + "&password=" + getPASSWORD()
                                        + "&minPoolSize=0&maxPoolSize=30&maxIdleTime=60"));
            conn = pools.get(db).getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}