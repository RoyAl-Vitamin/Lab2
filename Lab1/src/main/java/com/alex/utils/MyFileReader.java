package com.alex.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class MyFileReader implements Runnable {

    private File file;

    private Map<String, Integer> hashMap = new HashMap<>();

    public Connection connection;

    private Logger log = Logger.getLogger(MyFileReader.class.getName());

    @Override
    public void run() {
        connection = Utils.getConnection();
        read();
        pasteIntoDB();
    }

    /**
     * Вставка данных в ХэшМап для последующей вставки в БД
     */
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
            log.warning(ex.getMessage());
        }
    }

    /**
     * Вставка данных в таблицу
     */
    private void pasteIntoDB() {
        String filePath = file.getAbsolutePath();
        if (!rowIsExists(filePath)) {
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO 'file_input' ('file_input_file_name') VALUES (?);");
                stmt.setString(1, filePath);
                stmt.executeUpdate();
                stmt = connection.prepareStatement("SELECT file_input.id FROM file_input WHERE file_input_file_name = ?");
                stmt.setString(1, filePath);
                ResultSet rs = stmt.executeQuery();
                int tableId = 0;
                while (rs.next()) {
                    tableId = rs.getInt(1);
                }
                for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                    stmt = connection.prepareStatement("INSERT INTO 'word_count' ('word_count_word', 'word_count_count', 'file_input_id') VALUES (?, ?, ?); ");
                    stmt.setString(1, entry.getKey());
                    stmt.setInt(2, entry.getValue());
                    stmt.setInt(3, tableId);
                    stmt.executeUpdate();
                }
                //            connection.commit();
                connection.setAutoCommit(true);

            } catch (SQLException e) {
                log.warning(e.toString());
                e.printStackTrace();
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.warning(e1.toString());
                    e1.printStackTrace();
                }
            }
        }
    }

    private boolean rowIsExists(String filePath) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT fi.file_input_file_name FROM file_input fi WHERE fi.file_input_file_name = ?;");
            pstmt.setString(1, filePath);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            log.warning(e.getMessage());
        }
        return true;
    }

    public MyFileReader(File file) {
        this.file = file;
    }
}
