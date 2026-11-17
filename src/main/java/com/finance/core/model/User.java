package com.finance.core.model;

import java.util.Objects;

public class User {
    private final String login;
    private final String passwordHash;
    private Wallet wallet;

    public User(String login, String passwordHash, Wallet wallet) {
        this.login = Objects.requireNonNull(login, "login");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.wallet = Objects.requireNonNull(wallet, "wallet");
    }

    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }
}
