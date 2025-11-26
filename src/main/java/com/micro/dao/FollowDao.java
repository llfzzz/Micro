package com.micro.dao;

public interface FollowDao {
    boolean follow(long followerId, long followeeId);

    boolean unfollow(long followerId, long followeeId);

    boolean isFollowing(long followerId, long followeeId);
}
