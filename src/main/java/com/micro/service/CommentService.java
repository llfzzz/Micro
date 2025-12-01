package com.micro.service;

import com.micro.entity.Comment;

import java.util.List;

public interface CommentService {
    long addComment(Comment comment);

    List<Comment> getComments(long postId, int offset, int limit);

    List<Comment> getUserReplies(long userId, int offset, int limit);

    boolean deleteComment(long commentId, long operatorId);
}
