package com.alex.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@WebServlet(name="get_favicon", urlPatterns = "/favicon.ico")
public class GetFavicon extends HttpServlet {

    private final int BUFFER_SIZE = 4096;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream is = GetFavicon.class.getResourceAsStream("/favicon.ico");
        final byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while ((n = is.read(buffer)) > 0) {
            response.getOutputStream().write(buffer, 0, n);
        }
    }
}