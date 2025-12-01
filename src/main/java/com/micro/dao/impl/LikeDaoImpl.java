package com.micro.dao.impl;

import com.micro.dao.LikeDao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LikeDaoImpl extends BaseDao implements LikeDao {

    public LikeDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public boolean addLike(long postId, long userId) {
        String sql = "INSERT INTO likes(post_id, user_id) VALUES (?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // duplicate like should be treated as success
            if (isDuplicateEntry(e)) {
                return false;
            }
            throw new IllegalStateException("Unable to add like", e);
        }
    }

    @Override
    public boolean removeLike(long postId, long userId) {
        String sql = "DELETE FROM likes WHERE post_id=? AND user_id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to remove like", e);
        }
    }

    @Override
    public boolean exists(long postId, long userId) {
        String sql = "SELECT 1 FROM likes WHERE post_id=? AND user_id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to check like", e);
        }
    }

    @Override
    public java.util.List<Long> getLikedPostIds(long userId, int offset, int limit) {
        String sql = "SELECT post_id FROM likes WHERE user_id=? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                java.util.List<Long> ids = new java.util.ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getLong("post_id"));
                }
                return ids;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to get liked post ids", e);
        }
    }

    private boolean isDuplicateEntry(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }
}
