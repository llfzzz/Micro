package com.micro.dao;

import com.micro.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostDao {
    long create(Post post);

    Optional<Post> findById(long id);

    List<Post> listByUser(long userId, int offset, int limit);

    List<Post> listFeed(int offset, int limit);

    boolean softDelete(long postId, long operatorId);

    boolean updateCounts(long postId, int likeDelta, int commentDelta, int forwardDelta);

    long countAll();
}
