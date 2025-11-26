package com.micro.dao;

public interface LikeDao {
    boolean addLike(long postId, long userId);

    boolean removeLike(long postId, long userId);

    boolean exists(long postId, long userId);
}
