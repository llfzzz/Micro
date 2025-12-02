package com.micro.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = "/api/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestUri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = requestUri.substring(contextPath.length());
        String method = httpRequest.getMethod();

        // 1. Allow Auth endpoints
        if (relativePath.startsWith("/api/auth")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Allow public GET endpoints
        if ("GET".equalsIgnoreCase(method)) {
            // Allow avatar and banner images (e.g. /api/users/123/avatar)
            if (relativePath.matches("^/api/users/\\d+/(avatar|banner)$")) {
                chain.doFilter(request, response);
                return;
            }
            // Allow viewing user profiles (e.g. /api/users/123)
            if (relativePath.matches("^/api/users/\\d+$")) {
                chain.doFilter(request, response);
                return;
            }
            // Allow searching users
            if (relativePath.startsWith("/api/users")) {
                chain.doFilter(request, response);
                return;
            }
            // Allow viewing posts and comments
            if (relativePath.startsWith("/api/posts") || relativePath.startsWith("/api/comments")) {
                chain.doFilter(request, response);
                return;
            }
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"success\":false,\"error\":{\"code\":401,\"message\":\"UNAUTHORIZED\"}} ");
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
