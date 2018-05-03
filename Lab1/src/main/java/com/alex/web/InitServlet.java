package com.alex.web;

import com.alex.utils.Row;
import com.alex.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import static com.alex.utils.Utils.getCountRowDB;

@WebServlet(name="InitServlet", urlPatterns="/index")
public class InitServlet extends HttpServlet {

    private Logger log = Logger.getLogger(InitServlet.class.getName());

    private final static String MAP_NAME_COUNT =
            "SELECT fi.file_input_file_name, wc.word_count_count " +
            "FROM word_count wc " +
            "LEFT OUTER JOIN file_input fi ON fi.id = wc.file_input_id " +
            "WHERE wc.word_count_word = ? " +
            "ORDER BY wc.word_count_count DESC;";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("text") == null || request.getParameter("text").trim().length() == 0) {
            request.setAttribute("count", getCountRowDB());
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        try {
            doing(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            log.warning(e.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("text") == null || request.getParameter("text").trim().length() == 0) {
            request.setAttribute("count", getCountRowDB());
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        try {
            doing(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            log.warning(e.toString());
        }
    }

    private void doing(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        // Load from DB and create index
        String sentence = request.getParameter("text");
        request.setAttribute("sentence", sentence);
        Map<String, Integer> map = new HashMap<>();
        String[] listString = sentence.split("\\s+");
        log.info("SENTENCE: " + sentence);
//        for (String word : listString) {
//            log.info("word == " + word);
//        }
        for (String word : listString) {
            PreparedStatement stmt = Utils.getConnection().prepareStatement(MAP_NAME_COUNT);
            stmt.setString(1, word);
            ResultSet rs = stmt.executeQuery();
            log.info("FOUND START");
            while (rs.next()) {
//                log.info("FOUND WORD == " + rs.getString(1) + " COUNT == " + rs.getInt(2));
                if (map.containsKey(rs.getString(1))) {
                    Integer i = map.get(rs.getString(1));
                    map.put(rs.getString(1), i + rs.getInt(2));
                } else {
                    map.put(rs.getString(1), rs.getInt(2));
                }
            }
            log.info("FOUND END");
        }

        if (map.isEmpty()) {
            log.info("MAP is Empty");
            request.setAttribute("list", Collections.singletonList(new Row("#EMPTY", 0)));
            request.setAttribute("count", getCountRowDB());
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }
//        log.info("MAP:");
//        for (Map.Entry<String, Integer> entry : map.entrySet()) {
//            log.info("getKey == " + entry.getKey() + " getValue" + entry.getValue());
//        }


        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)-> o2.getValue().compareTo(o1.getValue()));

        List<Row> newList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : list) {
            newList.add(new Row(entry.getKey(), entry.getValue()));
        }
        request.setAttribute("list", newList);

        log.info("LIST:");
        for (Row row : newList) {
            log.info("getName == " + row.getName() + " getIndex == " + row.getIndex());
        }
        request.setAttribute("count", getCountRowDB());
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
