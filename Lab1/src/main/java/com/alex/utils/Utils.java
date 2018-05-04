package com.alex.utils;

import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Utils {

    private static final String driverName = "org.sqlite.JDBC";

    private static String connectionString;

    private static Logger log = Logger.getLogger(Utils.class.getName());

    private static Connection connection = null;

    private static final String COUNT_FILES = "SELECT Count(fi.id) FROM file_input fi";

    private static final String SELECT_FI_FILE_NAME = "SELECT fi.file_input_file_name FROM file_input fi WHERE fi.file_input_file_name = ?;";

    private static final String CREATE_TABLE_FI = "CREATE TABLE IF NOT EXISTS 'file_input' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'file_input_file_name' text unique);";

    private static final String CREATE_TABLE_WC  = "CREATE TABLE IF NOT EXISTS 'word_count' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'word_count_word' text, 'word_count_count' INT, file_input_id REFERENCES file_input(id));";

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

        StringBuilder sb = new StringBuilder("jdbc:sqlite:");
        sb.append(catalinaHome).append(File.separator).append("webapps").append(File.separator).append(artifactId)
                .append("-").append(version).append(File.separator).append("WEB-INF").append(File.separator)
                .append("classes").append(File.separator).append("sqlitedb.db");
//        connectionString = "jdbc:sqlite:" + catalinaHome + File.separator + "webapps" + File.separator + artifactId
//                + "-" + version + File.separator + "WEB-INF" + File.separator + "classes" + File.separator
//                + "sqlitedb.db";

        connectionString = sb.toString();

//        log.info("connectionString == " + connectionString);
    }

    /**
     * Создаёт коннект к базе, который живёт весь ЖЦ сервлета и сам закрывается при разрущении контекста сервлета
     * Нужен для нетрудоёмких опреаций и не продолжительных
     * @return {@link java.sql.Connection}
     */
    public static synchronized Connection getMainConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                preCreate();
                connectToDB();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    /**
     * Создаёт новый коннет к базе, который нужно самостоятельно закрыть, используется при многопоточном доступе к БД
     * Для асинхронных операций
     * @return {@link java.sql.Connection}
     */
    public static synchronized Connection getNewConnection() {
        Connection newConnection = null;
        try {
            if (connection == null || connection.isClosed()) {
                getMainConnection();
            }
            Class.forName(driverName);
            SQLiteConfig config = new SQLiteConfig();
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            newConnection = DriverManager.getConnection(connectionString, config.toProperties());
            return newConnection;
        } catch (SQLException | ClassNotFoundException e) {
            log.warning(e.toString());
            throw new RuntimeException(e);
        }
    }

    private static void connectToDB() {
        try {
            Class.forName(driverName);
            SQLiteConfig config = new SQLiteConfig();
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            connection = DriverManager.getConnection(connectionString, config.toProperties());
//            connection = DriverManager.getMainConnection(connectionString);
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

            statmt.execute(CREATE_TABLE_FI);
            statmt.execute(CREATE_TABLE_WC);
        } catch (SQLException e) {
            log.warning("Can't create Table");
            throw new RuntimeException(e);
        }
    }

    public static void closeConnectToDB() {
        try {
            connection.close();
            connection = null;
        } catch (SQLException e) {
            log.warning("Can't close connection");
            throw new RuntimeException(e);
        }
    }

    public static int getCountRowDB() {
        int count = 0;
        try {
            ResultSet rs = Utils.getMainConnection().prepareStatement(COUNT_FILES).executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            log.warning("Can't get count row DB");
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
