package com.micro.servlet;

import com.micro.entity.User;
import com.micro.listener.AppContextListener;
import com.micro.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = "/api/auth/*")
public class AuthServlet extends BaseServlet {

    private transient AuthService authService;

    @Override
    public void init() throws ServletException {
        var components = AppContextListener.getComponents(getServletContext());
        this.authService = components.authService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || path.isEmpty() || "/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/register".equals(path)) {
            handleRegister(req, resp);
        } else if ("/logout".equals(path)) {
            handleLogout(req, resp);
        } else {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, 404, "Unknown auth endpoint");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RegisterPayload payload = readJson(req, RegisterPayload.class);
        if (payload.username == null || payload.password == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4001, "Username and password required");
            return;
        }
        User user = new User();
        user.setUsername(payload.username);
        user.setEmail(payload.email);
        user.setDisplayName(payload.displayName);
        long userId = authService.register(user, payload.password);
        user.setId(userId);
        req.getSession(true).setAttribute("userId", userId);
        writeSuccess(resp, sanitize(user));
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginPayload payload = readJson(req, LoginPayload.class);
        if (payload.identifier == null || payload.password == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4002, "Identifier and password required");
            return;
        }
        var userOpt = authService.login(payload.identifier, payload.password);
        if (userOpt.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, 4011, "Invalid credentials");
            return;
        }
        User user = userOpt.get();
        req.getSession(true).setAttribute("userId", user.getId());
        writeSuccess(resp, sanitize(user));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        writeSuccess(resp, Map.of("message", "logged_out"));
    }

    private Map<String, Object> sanitize(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("username", user.getUsername());
        dto.put("email", user.getEmail());
        dto.put("displayName", user.getDisplayName());
        dto.put("role", user.getRole());
        dto.put("avatarPath", user.getAvatarPath());
        return dto;
    }

    private static final class LoginPayload {
        public String identifier;
        public String password;
    }

    private static final class RegisterPayload {
        public String username;
        public String password;
        public String email;
        public String displayName;
    }
}
