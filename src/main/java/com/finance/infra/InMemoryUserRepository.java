package com.finance.infra;

import com.finance.core.model.User;
import com.finance.core.repository.UserRepository;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new LinkedHashMap<>();

    @Override public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login.toLowerCase()));
    }
    @Override public boolean exists(String login) { return users.containsKey(login.toLowerCase()); }
    @Override public void save(User user) { users.put(user.getLogin().toLowerCase(), user); }
    @Override public Collection<User> findAll() { return Collections.unmodifiableCollection(users.values()); }
}
