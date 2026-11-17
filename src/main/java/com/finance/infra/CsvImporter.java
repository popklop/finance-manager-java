package com.finance.infra;

import com.finance.core.exception.AppException;
import com.finance.core.exception.ValidationException;
import com.finance.core.model.Transaction;
import com.finance.core.model.TransactionType;
import com.finance.core.model.Wallet;
import com.finance.core.service.FinanceService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CsvImporter {

    public int importFrom(Path file, Wallet wallet) {
        try {
            List<String> lines = Files.readAllLines(file);
            if (lines.isEmpty()) return 0;
            int start = lines.get(0).startsWith("type,") ? 1 : 0;
            int count = 0;
            for (int i = start; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue;
                List<String> p = parseCsvLine(line);
                if (p.size() < 4) throw new ValidationException("Некорректная строка CSV: " + line);
                TransactionType type = TransactionType.valueOf(p.get(0).trim().toUpperCase());
                String category = p.get(1).trim();
                BigDecimal amount = FinanceService.parseAmount(p.get(2).trim());
                LocalDateTime ts = p.get(3).isBlank()
                        ? LocalDateTime.now()
                        : LocalDateTime.parse(p.get(3).trim());
                String desc = p.size() >= 5 ? p.get(4) : "";
                wallet.addTransaction(new Transaction(type, category, amount, ts, desc));
                count++;
            }
            return count;
        } catch (IOException e) {
            throw new AppException("Ошибка импорта: " + e.getMessage(), e);
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                    else inQuotes = false;
                } else cur.append(c);
            } else {
                if (c == '"') inQuotes = true;
                else if (c == ',') { result.add(cur.toString()); cur.setLength(0); }
                else cur.append(c);
            }
        }
        result.add(cur.toString());
        return result;
    }
}
