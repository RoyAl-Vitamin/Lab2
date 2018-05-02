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

    private static Logger log = Logger.getLogger(MyFileReader.class.getName());

    private static final String INSERT_INTO_FI = "INSERT INTO 'file_input' ('file_input_file_name') VALUES (?);";

    private static final String INSERT_INTO_WC = "INSERT INTO 'word_count' ('word_count_word', 'word_count_count', 'file_input_id') VALUES (?, ?, ?);";

    private static final String SELECT_FI_ID = "SELECT file_input.id FROM file_input WHERE file_input_file_name = ?";

    private File file;

    public static Connection connection = Utils.getConnection();

    @Override
    public void run() {
        pasteIntoDB(file.getName(), read());
    }

    /**
     * Вставка данных в ХэшМап для последующей вставки в БД
     */
    private Map<String, Integer> read() {
        try(FileReader reader = new FileReader(file)) {
            Scanner scan = new Scanner(reader);
            Map<String, Integer> hashMap = new HashMap<>();
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
            return hashMap;
        } catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * Вставка данных в таблицу
     */
    public static void pasteIntoDB(String fileName, Map<String, Integer> map) {
        if (!Utils.rowIsExists(fileName)) {
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmt = connection.prepareStatement(INSERT_INTO_FI);
                stmt.setString(1, fileName);
                stmt.executeUpdate();
                stmt = connection.prepareStatement(SELECT_FI_ID);
                stmt.setString(1, fileName);
                ResultSet rs = stmt.executeQuery();
                int tableId = 0;
                while (rs.next()) {
                    tableId = rs.getInt(1);
                }
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    stmt = connection.prepareStatement(INSERT_INTO_WC);
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
        } else {
            log.warning("File exist! Conflict name");
        }
    }

    public MyFileReader(File file) {
        this.file = file;
    }
}
