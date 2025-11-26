package com.micro.service;

import com.micro.entity.User;

import java.util.Optional;

public interface AuthService {
    long register(User user, String rawPassword);

    Optional<User> login(String identifier, String rawPassword);

    void logout(long userId);
}
