package com.alex.web;

import com.alex.utils.Row;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "InitServlet", urlPatterns = "/index")
public class InitServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doing(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doing(request, response);
    }

    private void doing(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Load from DB and create index
        List<Row> list; list = new ArrayList<>();
        list.add(new Row("prefix1", "name1", 1));
        list.add(new Row("prefix2", "name2", 2));
        list.add(new Row("prefix3", "name3", 3));
        list.add(new Row("prefix4", "name4", 4));
        request.setAttribute("list", list);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
