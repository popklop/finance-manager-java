package com.finance;

import com.finance.core.exception.AuthException;
import com.finance.core.exception.ValidationException;
import com.finance.core.repository.UserRepository;
import com.finance.core.service.AuthService;
import com.finance.infra.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    private UserRepository repo;
    private AuthService auth;

    @BeforeEach void setUp() { repo = new InMemoryUserRepository(); auth = new AuthService(repo); }

    @Test void registersNewUser() {
        auth.register("alice", "secret");
        assertTrue(repo.exists("alice"));
    }

    @Test void rejectsDuplicateLogin() {
        auth.register("alice", "secret");
        assertThrows(AuthException.class, () -> auth.register("ALICE", "other"));
    }

    @Test void rejectsEmptyPassword() {
        assertThrows(ValidationException.class, () -> auth.register("bob", ""));
    }

    @Test void loginSuccess() {
        auth.register("alice", "secret");
        var u = auth.login("alice", "secret");
        assertEquals("alice", u.getLogin());
        assertTrue(auth.current().isPresent());
    }

    @Test void loginWrongPassword() {
        auth.register("alice", "secret");
        assertThrows(AuthException.class, () -> auth.login("alice", "nope"));
    }

    @Test void requireWithoutLoginThrows() {
        assertThrows(AuthException.class, () -> auth.require());
    }
}
