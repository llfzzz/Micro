package com.micro.service.impl;

import com.micro.dao.FollowDao;
import com.micro.entity.User;
import com.micro.service.FollowService;

import java.util.List;

public class FollowServiceImpl implements FollowService {

    private final FollowDao followDao;

    public FollowServiceImpl(FollowDao followDao) {
        this.followDao = followDao;
    }

    @Override
    public boolean follow(long followerId, long followeeId) {
        validateIds(followerId, followeeId);
        if (followerId == followeeId) {
            return false;
        }
        return followDao.follow(followerId, followeeId);
    }

    @Override
    public boolean unfollow(long followerId, long followeeId) {
        validateIds(followerId, followeeId);
        if (followerId == followeeId) {
            return false;
        }
        return followDao.unfollow(followerId, followeeId);
    }

    @Override
    public boolean isFollowing(long followerId, long followeeId) {
        validateIds(followerId, followeeId);
        if (followerId == followeeId) {
            return false;
        }
        return followDao.isFollowing(followerId, followeeId);
    }

    @Override
    public long countFollowers(long userId) {
        requirePositive(userId);
        return followDao.countFollowers(userId);
    }

    @Override
    public long countFollowing(long userId) {
        requirePositive(userId);
        return followDao.countFollowing(userId);
    }

    @Override
    public List<User> listFollowers(long userId, int offset, int limit) {
        requirePositive(userId);
        return followDao.listFollowers(userId, normalizeOffset(offset), normalizeLimit(limit));
    }

    @Override
    public List<User> listFollowing(long userId, int offset, int limit) {
        requirePositive(userId);
        return followDao.listFollowing(userId, normalizeOffset(offset), normalizeLimit(limit));
    }

    private void validateIds(long followerId, long followeeId) {
        requirePositive(followerId);
        requirePositive(followeeId);
    }

    private void requirePositive(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
    }

    private int normalizeOffset(int offset) {
        return Math.max(0, offset);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, 50);
    }
}
