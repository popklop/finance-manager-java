package com.finance.core.storage;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    List<UserRecord> loadAll();
    void saveAll(Collection<UserRecord> records);
}
