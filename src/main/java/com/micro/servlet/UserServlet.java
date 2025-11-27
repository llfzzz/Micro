package com.micro.servlet;

import com.micro.entity.User;
import com.micro.listener.AppContextListener;
import com.micro.service.UserService;
import com.micro.util.FileUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(urlPatterns = "/api/users/*")
@MultipartConfig
public class UserServlet extends BaseServlet {

    private transient UserService userService;

    @Override
    public void init() throws ServletException {
        this.userService = AppContextListener.getComponents(getServletContext()).userService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            handleSearch(req, resp);
            return;
        }
        long userId = parseId(req, resp);
        if (userId < 0) {
            return;
        }
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, 4040, "User not found");
            return;
        }
        writeSuccess(resp, sanitize(user.get()));
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getParameter("q");
        if (query == null || query.isBlank()) {
            writeSuccess(resp, List.of());
            return;
        }
        var users = userService.searchUsers(query, 50);
        var dtos = users.stream().map(this::sanitize).collect(java.util.stream.Collectors.toList());
        writeSuccess(resp, dtos);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long sessionUser = requireSessionUser(req, resp);
        if (sessionUser < 0) {
            return;
        }
        long targetId = parseId(req, resp);
        if (targetId < 0) {
            return;
        }
        if (sessionUser != targetId) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, 4030, "Cannot modify other profile");
            return;
        }
        Optional<User> currentUser = userService.findById(targetId);
        if (currentUser.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, 4042, "User not found");
            return;
        }
        User payload = readJson(req, User.class);
        User merged = currentUser.get();
        if (payload.getDisplayName() != null) {
            merged.setDisplayName(payload.getDisplayName());
        }
        if (payload.getBio() != null) {
            merged.setBio(payload.getBio());
        }
        if (payload.getEmail() != null) {
            merged.setEmail(payload.getEmail());
        }
        boolean updated = userService.updateProfile(merged);
        if (!updated) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4005, "Update failed");
            return;
        }
        writeSuccess(resp, sanitize(merged));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String path = req.getPathInfo();
        if (path != null && path.endsWith("/avatar")) {
            handleAvatarUpload(req, resp);
            return;
        }
        writeError(resp, HttpServletResponse.SC_NOT_FOUND, 4041, "Unknown action");
    }

    private void handleAvatarUpload(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        long sessionUser = requireSessionUser(req, resp);
        if (sessionUser < 0) {
            return;
        }
        long targetId = parseId(req, resp);
        if (targetId < 0) {
            return;
        }
        if (sessionUser != targetId) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, 4031, "Cannot change other avatar");
            return;
        }
        Part file = req.getPart("file");
        if (file == null || file.getSize() == 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4006, "File required");
            return;
        }
        String storageRoot = AppContextListener.getFileStoragePath(getServletContext());
        String relativePath;
        try (var stream = file.getInputStream()) {
            relativePath = FileUtil.saveToStorage(stream, storageRoot, targetId, file.getSubmittedFileName());
        }
        userService.updateAvatar(targetId, relativePath);
        Map<String, Object> payload = Map.of("avatarPath", relativePath);
        writeSuccess(resp, payload);
    }

    private long parseId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4004, "User id required");
            return -1;
        }
        String[] segments = path.split("/");
        try {
            return Long.parseLong(segments[1]);
        } catch (NumberFormatException ex) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4004, "Invalid user id");
            return -1;
        }
    }

    private long requireSessionUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, 4012, "Login required");
            return -1;
        }
        return (long) session.getAttribute("userId");
    }

    private Map<String, Object> sanitize(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("username", user.getUsername());
        dto.put("email", user.getEmail());
        dto.put("displayName", user.getDisplayName());
        dto.put("bio", user.getBio());
        dto.put("avatarPath", user.getAvatarPath());
        dto.put("role", user.getRole());
        return dto;
    }
}
