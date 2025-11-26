package com.micro.dao.impl;

import com.micro.dao.MediaDao;
import com.micro.entity.Media;

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

public class MediaDaoImpl extends BaseDao implements MediaDao {

    private static final String BASE_SELECT = "SELECT id, post_id, uploader_id, type, path, original_name, width, height, size, created_at FROM media";

    public MediaDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public long create(Media media) {
        String sql = "INSERT INTO media(post_id, uploader_id, type, path, original_name, width, height, size) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (media.getPostId() == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, media.getPostId());
            }
            if (media.getUploaderId() == null) {
                ps.setNull(2, java.sql.Types.BIGINT);
            } else {
                ps.setLong(2, media.getUploaderId());
            }
            ps.setString(3, media.getType());
            ps.setString(4, media.getPath());
            ps.setString(5, media.getOriginalName());
            if (media.getWidth() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, media.getWidth());
            }
            if (media.getHeight() == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, media.getHeight());
            }
            if (media.getSize() == null) {
                ps.setNull(8, java.sql.Types.BIGINT);
            } else {
                ps.setLong(8, media.getSize());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("Failed to get media id");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to create media", e);
        }
    }

    @Override
    public void bindToPost(long mediaId, long postId) {
        String sql = "UPDATE media SET post_id=? WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setLong(2, mediaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to bind media to post", e);
        }
    }

    @Override
    public List<Media> listByPost(long postId) {
        String sql = BASE_SELECT + " WHERE post_id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Media> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapMedia(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to list media", e);
        }
    }

    private Media mapMedia(ResultSet rs) throws SQLException {
        Media media = new Media();
        media.setId(rs.getLong("id"));
        long postId = rs.getLong("post_id");
        media.setPostId(rs.wasNull() ? null : postId);
        long uploaderId = rs.getLong("uploader_id");
        media.setUploaderId(rs.wasNull() ? null : uploaderId);
        media.setType(rs.getString("type"));
        media.setPath(rs.getString("path"));
        media.setOriginalName(rs.getString("original_name"));
        int width = rs.getInt("width");
        media.setWidth(rs.wasNull() ? null : width);
        int height = rs.getInt("height");
        media.setHeight(rs.wasNull() ? null : height);
        long size = rs.getLong("size");
        media.setSize(rs.wasNull() ? null : size);
        media.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        return media;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
