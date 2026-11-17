package com.finance;

import com.finance.core.model.Transaction;
import com.finance.core.model.TransactionType;
import com.finance.core.model.Wallet;
import com.finance.core.storage.UserRecord;
import com.finance.infra.FileUserStorage;
import com.finance.infra.FileWalletStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {
    @TempDir Path tmp;

    @Test void savesAndLoadsWallet() {
        Wallet w = new Wallet();
        w.addTransaction(new Transaction(TransactionType.INCOME, "Salary",
                new BigDecimal("1000"), LocalDateTime.now(), ""));
        w.setBudget("Food", new BigDecimal("500"));

        FileWalletStorage storage = new FileWalletStorage(tmp.resolve("wallets"));
        storage.save("alice", w);
        Wallet loaded = storage.load("alice").orElseThrow();

        assertEquals(0, new BigDecimal("1000").compareTo(loaded.getBalance()));
        assertEquals(1, loaded.getTransactions().size());
        assertEquals(0, new BigDecimal("500").compareTo(loaded.getBudgets().get("Food")));
    }

    @Test void userStorageRoundtrip() {
        FileUserStorage storage = new FileUserStorage(tmp.resolve("users.json"));
        storage.saveAll(List.of(new UserRecord("alice", "hash")));
        List<UserRecord> loaded = storage.loadAll();
        assertEquals(1, loaded.size());
        assertEquals("alice", loaded.get(0).login());
    }

    @Test void missingWalletReturnsEmpty() {
        FileWalletStorage storage = new FileWalletStorage(tmp.resolve("wallets"));
        assertTrue(storage.load("nobody").isEmpty());
    }
}
