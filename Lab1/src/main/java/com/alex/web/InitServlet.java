package com.alex.web;

import com.alex.utils.Row;
import com.alex.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import static com.alex.utils.Utils.getCountRowDB;

@WebServlet(urlPatterns = {"/index"}) // don't work with "/"
public class InitServlet extends HttpServlet {

    private Logger log = Logger.getLogger(InitServlet.class.getName());

    private long start = 0;

    private final static String MAP_NAME_COUNT =
            "SELECT fi.file_input_file_name, wc.word_count_count " +
            "FROM word_count wc " +
            "LEFT OUTER JOIN file_input fi ON fi.id = wc.file_input_id " +
            "WHERE wc.word_count_word = ? " +
            "ORDER BY wc.word_count_count DESC;";

    // Предпочитаемый вывод времени
    private final TimeDelimeter timeDelimeter = TimeDelimeter.MILLISECOND;

    private enum TimeDelimeter {
        NANOSECOND,
        MICROSECOND,
        MILLISECOND,
        SECOND;
    }

    // Кэш запросов
    private Map<String, List<Row>> cache;

    private void preCreate() {
        cache = (Map<String, List<Row>>) getServletContext().getAttribute("cache");
        if (cache == null) {
            cache = new HashMap<>();
        }
        start = System.nanoTime();
    }

    /**
     * Возвращает запрос, если он пуст или данные уже закешированы
     * @param request
     * @param response
     * @return true если нужно вернуть запрос, иначе - его нужно обработать
     * @throws ServletException
     * @throws IOException
     */
    private boolean revertRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("text") == null || request.getParameter("text").trim().length() == 0) {
            request.setAttribute("time", getProcessedTime());
            request.setAttribute("count", getCountRowDB());
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return true;
        }

        if (cache.containsKey(request.getParameter("text"))) {
            request.setAttribute("list", cache.get(request.getParameter("text")));
            request.setAttribute("sentence", request.getParameter("text"));
            request.setAttribute("count", getCountRowDB());
            request.setAttribute("time", getProcessedTime());
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return true;
        }
        return false;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        preCreate();

        if (revertRequest(request, response)) {
            return;
        }

        doing(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        preCreate();

        if (revertRequest(request, response)) {
            return;
        }

        doing(request, response);
    }

    private void doing(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Load from DB and create index
        String sentence = request.getParameter("text");
        request.setAttribute("sentence", sentence);
        Map<String, Integer> map = new HashMap<>();
        String[] listString = sentence.split("\\s+");

        try (Connection localConnection = Utils.getNewConnection();
             PreparedStatement stmt = localConnection.prepareStatement(MAP_NAME_COUNT)) {

            for (String word : listString) {
                stmt.setString(1, word.toLowerCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        if (map.containsKey(rs.getString(1))) {
                            Integer i = map.get(rs.getString(1));
                            map.put(rs.getString(1), i + rs.getInt(2));
                        } else {
                            map.put(rs.getString(1), rs.getInt(2));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.warning(e.toString());
            throw new RuntimeException(e);
        }

        if (map.isEmpty()) {
            cache.put(sentence, Collections.singletonList(new Row("#EMPTY", 0)));
            getServletContext().setAttribute("cache", cache);
            request.setAttribute("list", Collections.singletonList(new Row("#EMPTY", 0)));
            request.setAttribute("count", getCountRowDB());
            request.setAttribute("time", getProcessedTime());
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)-> o2.getValue().compareTo(o1.getValue()));

        List<Row> newList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : list) {
            newList.add(new Row(entry.getKey(), entry.getValue()));
        }

        cache.put(sentence, newList);
        getServletContext().setAttribute("cache", cache);
        request.setAttribute("list", newList);
        request.setAttribute("count", getCountRowDB());
        request.setAttribute("time", getProcessedTime());
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    private String getProcessedTime() {
        String time;
        switch (timeDelimeter) {
            case NANOSECOND:
                time = String.format("%d nanos", System.nanoTime() - start);
                break;
            case MICROSECOND:
                time = String.format("%.3f micros", (System.nanoTime() - start) / 1000F);
                break;
            case MILLISECOND:
                time = String.format("%.3f millis", (System.nanoTime() - start) / 1_000_000F);
                break;
            case SECOND:
                time = String.format("%.3f s", (System.nanoTime() - start) / 1_000_000_000F);
                break;
            default:
                time = "#undef";
        }
        return time;
    }
}
