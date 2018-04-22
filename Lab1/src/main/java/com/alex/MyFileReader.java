package com.alex;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MyFileReader implements Runnable {

    private final String driverName = "org.sqlite.JDBC";
    private final String connectionString = "jdbc:sqlite:sample.db";

    private File file;

    private Map<String, Integer> hashMap = new HashMap<>();

    private Connection connection;

    @Override
    public void run() {
        connectToDB();

        read();

        pasteIntoDB();

        closeConnectToDB();
    }

    private void read() {
        try(FileReader reader = new FileReader(file)) {
            Scanner scan = new Scanner(reader);
            while (scan.hasNextLine()) {
                String[] wordArray = scan.nextLine().trim().replaceAll("[^a-zA-Zа-яА-Я ]", "").toLowerCase().split("\\s+");

                for (String val: wordArray) {
                    if (hashMap.containsKey(val)) {
                        hashMap.put(val, hashMap.get(val) + 1);
                    } else {
                        hashMap.put(val, 1);
                    }
                }
            }
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void pasteIntoDB() {
        try {
            String filePath = file.getAbsolutePath();
            connection.setAutoCommit(false);
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO 'file_input' ('file_input_file_name') VALUES (?);");
            stmt.setString(1, filePath);
            stmt.executeUpdate();
            stmt = connection.prepareStatement("SELECT file_input.id FROM file_input WHERE file_input_file_name = ?");
            stmt.setString(1, filePath);
            ResultSet rs = stmt.executeQuery();
            int tableId = 0;
            while (rs.next()) {
                tableId = rs.getInt("id");
            }
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                stmt = connection.prepareStatement("INSERT INTO 'word_count' ('word_count_word', 'word_count_count', 'file_input_id') VALUES (?, ?, ?); ");
                stmt.setString(1, entry.getKey());
                stmt.setInt(2, entry.getValue());
                stmt.setInt(3, tableId);
                stmt.executeUpdate();
            }
            connection.commit();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void connectToDB() {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            System.out.println("Can't get class. No driver found");
            e.printStackTrace();
            return;
        }

        connection = null;
        try {
            connection = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            System.out.println("Can't get connection. Incorrect URL");
            e.printStackTrace();
            return;
        }

        try {
            Statement statmt = connection.createStatement();

            statmt.execute("CREATE TABLE if not exists 'file_input' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'file_input_file_name' text unique);");
            statmt.execute("CREATE TABLE if not exists 'word_count' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'word_count_word' text, 'word_count_count' INT, file_input_id REFERENCES file_input(id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Таблица создана или уже существует.");

    }

    private void closeConnectToDB() {
        /*for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            System.out.println(entry.getKey() + " + " + entry.getValue());
        }*/
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Can't close connection");
            e.printStackTrace();
            return;
        }
    }

    public MyFileReader(File file) {
        this.file = file;
    }
}
