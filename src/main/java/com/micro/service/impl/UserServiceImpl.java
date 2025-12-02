package com.micro.service.impl;

import com.micro.dao.UserDao;
import com.micro.entity.User;
import com.micro.entity.UserImage;
import com.micro.service.UserService;
import com.micro.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final int workFactor;

    public UserServiceImpl(UserDao userDao, int workFactor) {
        this.userDao = userDao;
        this.workFactor = workFactor;
    }

    @Override
    public Optional<User> findById(long id) {
        return userDao.findById(id);
    }

    @Override
    public long create(User user, String rawPassword) {
        user.setPasswordHash(PasswordUtil.hashPassword(rawPassword, workFactor));
        user.setRole(user.getRole() == null ? "USER" : user.getRole());
        return userDao.create(user);
    }

    @Override
    public boolean updateProfile(User user) {
        return userDao.update(user);
    }

    @Override
    public boolean updateAvatar(long userId, String avatarPath) {
        return userDao.setAvatar(userId, avatarPath);
    }

    @Override
    public boolean updateBanner(long userId, String bannerPath) {
        return userDao.setBanner(userId, bannerPath);
    }

    @Override
    public boolean updateAvatarData(long userId, byte[] data, String contentType) {
        return userDao.updateAvatarData(userId, data, contentType);
    }

    @Override
    public boolean updateBannerData(long userId, byte[] data, String contentType) {
        return userDao.updateBannerData(userId, data, contentType);
    }

    @Override
    public Optional<UserImage> getAvatarData(long userId) {
        return userDao.findAvatarData(userId);
    }

    @Override
    public Optional<UserImage> getBannerData(long userId) {
        return userDao.findBannerData(userId);
    }

    @Override
    public List<User> list(int offset, int limit) {
        return userDao.list(offset, limit);
    }


    @Override
    public boolean banUser(long userId, boolean banned) {
        return userDao.banUser(userId, banned);
    }

    @Override
    public List<User> searchUsers(String query, int limit) {
        return userDao.search(query, null, null, 0, limit);
    }
}
