package com.keehin.kxreport;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import javax.sql.DataSource;
import org.mariadb.jdbc.MariaDbPoolDataSource;

public class Database {
    // update application.properties
    public static String OUTPUT_PATH = "webapps/kxreport/";
    public static String REPORT_PATH = "/khgroup/report";
    public static String JDBC_URI = "jdbc:mariadb://localhost:3306/";
    public static String USER = "kxreport";
    public static String PASSWORD = "kxreport";
    public static final Properties prop = new Properties();

    static {
        try (FileReader input = new FileReader("webapps/kxreport/WEB-INF/classes/application.properties")) {
            prop.load(input);
            OUTPUT_PATH = prop.getProperty("OUTPUT_PATH", OUTPUT_PATH);
            REPORT_PATH = prop.getProperty("REPORT_PATH", REPORT_PATH);
            JDBC_URI = prop.getProperty("JDBC_URI", JDBC_URI);
            USER = prop.getProperty("USER", USER);
            PASSWORD = prop.getProperty("PASSWORD", PASSWORD);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final SimpleDateFormat DTFormat = new SimpleDateFormat("dd/MM/yy [HH:mm:ss]", Locale.US);

    private HashMap<String, DataSource> pools = new HashMap<String, DataSource>();

    public Database() throws SQLException {
        DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
    }

    public Connection getConnection(String db) {
        Connection conn = null;
        if (db == null)
            db = "kxtest";
        try {
            if (pools.get(db) == null)
            pools.put(db, new MariaDbPoolDataSource(JDBC_URI + db + "?user=" + USER + "&password=" + PASSWORD
                + "&minPoolSize=0&maxPoolSize=30&maxIdleTime=60"));
            conn = pools.get(db).getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

}