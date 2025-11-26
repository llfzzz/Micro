package com.micro.dao.impl;

import com.micro.dao.FollowDao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FollowDaoImpl extends BaseDao implements FollowDao {

    public FollowDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public boolean follow(long followerId, long followeeId) {
        String sql = "INSERT INTO follows(follower_id, followee_id) VALUES (?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, followerId);
            ps.setLong(2, followeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isConstraintViolation(e)) {
                return false;
            }
            throw new IllegalStateException("Unable to follow user", e);
        }
    }

    @Override
    public boolean unfollow(long followerId, long followeeId) {
        String sql = "DELETE FROM follows WHERE follower_id=? AND followee_id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, followerId);
            ps.setLong(2, followeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to unfollow user", e);
        }
    }

    @Override
    public boolean isFollowing(long followerId, long followeeId) {
        String sql = "SELECT 1 FROM follows WHERE follower_id=? AND followee_id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, followerId);
            ps.setLong(2, followeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to check follow state", e);
        }
    }

    @Override
    public long countFollowers(long userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE followee_id=?";
        return countBy(sql, userId);
    }

    @Override
    public long countFollowing(long userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id=?";
        return countBy(sql, userId);
    }

    private boolean isConstraintViolation(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }

    private long countBy(String sql, long userId) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to count follow records", e);
        }
    }
}
