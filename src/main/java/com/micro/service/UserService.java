package com.micro.service;

import com.micro.entity.User;
import com.micro.entity.UserImage;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findById(long id);

    long create(User user, String rawPassword);

    boolean updateProfile(User user);

    boolean updateAvatar(long userId, String avatarPath);

    boolean updateBanner(long userId, String bannerPath);

    boolean updateAvatarData(long userId, byte[] data, String contentType);

    boolean updateBannerData(long userId, byte[] data, String contentType);

    Optional<UserImage> getAvatarData(long userId);

    Optional<UserImage> getBannerData(long userId);

    List<User> list(int offset, int limit);

    boolean banUser(long userId, boolean banned);

    List<User> searchUsers(String query, int limit);
}
