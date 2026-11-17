package com.finance.infra;

import com.finance.core.exception.AppException;
import com.finance.core.model.Wallet;
import com.finance.core.storage.WalletStorage;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FileWalletStorage implements WalletStorage {
    private final Path dir;
    private final Gson gson = JsonFactory.create();

    public FileWalletStorage(Path dir) { this.dir = dir; }

    @Override
    public Optional<Wallet> load(String login) {
        Path file = dir.resolve(safe(login) + ".json");
        try {
            if (!Files.exists(file)) return Optional.empty();
            return Optional.ofNullable(gson.fromJson(Files.readString(file), Wallet.class));
        } catch (IOException e) {
            throw new AppException("Не удалось загрузить кошелёк: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(String login, Wallet wallet) {
        Path file = dir.resolve(safe(login) + ".json");
        try {
            Files.createDirectories(dir);
            Files.writeString(file, gson.toJson(wallet));
        } catch (IOException e) {
            throw new AppException("Не удалось сохранить кошелёк: " + e.getMessage(), e);
        }
    }

    private String safe(String login) {
        return login.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }
}
