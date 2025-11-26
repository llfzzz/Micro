package com.micro.servlet;

import com.micro.entity.Post;
import com.micro.entity.User;
import com.micro.listener.AppContextListener;
import com.micro.service.AdminService;
import com.micro.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(urlPatterns = "/api/admin/*")
public class AdminServlet extends BaseServlet {

    private transient AdminService adminService;
    private transient UserService userService;

    @Override
    public void init() throws ServletException {
        var components = AppContextListener.getComponents(getServletContext());
        this.adminService = components.adminService();
        this.userService = components.userService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<User> admin = requireAdmin(req, resp);
        if (admin.isEmpty()) {
            return;
        }
        String path = req.getPathInfo();
        int offset = parseInt(req.getParameter("offset"), 0);
        int limit = parseInt(req.getParameter("limit"), 50);
        if (path != null && path.startsWith("/users")) {
            List<User> users = adminService.listUsers(offset, limit);
            writeSuccess(resp, Map.of("items", users));
        } else if (path != null && path.startsWith("/posts")) {
            List<Post> posts = adminService.listPosts(offset, limit);
            writeSuccess(resp, Map.of("items", posts));
        } else {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, 4044, "Unknown admin endpoint");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<User> admin = requireAdmin(req, resp);
        if (admin.isEmpty()) {
            return;
        }
        String path = req.getPathInfo();
        if (path != null && path.endsWith("/ban")) {
            long userId = extractId(path);
            boolean banned = Boolean.parseBoolean(req.getParameter("banned"));
            boolean result = adminService.banUser(userId, banned);
            writeSuccess(resp, Map.of("banned", result));
        } else {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, 4045, "Unknown admin action");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<User> admin = requireAdmin(req, resp);
        if (admin.isEmpty()) {
            return;
        }
        String path = req.getPathInfo();
        if (path != null && path.startsWith("/posts")) {
            long postId = extractId(path);
            boolean result = adminService.deletePost(postId);
            writeSuccess(resp, Map.of("deleted", result));
        } else if (path != null && path.startsWith("/comments")) {
            long commentId = extractId(path);
            boolean result = adminService.deleteComment(commentId);
            writeSuccess(resp, Map.of("deleted", result));
        } else {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, 4046, "Unknown admin delete endpoint");
        }
    }

    private Optional<User> requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, 4017, "Login required");
            return Optional.empty();
        }
        long userId = (long) session.getAttribute("userId");
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty() || !"ADMIN".equals(user.get().getRole())) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, 4035, "Admin only");
            return Optional.empty();
        }
        return user;
    }

    private int parseInt(String value, int defaultVal) {
        try {
            return value == null ? defaultVal : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultVal;
        }
    }

    private long extractId(String path) {
        if (path == null) {
            return -1;
        }
        String[] parts = path.split("/");
        for (String part : parts) {
            if (!part.isBlank() && !part.equals("ban")) {
                try {
                    return Long.parseLong(part);
                } catch (NumberFormatException ignored) {
                    // continue
                }
            }
        }
        return -1;
    }
}
