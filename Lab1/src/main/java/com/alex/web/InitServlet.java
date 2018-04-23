package com.alex.web;

import com.alex.utils.MyFileReader;
import com.alex.utils.Row;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet(name = "InitServlet", urlPatterns = "/index")
public class InitServlet extends HttpServlet {

    private Logger log = Logger.getLogger(InitServlet.class.getName());

    private final String SQL =
            "SELECT fi.file_input_file_name, wc.word_count_count " +
            "FROM word_count wc " +
            "LEFT OUTER JOIN file_input fi ON fi.id = wc.file_input_id " +
            "WHERE wc.word_count_word = ? " +
            "ORDER BY wc.word_count_count DESC;";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            doing(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            log.warning(e.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            doing(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            log.warning(e.toString());
        }
    }

    private void doing(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        // Load from DB and create index
        // Теоретически должен работать
        String sentence = request.getParameter("text");
        for (String word : sentence.split("\\s+")) {
            PreparedStatement stmt = MyFileReader.connection.prepareStatement(SQL);
            stmt.setString(1, word);
            ResultSet rs = stmt.executeQuery();
            Map<String, Integer> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        }
        /*
        List<Map<String, Integer>> list = new ArrayList<>(); // May be?
        */
        List<Row> list; list = new ArrayList<>();
        list.add(new Row("name1", 1));
        list.add(new Row("name2", 2));
        list.add(new Row("name3", 3));
        list.add(new Row("name4", 4));
        request.setAttribute("list", list);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
