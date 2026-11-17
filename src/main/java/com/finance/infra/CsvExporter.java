package com.finance.infra;

import com.finance.core.exception.AppException;
import com.finance.core.model.Transaction;
import com.finance.core.model.Wallet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvExporter {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void export(Path file, Wallet wallet) {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("type,category,amount,timestamp,description");
            for (Transaction t : wallet.getTransactions()) {
                lines.add(String.join(",",
                        t.getType().name(),
                        escape(t.getCategory()),
                        t.getAmount().toPlainString(),
                        t.getTimestamp().format(FMT),
                        escape(t.getDescription() == null ? "" : t.getDescription())));
            }
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            throw new AppException("Ошибка экспорта: " + e.getMessage(), e);
        }
    }

    private String escape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
