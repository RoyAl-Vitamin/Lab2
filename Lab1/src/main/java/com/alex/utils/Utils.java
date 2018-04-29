package com.alex.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

public class Utils {

    private static final String driverName = "org.sqlite.JDBC";

    private static String connectionString;

    private static Logger log = Logger.getLogger(Utils.class.getName());

    private static Connection connection = null;

    private static void preCreate() {
        // read from system enviroments
        String catalinaHome = System.getenv().get("CATALINA_HOME");
        if (catalinaHome == null) {
            log.warning("$CATALINA_HOME must be determinate!");
            throw new RuntimeException("$CATALINA_HOME must be determinate!");
        }
        // read from init.properties
        String version, artifactId;
        try {
            final Properties properties = new Properties();
            properties.load(Utils.class.getResourceAsStream("/init.properties"));
            version = properties.getProperty("version");
            artifactId = properties.getProperty("artifactId");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        connectionString = "jdbc:sqlite:" + catalinaHome + "/webapps/" + artifactId + "-" + version +"/WEB-INF/classes/sqlitedb.db";
        log.info("connectionString == " + connectionString);
    }

    public static Connection getConnection() {
        if (connection == null) {
            preCreate();
            connectToDB();
        }
        return connection;
    }

    private static void connectToDB() {
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(connectionString);
        } catch (ClassNotFoundException e) {
            log.warning("Can't get class. No driver found");
            connection = null;
            throw new RuntimeException(e);
        } catch (SQLException e) {
            log.warning("Can't get connection. Incorrect URL");
            connection = null;
            throw new RuntimeException(e);
        }
        try {
            Statement statmt = connection.createStatement();

            statmt.execute("CREATE TABLE if not exists 'file_input' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'file_input_file_name' text unique);");
            statmt.execute("CREATE TABLE if not exists 'word_count' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'word_count_word' text, 'word_count_count' INT, file_input_id REFERENCES file_input(id));");
        } catch (SQLException e) {
            log.warning("Can't create Table");
            e.printStackTrace();
        }
    }

    public static void closeConnectToDB() {
        try {
            connection.close();
            connection = null;
        } catch (SQLException e) {
            log.warning("Can't close connection");
            e.printStackTrace();
        }
    }
}
