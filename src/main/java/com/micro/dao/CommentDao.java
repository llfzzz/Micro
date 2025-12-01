package com.micro.dao;

import com.micro.entity.Comment;

import java.util.List;

public interface CommentDao {
    long create(Comment comment);

    List<Comment> listByPost(long postId, int offset, int limit);

    List<Comment> listByUser(long userId, int offset, int limit);

    boolean softDelete(long commentId, long operatorId);

    long countAll();
}
