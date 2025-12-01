package com.micro.dao.impl;

import com.micro.dao.CommentDao;
import com.micro.entity.Comment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentDaoImpl extends BaseDao implements CommentDao {

    private static final String BASE_SELECT = "SELECT id, post_id, user_id, parent_comment_id, content, is_deleted, created_at FROM comments";

    public CommentDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public long create(Comment comment) {
        String sql = "INSERT INTO comments(post_id, user_id, parent_comment_id, content, is_deleted) VALUES (?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, comment.getPostId());
            ps.setLong(2, comment.getUserId());
            if (comment.getParentCommentId() == null) {
                ps.setNull(3, java.sql.Types.BIGINT);
            } else {
                ps.setLong(3, comment.getParentCommentId());
            }
            ps.setString(4, comment.getContent());
            ps.setBoolean(5, comment.isDeleted());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("Failed to retrieve comment id");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to create comment", e);
        }
    }

    @Override
    public List<Comment> listByPost(long postId, int offset, int limit) {
        String sql = BASE_SELECT + " WHERE post_id=? AND is_deleted=0 ORDER BY created_at ASC LIMIT ? OFFSET ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Comment> comments = new ArrayList<>();
                while (rs.next()) {
                    comments.add(mapComment(rs));
                }
                return comments;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to list comments", e);
        }
    }

    @Override
    public List<Comment> listByUser(long userId, int offset, int limit) {
        String sql = BASE_SELECT + " WHERE user_id=? AND is_deleted=0 ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Comment> comments = new ArrayList<>();
                while (rs.next()) {
                    comments.add(mapComment(rs));
                }
                return comments;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to list user comments", e);
        }
    }

    @Override
    public boolean softDelete(long commentId, long operatorId) {
        String sql = "UPDATE comments SET is_deleted=1 WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, commentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete comment", e);
        }
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM comments WHERE is_deleted=0";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to count comments", e);
        }
    }

    private Comment mapComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        comment.setPostId(rs.getLong("post_id"));
        comment.setUserId(rs.getLong("user_id"));
        long parentId = rs.getLong("parent_comment_id");
        comment.setParentCommentId(rs.wasNull() ? null : parentId);
        comment.setContent(rs.getString("content"));
        comment.setDeleted(rs.getBoolean("is_deleted"));
        comment.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        return comment;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
