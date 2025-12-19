package com.micro.servlet;

import com.micro.entity.User;
import com.micro.listener.AppContextListener;
import com.micro.service.FollowService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/api/follows/*")
public class FollowServlet extends BaseServlet {

    private transient FollowService followService;

    @Override
    public void init() throws ServletException {
        this.followService = AppContextListener.getComponents(getServletContext()).followService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] segments = splitPath(req.getPathInfo());
        long targetId = resolveTargetId(segments, req, resp);
        if (targetId < 0) {
            return;
        }
        if (segments.length == 1) {
            respondWithCounts(req, resp, targetId);
        } else if (segments.length == 2) {
            respondWithUserList(req, resp, targetId, segments[1]);
        } else {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4011, "未知的关注操作");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long actorId = requireSessionUser(req, resp);
        if (actorId < 0) {
            return;
        }
        String[] segments = splitPath(req.getPathInfo());
        long targetId = resolveTargetId(segments, req, resp);
        if (targetId < 0) {
            return;
        }
        if (actorId == targetId) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4009, "无法关注自己");
            return;
        }
        boolean followed = followService.follow(actorId, targetId);
        writeSuccess(resp, Map.of("following", followed || followService.isFollowing(actorId, targetId)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long actorId = requireSessionUser(req, resp);
        if (actorId < 0) {
            return;
        }
        String[] segments = splitPath(req.getPathInfo());
        long targetId = resolveTargetId(segments, req, resp);
        if (targetId < 0) {
            return;
        }
        followService.unfollow(actorId, targetId);
        writeSuccess(resp, Map.of("following", false));
    }

    private void respondWithCounts(HttpServletRequest req, HttpServletResponse resp, long targetId) throws IOException {
        long viewerId = getSessionUserId(req);
        boolean isFollowing = viewerId > 0 && followService.isFollowing(viewerId, targetId);
        long followers = followService.countFollowers(targetId);
        long following = followService.countFollowing(targetId);
        writeSuccess(resp, Map.of(
                "followers", followers,
                "following", following,
                "followingState", isFollowing
        ));
    }

    private void respondWithUserList(HttpServletRequest req, HttpServletResponse resp, long targetId, String type) throws IOException {
        int offset = Math.max(0, parseInt(req.getParameter("offset"), 0));
        int limit = clampLimit(parseInt(req.getParameter("limit"), 20));
        List<User> users;
        long total;
        switch (type) {
            case "followers":
                users = followService.listFollowers(targetId, offset, limit);
                total = followService.countFollowers(targetId);
                break;
            case "following":
                users = followService.listFollowing(targetId, offset, limit);
                total = followService.countFollowing(targetId);
                break;
            default:
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4011, "未知的关注列表类型");
                return;
        }
        List<Map<String, Object>> items = users.stream()
                .map(this::summarizeUser)
                .toList();
        writeSuccess(resp, Map.of(
                "items", items,
                "offset", offset,
                "limit", limit,
                "total", total,
                "hasMore", offset + users.size() < total
        ));
    }

    private long parseUserId(String raw, HttpServletResponse resp) throws IOException {
        if (raw == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4010, "需要用户ID");
            return -1;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4010, "用户ID不合法");
            return -1;
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private int clampLimit(int requested) {
        if (requested <= 0) {
            return 20;
        }
        return Math.min(requested, 50);
    }

    private Map<String, Object> summarizeUser(User user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "avatarPath", user.getAvatarPath(),
                "bio", user.getBio(),
                "role", user.getRole()
        );
    }

    private String[] splitPath(String pathInfo) {
        if (pathInfo == null) {
            return new String[0];
        }
        return Arrays.stream(pathInfo.split("/"))
                .filter(segment -> !segment.isBlank())
                .toArray(String[]::new);
    }

    private long resolveTargetId(String[] segments, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (segments.length > 0) {
            return parseUserId(segments[0], resp);
        }
        return parseUserId(req.getParameter("userId"), resp);
    }

    private long requireSessionUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long userId = getSessionUserId(req);
        if (userId <= 0) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, 4013, "需要登录");
            return -1;
        }
        return userId;
    }

    private long getSessionUserId(HttpServletRequest req) {
        var session = req.getSession(false);
        if (session == null) {
            return -1;
        }
        Object attr = session.getAttribute("userId");
        return attr instanceof Long ? (Long) attr : -1;
    }
}
