package com.micro.service;

import com.micro.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostService {
    long createPost(Post post);

    Optional<Post> findById(long id);

    List<Post> getFeed(int offset, int limit);

    List<Post> getByUser(long userId, int offset, int limit);

    boolean deletePost(long postId, long operatorId);

    boolean toggleLike(long postId, long userId);

    List<Post> search(String keyword, int offset, int limit);

    List<String> searchTags(String query, int limit);

    boolean isLiked(long postId, long userId);

    List<Post> getLikedPosts(long userId, int offset, int limit);
}
