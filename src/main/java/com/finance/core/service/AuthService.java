package com.finance.core.service;

import com.finance.core.exception.AuthException;
import com.finance.core.exception.ValidationException;
import com.finance.core.model.User;
import com.finance.core.model.Wallet;
import com.finance.core.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class AuthService {
    private final UserRepository users;
    private User current;

    public AuthService(UserRepository users) { this.users = users; }

    public User register(String login, String password) {
        validate(login, password);
        if (users.exists(login)) throw new AuthException("Пользователь уже существует: " + login);
        User u = new User(login, hash(password), new Wallet());
        users.save(u);
        return u;
    }

    public User login(String login, String password) {
        validate(login, password);
        User u = users.findByLogin(login)
                .orElseThrow(() -> new AuthException("Пользователь не найден: " + login));
        if (!u.getPasswordHash().equals(hash(password)))
            throw new AuthException("Неверный пароль");
        current = u;
        return u;
    }

    public void logout() { current = null; }
    public Optional<User> current() { return Optional.ofNullable(current); }

    public User require() {
        if (current == null) throw new AuthException("Требуется авторизация. Используйте 'login <login> <password>'");
        return current;
    }

    private void validate(String login, String password) {
        if (login == null || login.isBlank()) throw new ValidationException("Логин не может быть пустым");
        if (password == null || password.isBlank()) throw new ValidationException("Пароль не может быть пустым");
        if (login.length() < 3) throw new ValidationException("Логин должен содержать не менее 3 символов");
        if (password.length() < 3) throw new ValidationException("Пароль должен содержать не менее 3 символов");
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
