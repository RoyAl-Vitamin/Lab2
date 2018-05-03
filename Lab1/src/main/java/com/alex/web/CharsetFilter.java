package com.alex.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(urlPatterns = "/*", initParams = {
        @WebInitParam(name = "charset", value = "UTF-8") })
public class CharsetFilter implements Filter {

    private Logger log = Logger.getLogger(CharsetFilter.class.getName());

    private String encoding;

    @Override
    public void init(FilterConfig config) {
        encoding = config.getInitParameter("charset");
        if (encoding == null) encoding = "UTF-8";
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        request.setCharacterEncoding(encoding);
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        filterChain.doFilter(request, response);
    }
}
