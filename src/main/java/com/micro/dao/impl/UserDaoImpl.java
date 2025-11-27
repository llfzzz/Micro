package com.micro.dao.impl;

import com.micro.dao.UserDao;
import com.micro.entity.User;

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

public class UserDaoImpl extends BaseDao implements UserDao {

    private static final String BASE_SELECT = "SELECT id, username, email, password_hash, display_name, bio, avatar_path, role, is_banned, created_at, updated_at FROM users";

    public UserDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Optional<User> findById(long id) {
        return queryOne(BASE_SELECT + " WHERE id=?", ps -> ps.setLong(1, id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return queryOne(BASE_SELECT + " WHERE username=?", ps -> ps.setString(1, username));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return queryOne(BASE_SELECT + " WHERE email=?", ps -> ps.setString(1, email));
    }

    @Override
    public long create(User user) {
        String sql = "INSERT INTO users(username, email, password_hash, display_name, bio, avatar_path, role, is_banned) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getDisplayName());
            ps.setString(5, user.getBio());
            ps.setString(6, user.getAvatarPath());
            ps.setString(7, user.getRole());
            ps.setBoolean(8, user.isBanned());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("Failed to retrieve generated id for user");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to create user", e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET email=?, display_name=?, bio=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getDisplayName());
            ps.setString(3, user.getBio());
            ps.setLong(4, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update user", e);
        }
    }

    @Override
    public List<User> search(String keyword, String role, Boolean banned, int offset, int limit) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendUserFilters(sql, params, keyword, role, banned);
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(sql.toString(), ps -> setParams(ps, params));
    }

    @Override
    public long count(String keyword, String role, Boolean banned) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendUserFilters(sql, params, keyword, role, banned);
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to count users", e);
        }
    }

    @Override
    public boolean updatePassword(long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update password", e);
        }
    }

    @Override
    public boolean setAvatar(long userId, String avatarPath) {
        String sql = "UPDATE users SET avatar_path=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, avatarPath);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to set avatar", e);
        }
    }

    @Override
    public List<User> list(int offset, int limit) {
        String sql = BASE_SELECT + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapUser(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to list users", e);
        }
    }

    @Override
    public boolean banUser(long userId, boolean banned) {
        String sql = "UPDATE users SET is_banned=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, banned);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to ban user", e);
        }
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to count users", e);
        }
    }

    private Optional<User> queryOne(String sql, PreparedStatementSetter setter) {
        List<User> users = queryList(sql, setter);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    private List<User> queryList(String sql, PreparedStatementSetter setter) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setter.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapUser(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to query user", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
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

    private void appendUserFilters(StringBuilder sql, List<Object> params, String keyword, String role, Boolean banned) {
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (username LIKE ? OR email LIKE ? OR display_name LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (role != null && !role.isBlank()) {
            sql.append(" AND role=?");
            params.add(role.toUpperCase());
        }
        if (banned != null) {
            sql.append(" AND is_banned=?");
            params.add(banned ? 1 : 0);
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
            } else if (value instanceof Boolean) {
                ps.setBoolean(i + 1, (Boolean) value);
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
