package com.micro.service;

import com.micro.entity.User;

import java.util.List;

public interface FollowService {
    boolean follow(long followerId, long followeeId);

    boolean unfollow(long followerId, long followeeId);

    boolean isFollowing(long followerId, long followeeId);

    long countFollowers(long userId);

    long countFollowing(long userId);

    List<User> listFollowers(long userId, int offset, int limit);

    List<User> listFollowing(long userId, int offset, int limit);
}
