package com.micro.service;

public interface FollowService {
    boolean follow(long followerId, long followeeId);

    boolean unfollow(long followerId, long followeeId);

    boolean isFollowing(long followerId, long followeeId);

    long countFollowers(long userId);

    long countFollowing(long userId);
}
