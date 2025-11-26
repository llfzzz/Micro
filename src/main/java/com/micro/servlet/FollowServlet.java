package com.micro.servlet;

import com.micro.listener.AppContextListener;
import com.micro.service.FollowService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = "/api/follows/*")
public class FollowServlet extends BaseServlet {

    private transient FollowService followService;

    @Override
    public void init() throws ServletException {
        this.followService = AppContextListener.getComponents(getServletContext()).followService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long targetId = parseTargetId(req, resp);
        if (targetId < 0) {
            return;
        }
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long actorId = requireSessionUser(req, resp);
        if (actorId < 0) {
            return;
        }
        long targetId = parseTargetId(req, resp);
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
        long targetId = parseTargetId(req, resp);
        if (targetId < 0) {
            return;
        }
        followService.unfollow(actorId, targetId);
        writeSuccess(resp, Map.of("following", false));
    }

    private long parseTargetId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4010, "需要用户ID");
            return -1;
        }
        try {
            return Long.parseLong(path.substring(1));
        } catch (NumberFormatException ex) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4010, "用户ID不合法");
            return -1;
        }
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
