package com.finance.core.service;

import com.finance.core.exception.NotFoundException;
import com.finance.core.exception.ValidationException;
import com.finance.core.model.Transaction;
import com.finance.core.model.TransactionType;
import com.finance.core.model.User;
import com.finance.core.model.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FinanceService {
    private final AlertService alerts;

    public FinanceService(AlertService alerts) { this.alerts = alerts; }

    public List<String> addIncome(User user, String category, String amountRaw, String description) {
        BigDecimal amount = parseAmount(amountRaw);
        validateCategory(category);
        user.getWallet().addTransaction(new Transaction(TransactionType.INCOME, category, amount,
                LocalDateTime.now(), desc(description)));
        return List.of();
    }

    public List<String> addExpense(User user, String category, String amountRaw, String description) {
        BigDecimal amount = parseAmount(amountRaw);
        validateCategory(category);
        Wallet w = user.getWallet();
        w.addTransaction(new Transaction(TransactionType.EXPENSE, category, amount,
                LocalDateTime.now(), desc(description)));

        List<String> messages = new ArrayList<>();
        messages.addAll(alerts.checkBudget(w, category));
        messages.addAll(alerts.checkExpenseExceedsIncome(w));
        messages.addAll(alerts.checkZeroBalance(w));
        return messages;
    }

    public void setBudget(User user, String category, String limitRaw) {
        validateCategory(category);
        BigDecimal limit = parseAmount(limitRaw);
        user.getWallet().setBudget(category, limit);
    }

    public void editBudget(User user, String category, String limitRaw) {
        if (!user.getWallet().hasBudget(category))
            throw new NotFoundException("Бюджет не найден: " + category);
        setBudget(user, category, limitRaw);
    }

    public boolean removeBudget(User user, String category) {
        return user.getWallet().removeBudget(category);
    }

    public void renameCategory(User user, String from, String to) {
        validateCategory(from);
        validateCategory(to);
        Wallet w = user.getWallet();
        List<Transaction> updated = new ArrayList<>();
        boolean any = false;
        for (Transaction t : w.getTransactions()) {
            if (t.getCategory().equalsIgnoreCase(from)) {
                updated.add(new Transaction(t.getType(), to, t.getAmount(), t.getTimestamp(), t.getDescription()));
                any = true;
            } else {
                updated.add(t);
            }
        }
        BigDecimal budget = w.getBudgets().get(from);
        if (budget != null) {
            w.removeBudget(from);
            w.setBudget(to, budget);
            any = true;
        }
        if (!any) throw new NotFoundException("Категория не найдена: " + from);
        w.setTransactions(updated);
    }

    public static BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) throw new ValidationException("Сумма не может быть пустой");
        try {
            BigDecimal v = new BigDecimal(raw.trim().replace(",", "."));
            if (v.signum() <= 0) throw new ValidationException("Сумма должна быть положительной");
            return v;
        } catch (NumberFormatException e) {
            throw new ValidationException("Некорректная сумма: " + raw);
        }
    }

    private void validateCategory(String c) {
        if (c == null || c.isBlank()) throw new ValidationException("Категория не может быть пустой");
        if (c.length() > 64) throw new ValidationException("Слишком длинное имя категории");
    }

    private String desc(String d) { return d == null ? "" : d.trim(); }
}
