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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(name="DropDB", urlPatterns="/dropdb")
public class DropDB extends HttpServlet {

    private Logger log = Logger.getLogger(DropDB.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("Drop tables");
        try (Connection connection = Utils.getNewConnection();
             PreparedStatement pS1 = connection.prepareStatement("DELETE FROM word_count;");
             PreparedStatement pS2 = connection.prepareStatement("DELETE FROM file_input;")) {

            pS1.execute();
            pS2.execute();
        } catch (SQLException e) {
            log.warning("Can't drop tables word_count, file_input");
        }

        getServletContext().setAttribute("cache", new HashMap<String, List<Row>>());

        request.setAttribute("count", Utils.getCountRowDB());
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
