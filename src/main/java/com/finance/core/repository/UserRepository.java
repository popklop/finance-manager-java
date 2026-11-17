package com.finance.core.repository;

import com.finance.core.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByLogin(String login);
    boolean exists(String login);
    void save(User user);
    Collection<User> findAll();
}
