package com.micro.service;

import com.micro.entity.Post;
import com.micro.entity.User;

import java.util.List;
import java.util.Map;

public interface AdminService {
    List<User> listUsers(int offset, int limit);

    List<Post> listPosts(int offset, int limit);

    boolean deletePost(long postId);

    boolean deleteComment(long commentId);

    boolean banUser(long userId, boolean banned);

    Map<String, Long> countStats();
}
