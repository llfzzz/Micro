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
        return userDao.list(offset, limit);
    }

    @Override
    public List<Post> listPosts(int offset, int limit) {
        return postDao.listFeed(offset, limit);
    }

    @Override
    public boolean deletePost(long postId) {
        return postDao.softDelete(postId, 0L);
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
}
