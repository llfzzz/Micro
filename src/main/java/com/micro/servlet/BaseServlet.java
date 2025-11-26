package com.micro.servlet;

import com.micro.util.JsonUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides helpers for consistent JSON responses.
 */
public abstract class BaseServlet extends HttpServlet {

    protected void writeSuccess(HttpServletResponse resp, Object data) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("data", data);
        writeJson(resp, payload, HttpServletResponse.SC_OK);
    }

    protected void writeError(HttpServletResponse resp, int status, int code, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", false);
        payload.put("error", error);
        writeJson(resp, payload, status);
    }

    protected <T> T readJson(HttpServletRequest request, Class<T> type) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining());
        return JsonUtil.fromJson(body, type);
    }

    private void writeJson(HttpServletResponse resp, Object payload, int status) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(JsonUtil.toJson(payload));
    }
}
