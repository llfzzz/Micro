package com.micro.servlet;

import com.micro.entity.Media;
import com.micro.entity.Post;
import com.micro.entity.User;
import com.micro.listener.AppContextListener;
import com.micro.service.AdminService;
import com.micro.service.MediaService;
import com.micro.service.PostService;
import com.micro.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles MVC style routes under /app and forwards to JSP views.
 */
@WebServlet(urlPatterns = "/app/*")
public class PageServlet extends HttpServlet {

    private transient PostService postService;
    private transient UserService userService;
    private transient MediaService mediaService;
    private transient AdminService adminService;

    @Override
    public void init() throws ServletException {
        var components = AppContextListener.getComponents(getServletContext());
        this.postService = components.postService();
        this.userService = components.userService();
        this.mediaService = components.mediaService();
        this.adminService = components.adminService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String path = normalize(req.getPathInfo());
        switch (path) {
            case "":
                resp.sendRedirect(req.getContextPath() + "/app/feed");
                break;
            case "feed":
                handleFeed(req, resp);
                break;
            case "create-post":
                requireLoginOrRedirect(req, resp, () -> forward(req, resp, "/WEB-INF/jsp/create_post.jsp"));
                break;
            case "post":
                handlePostDetail(req, resp);
                break;
            case "profile":
                handleProfile(req, resp);
                break;
            case "login":
                if (isLoggedIn(req)) {
                    resp.sendRedirect(req.getContextPath() + "/app/feed");
                } else {
                    forward(req, resp, "/WEB-INF/jsp/login.jsp");
                }
                break;
            case "register":
                if (isLoggedIn(req)) {
                    resp.sendRedirect(req.getContextPath() + "/app/feed");
                } else {
                    forward(req, resp, "/WEB-INF/jsp/register.jsp");
                }
                break;
            case "admin":
                handleAdmin(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleFeed(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Post> posts = postService.getFeed(0, 10);
        req.setAttribute("feedList", buildPostView(posts));
        req.setAttribute("feedOffset", posts.size());
        forward(req, resp, "/WEB-INF/jsp/feed.jsp");
    }

    private void handlePostDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        long postId = parseLong(req.getParameter("id"));
        if (postId <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "post id required");
            return;
        }
        Optional<Post> post = postService.findById(postId);
        if (post.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "post not found");
            return;
        }
        List<Media> mediaList = mediaService.getMediaByPost(postId);
        req.setAttribute("post", post.get());
        req.setAttribute("postMedia", mediaList);
        forward(req, resp, "/WEB-INF/jsp/post.jsp");
    }

    private void handleProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        long userId = parseLong(req.getParameter("id"));
        if (userId <= 0) {
            userId = getSessionUserId(req);
        }
        if (userId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/app/login");
            return;
        }
        Optional<User> profileUser = userService.findById(userId);
        if (profileUser.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "user not found");
            return;
        }
        List<Post> posts = postService.getByUser(userId, 0, 10);
        Map<String, Object> stats = new HashMap<>();
        stats.put("postCount", posts.size());
        stats.put("followerCount", 0);
        stats.put("followingCount", 0);
        req.setAttribute("profileUser", profileUser.get());
        req.setAttribute("profilePosts", posts);
        req.setAttribute("profileStats", stats);
        forward(req, resp, "/WEB-INF/jsp/profile.jsp");
    }

    private void handleAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Optional<User> currentUser = currentUser(req);
        if (currentUser.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/app/login");
            return;
        }
        if (!"ADMIN".equalsIgnoreCase(currentUser.get().getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "admin only");
            return;
        }
        List<User> users = adminService.listUsers(0, 10);
        List<Post> posts = adminService.listPosts(0, 10);
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", users.size());
        stats.put("postCount", posts.size());
        stats.put("commentCount", posts.stream().mapToInt(Post::getCommentCount).sum());
        req.setAttribute("adminUsers", users);
        req.setAttribute("adminPosts", posts);
        req.setAttribute("adminStats", stats);
        forward(req, resp, "/WEB-INF/jsp/admin/dashboard.jsp");
    }

    private void requireLoginOrRedirect(HttpServletRequest req, HttpServletResponse resp, Runnable action) throws IOException {
        if (!isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/app/login");
            return;
        }
        action.run();
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        return getSessionUserId(req) > 0;
    }

    private long getSessionUserId(HttpServletRequest req) {
        var session = req.getSession(false);
        if (session == null) {
            return -1;
        }
        Object userId = session.getAttribute("userId");
        return userId instanceof Long ? (Long) userId : -1;
    }

    private Optional<User> currentUser(HttpServletRequest req) {
        long userId = getSessionUserId(req);
        if (userId <= 0) {
            return Optional.empty();
        }
        return userService.findById(userId);
    }

    private List<Map<String, Object>> buildPostView(List<Post> posts) {
        Map<Long, String> usernameCache = new HashMap<>();
        return posts.stream().map(post -> {
            Map<String, Object> view = new HashMap<>();
            view.put("id", post.getId());
            view.put("contentText", post.getContentText());
            view.put("likeCount", post.getLikeCount());
            view.put("commentCount", post.getCommentCount());
            view.put("createdAt", post.getCreatedAt());
            view.put("username", usernameCache.computeIfAbsent(post.getUserId(), this::resolveUsername));
            return view;
        }).collect(Collectors.toList());
    }

    private String resolveUsername(long userId) {
        return userService.findById(userId)
                .map(User::getUsername)
                .orElse("user-" + userId);
    }

    private long parseLong(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String normalize(String pathInfo) {
        if (pathInfo == null || "/".equals(pathInfo)) {
            return "";
        }
        String path = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view) {
        try {
            req.getRequestDispatcher(view).forward(req, resp);
        } catch (ServletException | IOException e) {
            throw new IllegalStateException("Unable to forward to view " + view, e);
        }
    }
}
