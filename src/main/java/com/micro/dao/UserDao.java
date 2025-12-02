package com.micro.dao;

import com.micro.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    long create(User user);

    boolean update(User user);

    List<User> search(String keyword, String role, Boolean banned, int offset, int limit);

    long count(String keyword, String role, Boolean banned);

    boolean updatePassword(long userId, String passwordHash);

    boolean setAvatar(long userId, String avatarPath);

    boolean setBanner(long userId, String bannerPath);

    List<User> list(int offset, int limit);

    boolean banUser(long userId, boolean banned);

    long countAll();
}
