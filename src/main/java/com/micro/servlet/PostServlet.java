package com.micro.servlet;

import com.micro.entity.Post;
import com.micro.entity.User;
import com.micro.service.UserService;
import com.micro.listener.AppContextListener;
import com.micro.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(urlPatterns = "/api/posts/*")
public class PostServlet extends BaseServlet {

    private transient PostService postService;
    private transient UserService userService;

    @Override
    public void init() throws ServletException {
        var components = AppContextListener.getComponents(getServletContext());
        this.postService = components.postService();
        this.userService = components.userService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            handleFeed(req, resp);
        } else if (path.endsWith("/tags")) {
            handleTagSearch(req, resp);
        } else {
            long postId = extractId(path);
            if (postId < 0) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4007, "Invalid post id");
                return;
            }
            Optional<Post> post = postService.findById(postId);
            if (post.isEmpty()) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, 4042, "Post not found");
                return;
            }
            writeSuccess(resp, post.get());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path != null && path.endsWith("/like")) {
            handleToggleLike(req, resp, path);
            return;
        }
        long userId = requireSessionUser(req, resp);
        if (userId < 0) {
            return;
        }
        Post payload = readJson(req, Post.class);
        payload.setUserId(userId);
        long postId = postService.createPost(payload);
        payload.setId(postId);
        writeSuccess(resp, payload);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long userId = requireSessionUser(req, resp);
        if (userId < 0) {
            return;
        }
        String path = req.getPathInfo();
        long postId = extractId(path);
        if (postId < 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4008, "Invalid post id");
            return;
        }
        boolean deleted = postService.deletePost(postId, userId);
        if (!deleted) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, 4043, "Delete failed");
            return;
        }
        writeSuccess(resp, Map.of("postId", postId));
    }

    private void handleFeed(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int offset = parseInt(req.getParameter("offset"), 0);
        int limit = parseInt(req.getParameter("limit"), 20);
        String userIdParam = req.getParameter("userId");
        List<Post> posts;
        if (userIdParam != null) {
            long userId = Long.parseLong(userIdParam);
            posts = postService.getByUser(userId, offset, limit);
        } else {
            posts = postService.getFeed(offset, limit);
        }

        long viewerId = -1;
        var session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            viewerId = (long) session.getAttribute("userId");
        }
        final long currentUserId = viewerId;

        // map posts to lightweight view objects including username and avatarPath
        List<Map<String, Object>> items = posts.stream().map(post -> {
            Map<String, Object> v = new HashMap<>();
            v.put("id", post.getId());
            v.put("contentText", post.getContentText());
            v.put("likeCount", post.getLikeCount());
            v.put("commentCount", post.getCommentCount());
            v.put("createdAt", post.getCreatedAt());
            v.put("mediaMetaJson", post.getMediaMetaJson());
            v.put("liked", currentUserId > 0 && postService.isLiked(post.getId(), currentUserId));
            // resolve username and avatar path
            userService.findById(post.getUserId()).ifPresentOrElse((User u) -> {
                v.put("username", u.getUsername());
                v.put("displayName", u.getDisplayName());
                v.put("avatarPath", u.getAvatarPath());
            }, () -> {
                v.put("username", "user-" + post.getUserId());
                v.put("displayName", null);
                v.put("avatarPath", null);
            });
            return v;
        }).toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("items", items);
        payload.put("offset", offset);
        payload.put("limit", limit);
        writeSuccess(resp, payload);
    }

    private void handleToggleLike(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        long userId = requireSessionUser(req, resp);
        if (userId < 0) {
            return;
        }
        String trimmed = path.substring(0, path.length() - "/like".length());
        long postId = extractId(trimmed);
        if (postId < 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 4015, "Invalid post id");
            return;
        }
        boolean result = postService.toggleLike(postId, userId);
        
        // Fetch updated count
        Optional<Post> updatedPost = postService.findById(postId);
        int newCount = updatedPost.map(Post::getLikeCount).orElse(0);
        
        writeSuccess(resp, Map.of("liked", result, "likeCount", newCount));
    }

    private void handleTagSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getParameter("q");
        if (query == null || query.isBlank()) {
            writeSuccess(resp, List.of());
            return;
        }
        List<String> tags = postService.searchTags(query, 50);
        writeSuccess(resp, tags);
    }

    private long requireSessionUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, 4014, "Login required");
            return -1;
        }
        return (long) session.getAttribute("userId");
    }

    private int parseInt(String val, int defaultVal) {
        try {
            return val == null ? defaultVal : Integer.parseInt(val);
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
            if (!part.isBlank()) {
                try {
                    return Long.parseLong(part);
                } catch (NumberFormatException ex) {
                    return -1;
                }
            }
        }
        return -1;
    }
}
