package com.micro.servlet;

import com.micro.entity.Comment;
import com.micro.entity.Media;
import com.micro.entity.Post;
import com.micro.entity.User;
import com.micro.listener.AppContextListener;
import com.micro.service.AdminService;
import com.micro.service.CommentService;
import com.micro.service.FollowService;
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
	private transient FollowService followService;
	private transient CommentService commentService;

	@Override
	public void init() throws ServletException {
		var components = AppContextListener.getComponents(getServletContext());
		this.postService = components.postService();
		this.userService = components.userService();
		this.mediaService = components.mediaService();
		this.adminService = components.adminService();
		this.followService = components.followService();
		this.commentService = components.commentService();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String path = normalize(req.getPathInfo());
		if (path.isEmpty()) {
			resp.sendRedirect(req.getContextPath() + "/app/feed");
			return;
		}

		switch (path) {
			case "feed":
				handleFeed(req, resp);
				return;
			case "create-post":
				requireLoginOrRedirect(req, resp, () -> forward(req, resp, "/WEB-INF/jsp/create_post.jsp"));
				return;
			case "post":
				handlePostDetail(req, resp);
				return;
			case "profile":
				handleProfile(req, resp);
				return;
			case "login":
				if (isLoggedIn(req)) {
					resp.sendRedirect(req.getContextPath() + "/app/feed");
				} else {
					forward(req, resp, "/WEB-INF/jsp/login.jsp");
				}
				return;
			case "register":
				if (isLoggedIn(req)) {
					resp.sendRedirect(req.getContextPath() + "/app/feed");
				} else {
					forward(req, resp, "/WEB-INF/jsp/register.jsp");
				}
				return;
			case "admin":
				handleAdmin(req, resp);
				return;
			case "follows":
				handleFollows(req, resp);
				return;
			case "search":
				handleSearch(req, resp);
				return;
			default:
				break;
		}

		if (path.startsWith("profile/")) {
			String suffix = path.substring("profile/".length());
			if ("edit".equals(suffix)) {
				handleProfileEdit(req, resp);
				return;
			}
			if ("followers".equals(suffix) || "following".equals(suffix)) {
				redirectToFollowPage(req, resp, suffix);
				return;
			}
		}

		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void handleFeed(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<Post> posts = postService.getFeed(0, 10);
		long viewerId = getSessionUserId(req);
		req.setAttribute("feedList", buildPostView(posts, viewerId));
		req.setAttribute("feedOffset", posts.size());
		forward(req, resp, "/WEB-INF/jsp/feed.jsp");
	}

	private void handlePostDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		long postId = parseLong(req.getParameter("id"));
		if (postId <= 0) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "post id required");
			return;
		}
		Optional<Post> postOpt = postService.findById(postId);
		if (postOpt.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "post not found");
			return;
		}
		Post post = postOpt.get();
		List<Media> mediaList = mediaService.getMediaByPost(postId);
		
		long viewerId = getSessionUserId(req);
		boolean isLiked = viewerId > 0 && postService.isLiked(postId, viewerId);

		// Build view map with user info
		Map<String, Object> view = new HashMap<>();
		view.put("id", post.getId());
		view.put("userId", post.getUserId());
		view.put("contentText", post.getContentText());
		view.put("likeCount", post.getLikeCount());
		view.put("commentCount", post.getCommentCount());
		view.put("createdAt", post.getCreatedAt());
		view.put("mediaMetaJson", post.getMediaMetaJson());
		view.put("liked", isLiked);
		
		Optional<User> userOpt = userService.findById(post.getUserId());
		view.put("username", userOpt.map(User::getUsername).orElse("user-" + post.getUserId()));
		view.put("displayName", userOpt.map(User::getDisplayName).orElse(null));
		view.put("avatarPath", userOpt.map(User::getAvatarPath).orElse(null));

		req.setAttribute("post", view);
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

		String tab = req.getParameter("tab");
		if (tab == null || tab.isEmpty()) {
			tab = "posts";
		}

		long viewerId = getSessionUserId(req);
		
		if ("likes".equals(tab)) {
			List<Post> posts = postService.getLikedPosts(userId, 0, 20);
			req.setAttribute("profilePosts", buildPostView(posts, viewerId));
		} else if ("replies".equals(tab)) {
			List<Comment> comments = commentService.getUserReplies(userId, 0, 20);
			req.setAttribute("profileReplies", buildReplyViewWithStats(comments, viewerId));
		} else {
			// Default: posts
			List<Post> posts = postService.getByUser(userId, 0, 20);
			req.setAttribute("profilePosts", buildPostView(posts, viewerId));
		}

		Map<String, Object> stats = new HashMap<>();
		stats.put("postCount", postService.countByUser(userId));
		stats.put("followerCount", followService.countFollowers(userId));
		stats.put("followingCount", followService.countFollowing(userId));
		
		boolean isOwner = viewerId > 0 && viewerId == userId;
		boolean isFollowing = !isOwner && viewerId > 0 && followService.isFollowing(viewerId, userId);
		req.setAttribute("profileUser", profileUser.get());
		req.setAttribute("profileStats", stats);
		req.setAttribute("currentTab", tab);
		req.setAttribute("isOwner", isOwner);
		req.setAttribute("isFollowing", isFollowing);
		forward(req, resp, "/WEB-INF/jsp/profile.jsp");
	}

	private List<Map<String, Object>> buildReplyViewWithStats(List<Comment> comments, long viewerId) {
		return comments.stream().map(comment -> {
			Map<String, Object> view = new HashMap<>();
			view.put("id", comment.getId());
			view.put("content", comment.getContent());
			view.put("createdAt", comment.getCreatedAt());

			// Parent Post Info
			Optional<Post> postOpt = postService.findById(comment.getPostId());
			if (postOpt.isPresent()) {
				Post post = postOpt.get();
				view.put("postId", post.getId());
				view.put("postContent", post.getContentText());
				view.put("postMediaMetaJson", post.getMediaMetaJson());
				view.put("postLikeCount", post.getLikeCount());
				view.put("postCommentCount", post.getCommentCount());
				view.put("postLiked", viewerId > 0 && postService.isLiked(post.getId(), viewerId));
				view.put("postCreatedAt", post.getCreatedAt());

				// Post Author Info
				Optional<User> authorOpt = userService.findById(post.getUserId());
				view.put("postAuthorId", post.getUserId());
				view.put("postAuthorUsername", authorOpt.map(User::getUsername).orElse("unknown"));
				view.put("postAuthorDisplayName", authorOpt.map(User::getDisplayName).orElse("Unknown"));
				view.put("postAuthorAvatar", authorOpt.map(User::getAvatarPath).orElse(null));
			}
			return view;
		}).collect(Collectors.toList());
	}

	private void handleProfileEdit(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		long userId = getSessionUserId(req);
		if (userId <= 0) {
			resp.sendRedirect(req.getContextPath() + "/app/login");
			return;
		}
		Optional<User> profileUser = userService.findById(userId);
		if (profileUser.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "user not found");
			return;
		}
		req.setAttribute("profileUser", profileUser.get());
		forward(req, resp, "/WEB-INF/jsp/profile_edit.jsp");
	}

	private void redirectToFollowPage(HttpServletRequest req, HttpServletResponse resp, String suffix) throws IOException {
		String targetType = "following".equals(suffix) ? "following" : "followers";
		String targetId = Optional.ofNullable(req.getParameter("id"))
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.orElse("");
		StringBuilder redirectUrl = new StringBuilder(String.format("%s/app/follows?type=%s", req.getContextPath(), targetType));
		if (!targetId.isEmpty()) {
			redirectUrl.append("&id=").append(targetId);
		}
		resp.sendRedirect(redirectUrl.toString());
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
		Map<String, Long> stats = adminService.countStats();
		req.setAttribute("adminUsers", users);
		req.setAttribute("adminPosts", posts);
		req.setAttribute("adminStats", stats);
		forward(req, resp, "/WEB-INF/jsp/admin/dashboard.jsp");
	}

	private void handleFollows(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		long userId = parseLong(req.getParameter("id"));
		if (userId <= 0) {
			userId = getSessionUserId(req);
		}
		if (userId <= 0) {
			resp.sendRedirect(req.getContextPath() + "/app/login");
			return;
		}
		Optional<User> followUser = userService.findById(userId);
		if (followUser.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "user not found");
			return;
		}
		String type = req.getParameter("type");
		if (!"following".equals(type) && !"followers".equals(type)) {
			type = "followers";
		}
		req.setAttribute("followUser", followUser.get());
		req.setAttribute("followType", type);
		req.setAttribute("followerCount", followService.countFollowers(userId));
		req.setAttribute("followingCount", followService.countFollowing(userId));
		forward(req, resp, "/WEB-INF/jsp/follow_list.jsp");
	}

	private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String query = req.getParameter("q");
		if (query == null) {
			query = "";
		}
		query = query.trim();
		req.setAttribute("searchQuery", query);

		String type = req.getParameter("type");
		if ("user_posts".equals(type)) {
			long uid = parseLong(req.getParameter("uid"));
			if (uid > 0) {
				List<Post> posts = postService.searchByUser(uid, query, 0, 20);
				req.setAttribute("searchType", "posts");
				long viewerId = getSessionUserId(req);
				req.setAttribute("feedList", buildPostView(posts, viewerId));

				Optional<User> targetUser = userService.findById(uid);
				targetUser.ifPresent(user -> req.setAttribute("searchTargetUser", user));

				forward(req, resp, "/WEB-INF/jsp/search.jsp");
				return;
			}
		}

		if (query.startsWith("@")) {
			// Search users
			String keyword = query.substring(1);
			List<User> users = userService.searchUsers(keyword, 20);
			req.setAttribute("searchType", "users");
			req.setAttribute("userList", users);
		} else {
			// Search posts (including #tags)
			// Current implementation uses LIKE %keyword%, which works for both plain text and #tag searches
			// If user types "#java", it searches for "%#java%" in post content
			List<Post> posts = postService.search(query, 0, 20);
			req.setAttribute("searchType", "posts");
			long viewerId = getSessionUserId(req);
			req.setAttribute("feedList", buildPostView(posts, viewerId));
		}
		forward(req, resp, "/WEB-INF/jsp/search.jsp");
	}

	private void requireLoginOrRedirect(HttpServletRequest req, HttpServletResponse resp, ServletAction action) throws IOException, ServletException {
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

	private List<Map<String, Object>> buildPostView(List<Post> posts, long viewerId) {
		return posts.stream().map(post -> {
			Map<String, Object> view = new HashMap<>();
			view.put("id", post.getId());
			view.put("userId", post.getUserId());
			view.put("contentText", post.getContentText());
			view.put("likeCount", post.getLikeCount());
			view.put("commentCount", post.getCommentCount());
			view.put("createdAt", post.getCreatedAt());
			view.put("mediaMetaJson", post.getMediaMetaJson());
			view.put("liked", viewerId > 0 && postService.isLiked(post.getId(), viewerId));
			Optional<User> userOpt = userService.findById(post.getUserId());
			view.put("username", userOpt.map(User::getUsername).orElse("user-" + post.getUserId()));
			view.put("displayName", userOpt.map(User::getDisplayName).orElse(null));
			view.put("avatarPath", userOpt.map(User::getAvatarPath).orElse(null));
			return view;
		}).collect(Collectors.toList());
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

	private void forward(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
		req.getRequestDispatcher(jsp).forward(req, resp);
	}

	@FunctionalInterface
	private interface ServletAction {
		void run() throws ServletException, IOException;
	}
}
