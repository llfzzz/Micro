package com.micro.dao.impl;

import com.micro.dao.FollowDao;
import com.micro.entity.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<User> listFollowers(long userId, int offset, int limit) {
        String sql = "SELECT u.id, u.username, u.email, u.display_name, u.bio, u.avatar_path, u.role, u.is_banned, u.created_at, u.updated_at " +
                "FROM users u JOIN follows f ON f.follower_id = u.id WHERE f.followee_id=? ORDER BY f.created_at DESC LIMIT ? OFFSET ?";
        return queryUsers(sql, ps -> {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
        });
    }

    @Override
    public List<User> listFollowing(long userId, int offset, int limit) {
        String sql = "SELECT u.id, u.username, u.email, u.display_name, u.bio, u.avatar_path, u.role, u.is_banned, u.created_at, u.updated_at " +
                "FROM users u JOIN follows f ON f.followee_id = u.id WHERE f.follower_id=? ORDER BY f.created_at DESC LIMIT ? OFFSET ?";
        return queryUsers(sql, ps -> {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
        });
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

    private List<User> queryUsers(String sql, PreparedStatementSetter setter) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setter.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
                return users;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to query follow users", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setDisplayName(rs.getString("display_name"));
        user.setBio(rs.getString("bio"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setRole(rs.getString("role"));
        user.setBanned(rs.getBoolean("is_banned"));
        user.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        user.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return user;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    @FunctionalInterface
    private interface PreparedStatementSetter {
        void accept(PreparedStatement ps) throws SQLException;
    }
}
