package com.finance;

import com.finance.core.model.Transaction;
import com.finance.core.model.TransactionType;
import com.finance.core.model.Wallet;
import com.finance.infra.CsvExporter;
import com.finance.infra.CsvImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CsvTest {
    @TempDir Path tmp;

    @Test void exportImportRoundtrip() {
        Wallet w = new Wallet();
        w.addTransaction(new Transaction(TransactionType.EXPENSE, "Food",
                new BigDecimal("100"), LocalDateTime.now(), "lunch"));
        w.addTransaction(new Transaction(TransactionType.INCOME, "Salary",
                new BigDecimal("5000"), LocalDateTime.now(), ""));

        Path file = tmp.resolve("report.csv");
        new CsvExporter().export(file, w);

        Wallet other = new Wallet();
        int n = new CsvImporter().importFrom(file, other);
        assertEquals(2, n);
        assertEquals(0, new BigDecimal("4900").compareTo(other.getBalance()));
    }

    @Test void importHandlesQuotedFields() throws Exception {
        Path file = tmp.resolve("q.csv");
        Files.writeString(file,
                "type,category,amount,timestamp,description\n" +
                "EXPENSE,\"Food, drinks\",100,2024-01-01T10:00:00,\"lunch, cafe\"\n");
        Wallet w = new Wallet();
        new CsvImporter().importFrom(file, w);
        assertEquals(1, w.getTransactions().size());
        assertEquals("Food, drinks", w.getTransactions().get(0).getCategory());
    }
}
