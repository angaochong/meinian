package com.jkweilai.meinian.api.config.xss;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class XSSFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        XSSHttpServletRequestWrapper requestWrapper = new XSSHttpServletRequestWrapper(request);
        filterChain.doFilter(requestWrapper, servletResponse);
    }
}
