package com.finance.core.storage;

import com.finance.core.model.Wallet;

import java.util.Optional;

public interface WalletStorage {
    Optional<Wallet> load(String login);
    void save(String login, Wallet wallet);
}
