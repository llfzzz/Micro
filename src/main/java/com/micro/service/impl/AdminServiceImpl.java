package com.micro.service.impl;

import com.micro.dao.CommentDao;
import com.micro.dao.PostDao;
import com.micro.dao.UserDao;
import com.micro.entity.Post;
import com.micro.entity.User;
import com.micro.service.AdminService;

import java.util.List;
import java.util.Map;

public class AdminServiceImpl implements AdminService {

    private final UserDao userDao;
    private final PostDao postDao;
    private final CommentDao commentDao;

    public AdminServiceImpl(UserDao userDao, PostDao postDao, CommentDao commentDao) {
        this.userDao = userDao;
        this.postDao = postDao;
        this.commentDao = commentDao;
    }

    @Override
    public List<User> listUsers(int offset, int limit) {
        return searchUsers(null, null, null, offset, limit);
    }

    @Override
    public List<User> searchUsers(String keyword, String role, Boolean banned, int offset, int limit) {
        return userDao.search(keyword, normalize(role), banned, offset, limit);
    }

    @Override
    public long countUsers(String keyword, String role, Boolean banned) {
        return userDao.count(keyword, normalize(role), banned);
    }

    @Override
    public List<Post> listPosts(int offset, int limit) {
        return searchPosts(null, null, null, Boolean.FALSE, offset, limit);
    }

    @Override
    public List<Post> searchPosts(Long userId, String keyword, String visibility, Boolean deleted, int offset, int limit) {
        return postDao.adminSearch(userId, keyword, normalize(visibility), deleted, offset, limit);
    }

    @Override
    public long countPosts(Long userId, String keyword, String visibility, Boolean deleted) {
        return postDao.countAdmin(userId, keyword, normalize(visibility), deleted);
    }

    @Override
    public boolean deletePost(long postId) {
        return postDao.delete(postId);
    }

    @Override
    public boolean deleteComment(long commentId) {
        return commentDao.softDelete(commentId, 0L);
    }

    @Override
    public boolean banUser(long userId, boolean banned) {
        return userDao.banUser(userId, banned);
    }

    @Override
    public Map<String, Long> countStats() {
        return Map.of(
                "users", userDao.countAll(),
                "posts", postDao.countAll(),
                "comments", commentDao.countAll()
        );
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
