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
    public boolean deleteComment(long commentId, long operatorId) {
        boolean result = commentDao.softDelete(commentId, operatorId);
        if (result) {
            // cannot determine postId without extra query; rely on triggers or follow-up fetch in higher layer if needed
        }
        return result;
    }
}
