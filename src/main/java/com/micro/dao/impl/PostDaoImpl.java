package com.micro.dao.impl;

import com.micro.dao.PostDao;
import com.micro.entity.Post;

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
import java.util.Optional;

public class  PostDaoImpl extends BaseDao implements PostDao {

    private static final String BASE_SELECT = "SELECT id, user_id, content_text, media_meta, link_url, visibility, like_count, comment_count, forward_count, is_deleted, created_at, updated_at FROM posts";

    public PostDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public long create(Post post) {
        String sql = "INSERT INTO posts(user_id, content_text, media_meta, link_url, visibility, like_count, comment_count, forward_count, is_deleted) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, post.getUserId());
            ps.setString(2, post.getContentText());
            ps.setString(3, post.getMediaMetaJson());
            ps.setString(4, post.getLinkUrl());
            ps.setString(5, post.getVisibility());
            ps.setInt(6, post.getLikeCount());
            ps.setInt(7, post.getCommentCount());
            ps.setInt(8, post.getForwardCount());
            ps.setBoolean(9, post.isDeleted());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("Failed to obtain post id");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to create post", e);
        }
    }

    @Override
    public Optional<Post> findById(long id) {
        return queryOne(BASE_SELECT + " WHERE id=?", ps -> ps.setLong(1, id));
    }

    @Override
    public List<Post> listByUser(long userId, int offset, int limit) {
        String sql = BASE_SELECT + " WHERE user_id=? AND is_deleted=0 ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return queryList(sql, ps -> {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
        });
    }

    @Override
    public List<Post> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(") AND is_deleted=0 ORDER BY created_at DESC");
        return queryList(sql.toString(), ps -> {
            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 1, ids.get(i));
            }
        });
    }

    @Override
    public List<Post> listFeed(int offset, int limit) {
        String sql = BASE_SELECT + " WHERE is_deleted=0 ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return queryList(sql, ps -> {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
        });
    }

    @Override
    public List<Post> adminSearch(Long userId, String keyword, String visibility, Boolean deleted, int offset, int limit) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1");
        var params = new ArrayList<Object>();
        appendPostFilters(sql, params, userId, keyword, visibility, deleted);
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(sql.toString(), ps -> setParams(ps, params));
    }

    @Override
    public long countAdmin(Long userId, String keyword, String visibility, Boolean deleted) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM posts WHERE 1=1");
        var params = new ArrayList<Object>();
        appendPostFilters(sql, params, userId, keyword, visibility, deleted);
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to count posts", e);
        }
    }

    @Override
    public boolean delete(long postId, long operatorId) {
        String sql = "DELETE FROM posts WHERE id=? AND user_id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setLong(2, operatorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete post", e);
        }
    }

    @Override
    public boolean delete(long postId) {
        String sql = "DELETE FROM posts WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete post", e);
        }
    }

    @Override
    public boolean updateCounts(long postId, int likeDelta, int commentDelta, int forwardDelta) {
        String sql = "UPDATE posts SET like_count = like_count + ?, comment_count = comment_count + ?, forward_count = forward_count + ?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, likeDelta);
            ps.setInt(2, commentDelta);
            ps.setInt(3, forwardDelta);
            ps.setLong(4, postId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update post counters", e);
        }
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM posts WHERE is_deleted=0";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to count posts", e);
        }
    }

    @Override
    public List<String> listContentWithTag(String tagPrefix, int limit) {
        String sql = "SELECT content_text FROM posts WHERE content_text LIKE ? AND is_deleted=0 ORDER BY created_at DESC LIMIT ?";
        return queryList(sql, ps -> {
            ps.setString(1, "%#" + tagPrefix + "%");
            ps.setInt(2, limit);
        }).stream().map(Post::getContentText).collect(java.util.stream.Collectors.toList());
    }

    private Optional<Post> queryOne(String sql, PreparedStatementSetter setter) {
        List<Post> posts = queryList(sql, setter);
        return posts.isEmpty() ? Optional.empty() : Optional.of(posts.get(0));
    }

    private List<Post> queryList(String sql, PreparedStatementSetter setter) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setter.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<Post> posts = new ArrayList<>();
                while (rs.next()) {
                    posts.add(mapPost(rs));
                }
                return posts;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to query posts", e);
        }
    }

    private Post mapPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setUserId(rs.getLong("user_id"));
        post.setContentText(rs.getString("content_text"));
        post.setMediaMetaJson(rs.getString("media_meta"));
        post.setLinkUrl(rs.getString("link_url"));
        post.setVisibility(rs.getString("visibility"));
        post.setLikeCount(rs.getInt("like_count"));
        post.setCommentCount(rs.getInt("comment_count"));
        post.setForwardCount(rs.getInt("forward_count"));
        post.setDeleted(rs.getBoolean("is_deleted"));
        post.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        post.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return post;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void appendPostFilters(StringBuilder sql, List<Object> params, Long userId, String keyword, String visibility, Boolean deleted) {
        if (userId != null && userId > 0) {
            sql.append(" AND user_id=?");
            params.add(userId);
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (content_text LIKE ? OR link_url LIKE ?)");
            params.add(like);
            params.add(like);
        }
        if (visibility != null && !visibility.isBlank()) {
            sql.append(" AND visibility=?");
            params.add(visibility.toUpperCase());
        }
        if (deleted != null) {
            sql.append(" AND is_deleted=?");
            params.add(deleted ? 1 : 0);
        }
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof String) {
                ps.setString(i + 1, (String) value);
            } else if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else if (value instanceof Long) {
                ps.setLong(i + 1, (Long) value);
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    @FunctionalInterface
    private interface PreparedStatementSetter {
        void accept(PreparedStatement ps) throws SQLException;
    }
}
