package com.micro.dao;

import com.micro.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostDao {
    long create(Post post);

    Optional<Post> findById(long id);

    List<Post> listByUser(long userId, int offset, int limit);

    List<Post> listByIds(List<Long> ids);

    List<Post> listFeed(int offset, int limit);

    List<Post> adminSearch(Long userId, String keyword, String visibility, Boolean deleted, int offset, int limit);

    long countAdmin(Long userId, String keyword, String visibility, Boolean deleted);

    boolean delete(long postId, long operatorId);

    boolean delete(long postId);

    boolean updateCounts(long postId, int likeDelta, int commentDelta, int forwardDelta);

    long countAll();

    List<String> listContentWithTag(String tagPrefix, int limit);
}
