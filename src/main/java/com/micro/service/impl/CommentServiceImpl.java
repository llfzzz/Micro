package com.micro.service.impl;

import com.micro.dao.CommentDao;
import com.micro.dao.PostDao;
import com.micro.entity.Comment;
import com.micro.service.CommentService;

import java.util.List;

public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;
    private final PostDao postDao;

    public CommentServiceImpl(CommentDao commentDao, PostDao postDao) {
        this.commentDao = commentDao;
        this.postDao = postDao;
    }

    @Override
    public long addComment(Comment comment) {
        long id = commentDao.create(comment);
        postDao.updateCounts(comment.getPostId(), 0, 1, 0);
        return id;
    }

    @Override
    public List<Comment> getComments(long postId, int offset, int limit) {
        return commentDao.listByPost(postId, offset, limit);
    }

    @Override
    public List<Comment> getUserReplies(long userId, int offset, int limit) {
        return commentDao.listByUser(userId, offset, limit);
    }

    @Override
    public boolean deleteComment(long commentId, long operatorId) {
        // Note: Comment count update is handled by database triggers or should be done at a higher layer
        // as we cannot determine postId without an extra query
        return commentDao.softDelete(commentId, operatorId);
    }
}
