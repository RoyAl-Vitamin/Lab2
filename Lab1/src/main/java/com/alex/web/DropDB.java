package com.alex.web;

import com.alex.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet(name="DropDB", urlPatterns="/dropdb")
public class DropDB extends HttpServlet {

    private Logger log = Logger.getLogger(DropDB.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("Drop tables");
        Connection connection = Utils.getConnection();
        try {
            connection.prepareStatement("DELETE FROM word_count;").execute();
            connection.prepareStatement("DELETE FROM file_input;").execute();
        } catch (SQLException e) {
            log.warning("Can't drop tables word_count, file_input");
        }

        request.setAttribute("count", Utils.getCountRowDB());
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
