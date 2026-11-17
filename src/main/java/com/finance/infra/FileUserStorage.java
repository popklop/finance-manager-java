package com.finance.infra;

import com.finance.core.exception.AppException;
import com.finance.core.storage.UserRecord;
import com.finance.core.storage.UserStorage;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FileUserStorage implements UserStorage {
    private final Path file;
    private final Gson gson = JsonFactory.create();

    public FileUserStorage(Path file) { this.file = file; }

    @Override
    public List<UserRecord> loadAll() {
        try {
            if (!Files.exists(file)) return List.of();
            String json = Files.readString(file);
            UserRecord[] arr = gson.fromJson(json, UserRecord[].class);
            return arr == null ? List.of() : Arrays.asList(arr);
        } catch (IOException e) {
            throw new AppException("Не удалось загрузить пользователей: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveAll(Collection<UserRecord> records) {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.writeString(file, gson.toJson(records));
        } catch (IOException e) {
            throw new AppException("Не удалось сохранить пользователей: " + e.getMessage(), e);
        }
    }
}
