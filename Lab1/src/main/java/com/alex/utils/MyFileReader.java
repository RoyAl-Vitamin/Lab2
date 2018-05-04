package com.alex.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import static java.sql.Statement.EXECUTE_FAILED;
import static java.sql.Statement.SUCCESS_NO_INFO;

public class MyFileReader implements Runnable {

    private static Logger log = Logger.getLogger(MyFileReader.class.getName());

    private static final String INSERT_INTO_FI = "INSERT INTO 'file_input' ('file_input_file_name') VALUES (?);";

    private static final String INSERT_INTO_WC = "INSERT INTO 'word_count' ('word_count_word', 'word_count_count', 'file_input_id') VALUES (?, ?, ?);";

    private static final String SELECT_FI_ID = "SELECT file_input.id FROM file_input WHERE file_input_file_name = ?";

    private File file;

    public static Connection connection = Utils.getMainConnection();

    @Override
    public void run() {
        pasteIntoDB(file.getName(), read());
    }

    /**
     * Вставка данных в ХэшМап для последующей вставки в БД
     */
    private Map<String, Integer> read() {
//        try(FileReader reader = new FileReader(file)) {
        try(InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
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
            boolean isOk = true;
            try (Connection localConnection = Utils.getNewConnection()) {
                localConnection.setAutoCommit(false);
                try {
                    PreparedStatement stmt = localConnection.prepareStatement(INSERT_INTO_FI);
                    stmt.setString(1, fileName);
                    stmt.executeUpdate();
                    stmt = localConnection.prepareStatement(SELECT_FI_ID);
                    stmt.setString(1, fileName);
                    ResultSet rs = stmt.executeQuery();
                    int tableId = 0;
                    if (rs.next()) {
                        tableId = rs.getInt(1);
                    } else {
                        throw new RuntimeException("Can't find tableID");
                    }
                    stmt = localConnection.prepareStatement(INSERT_INTO_WC);
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        stmt.setString(1, entry.getKey());
                        stmt.setInt(2, entry.getValue());
                        stmt.setInt(3, tableId);
                        stmt.addBatch(); // Должно работать быстрее?
                    }
                    int[] updateCounts = stmt.executeBatch();
                    int success = 0, successNoInfo = 0, executeFailed = 0;
                    for (int i : updateCounts) {
                        if (i >= 0) {
                            success++;
                        } else if (i == SUCCESS_NO_INFO) {
                            successNoInfo++;
                        } else if (i == EXECUTE_FAILED) {
                            executeFailed++;
                        }
                    }
//                    log.info("SUCCESS == " + success);
//                    log.info("SUCCESS_NO_INFO == " + successNoInfo);
//                    log.info("EXECUTE_FAILED == " + executeFailed);
                } catch (SQLException e) {
                    try {
                        localConnection.rollback();
                        isOk = false;
                    } catch (SQLException e1) {
                        log.warning(e1.toString());
                        throw new RuntimeException(e1);
                    }
                }
                if (isOk) {
                    localConnection.commit();
                }
                localConnection.setAutoCommit(true);
            } catch (SQLException e) {
                log.warning(e.toString());
                throw new RuntimeException(e);
            }
        } else {
            log.warning("File exist! Conflict name");
        }
    }

    public MyFileReader(File file) {
        this.file = file;
    }
}
