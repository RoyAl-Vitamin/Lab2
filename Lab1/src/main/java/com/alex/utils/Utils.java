package com.alex.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Utils {

    private static final String driverName = "org.sqlite.JDBC";
//    private static final String connectionString = "jdbc:sqlite::memory:";
    private static String connectionString;

    private static Logger log = Logger.getLogger(Utils.class.getName());

    private static Connection connection = null;

    private static void preCreate() {
        // Костыль внедрён
        /*MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try {
            model = reader.read(new FileReader("pom.xml"));
        } catch (IOException e) {
            e.printStackTrace();
            log.warning(e.getMessage());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            log.warning(e.getMessage());
        }*/

        String catalinaHome = System.getenv().get("CATALINA_HOME");
        if (catalinaHome == null) {
            log.warning("$CATALINA_HOME must be determinate!");
            throw new RuntimeException("$CATALINA_HOME must be determinate!");
        }
        // read from init.properties

//        connectionString = "jdbc:sqlite:" + System.getProperties().getProperty("CATALINA_HOME") + "webapps/" + model.getArtifactId() + "-" + model.getVersion() + "/sqlitedb.db";
        connectionString = "jdbc:sqlite:" + catalinaHome + "/webapps/Lab1-1.0-SNAPSHOT/WEB-INF/classes/sqlitedb.db";
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
            e.printStackTrace();
            connection = null;
            return;
        } catch (SQLException e) {
            log.warning("Can't get connection. Incorrect URL");
            e.printStackTrace();
            connection = null;
            return;
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
