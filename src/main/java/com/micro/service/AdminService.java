package com.micro.service;

import com.micro.entity.Post;
import com.micro.entity.User;

import java.util.List;
import java.util.Map;

public interface AdminService {
    List<User> listUsers(int offset, int limit);

    List<User> searchUsers(String keyword, String role, Boolean banned, int offset, int limit);

    long countUsers(String keyword, String role, Boolean banned);

    List<Post> listPosts(int offset, int limit);

    List<Post> searchPosts(Long userId, String keyword, String visibility, Boolean deleted, int offset, int limit);

    long countPosts(Long userId, String keyword, String visibility, Boolean deleted);

    boolean deletePost(long postId);

    boolean deleteComment(long commentId);

    boolean banUser(long userId, boolean banned);

    Map<String, Long> countStats();
}
