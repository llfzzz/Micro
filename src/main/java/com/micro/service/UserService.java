package com.micro.service;

import com.micro.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findById(long id);

    long create(User user, String rawPassword);

    boolean updateProfile(User user);

    boolean updateAvatar(long userId, String avatarPath);

    boolean updateBanner(long userId, String bannerPath);

    List<User> list(int offset, int limit);

    boolean banUser(long userId, boolean banned);

    List<User> searchUsers(String query, int limit);
}
