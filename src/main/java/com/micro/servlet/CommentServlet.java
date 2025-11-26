package com.micro.servlet;

import com.micro.entity.Comment;
import com.micro.listener.AppContextListener;
import com.micro.service.CommentService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/api/comments/*")
public class CommentServlet extends BaseServlet {

    private transient CommentService commentService;

    @Override
    public void init() throws ServletException {
        this.commentService = AppContextListener.getComponents(getServletContext()).commentService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long postId = parseLong(req.getParameter("postId"));
        int offset = parseInt(req.getParameter("offset"), 0);
        int limit = parseInt(req.getParameter("limit"), 20);
        if (postId <= 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4009, "postId required");
            return;
        }
        List<Comment> comments = commentService.getComments(postId, offset, limit);
        writeSuccess(resp, Map.of("items", comments, "offset", offset, "limit", limit));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long userId = requireSessionUser(req, resp);
        if (userId < 0) {
            return;
        }
        Comment payload = readJson(req, Comment.class);
        payload.setUserId(userId);
        if (payload.getPostId() <= 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4010, "postId required");
            return;
        }
        long id = commentService.addComment(payload);
        payload.setId(id);
        writeSuccess(resp, payload);
    }

    private long requireSessionUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, 4013, "Login required");
            return -1;
        }
        return (long) session.getAttribute("userId");
    }

    private long parseLong(String value) {
        try {
            return value == null ? -1 : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private int parseInt(String value, int defaultVal) {
        try {
            return value == null ? defaultVal : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultVal;
        }
    }
}
