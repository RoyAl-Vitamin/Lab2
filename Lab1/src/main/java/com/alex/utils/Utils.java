package com.alex.utils;

import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Utils {

    private static final String driverName = "org.sqlite.JDBC";

    private static Logger log = Logger.getLogger(Utils.class.getName());

    private static String connectionString = getConnectionString();

//    private static final String WAL = "pragma journal_mode=wal";

    private static final String COUNT_FILES = "SELECT Count(fi.id) FROM file_input fi";

    private static final String SELECT_FI_FILE_NAME = "SELECT fi.file_input_file_name FROM file_input fi WHERE fi.file_input_file_name = ?;";

    private static final String CREATE_TABLE_FI = "CREATE TABLE IF NOT EXISTS 'file_input' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'file_input_file_name' text unique);";

    private static final String CREATE_TABLE_WC  = "CREATE TABLE IF NOT EXISTS 'word_count' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'word_count_word' text, 'word_count_count' INT, file_input_id REFERENCES file_input(id));";

    /**
     * Создаёт новый коннет к базе, который нужно самостоятельно закрыть, используется при многопоточном доступе к БД
     * Для асинхронных операций
     * @return {@link java.sql.Connection}
     */
    public static synchronized Connection getNewConnection() {
        Connection connection = null;
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            connection = DriverManager.getConnection(connectionString, config.toProperties());
        } catch (SQLException e) {
            log.warning(e.toString());
            throw new RuntimeException(e);
        }
        return connection;
    }

    private static String getConnectionString() {
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

        return sb.toString();
    }

    public static void init() {

        SQLiteConfig config;
        try {
            Class.forName(driverName);
            config = new SQLiteConfig();
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        } catch (ClassNotFoundException e) {
            log.warning("Can't get class. No driver found");
            throw new RuntimeException(e);
        }

        try (Connection connection = DriverManager.getConnection(connectionString, config.toProperties());
             Statement statmt = connection.createStatement()) {
            statmt.execute(CREATE_TABLE_FI);
            statmt.execute(CREATE_TABLE_WC);
        } catch (SQLException e) {
            log.warning("Can't get connection. Incorrect URL or can't create Table");
            throw new RuntimeException(e);
        }
    }

    /**
     * Количество проиндексированных файлов
     * @return
     */
    public static int getCountRowDB() {
        int count = 0;
        try (Connection connection = getNewConnection();
             PreparedStatement pstmt = connection.prepareStatement(COUNT_FILES);
             ResultSet rs = pstmt.executeQuery()) {

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
        boolean ret = false;

        try (Connection connection = getNewConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_FI_FILE_NAME)) {

            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            ret = rs.next();
            rs.close();
        } catch (SQLException e) {
            log.warning(e.toString());
            throw new RuntimeException(e);
        }
        return ret;
    }
}
