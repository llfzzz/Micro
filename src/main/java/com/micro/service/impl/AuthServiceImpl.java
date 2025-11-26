package com.micro.service.impl;

import com.micro.dao.UserDao;
import com.micro.entity.User;
import com.micro.service.AuthService;
import com.micro.util.PasswordUtil;

import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final int workFactor;

    public AuthServiceImpl(UserDao userDao, int workFactor) {
        this.userDao = userDao;
        this.workFactor = workFactor;
    }

    @Override
    public long register(User user, String rawPassword) {
        user.setPasswordHash(PasswordUtil.hashPassword(rawPassword, workFactor));
        user.setRole(user.getRole() == null ? "USER" : user.getRole());
        return userDao.create(user);
    }

    @Override
    public Optional<User> login(String identifier, String rawPassword) {
        Optional<User> userOpt = userDao.findByUsername(identifier)
                .or(() -> userDao.findByEmail(identifier));
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (user.isBanned()) {
            return Optional.empty();
        }
        return PasswordUtil.matches(rawPassword, user.getPasswordHash())
                ? Optional.of(user)
                : Optional.empty();
    }

    @Override
    public void logout(long userId) {
        // Stateless service placeholder for session removal at servlet level
    }
}
