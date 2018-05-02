package com.alex.utils;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Utils {

    private static final String driverName = "org.sqlite.JDBC";

    private static String connectionString;

    private static Logger log = Logger.getLogger(Utils.class.getName());

    private static Connection connection = null;

    private final static String COUNT_FILES = "SELECT Count(fi.id) FROM file_input fi";

    private static final String SELECT_FI_FILE_NAME = "SELECT fi.file_input_file_name FROM file_input fi WHERE fi.file_input_file_name = ?;";

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

    public static int getCountRowDB() {
        int count = 0;
        try {
            ResultSet rs = Utils.getConnection().prepareStatement(COUNT_FILES).executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    /**
     * Проверяет, существует ли запись с таким именем файла в БД
     * @param fileName
     * @return
     */
    public static boolean rowIsExists(String fileName) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(SELECT_FI_FILE_NAME);
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
