package com.finance.core.service;

import com.finance.core.exception.NotFoundException;
import com.finance.core.exception.ValidationException;
import com.finance.core.model.BudgetStatus;
import com.finance.core.model.Transaction;
import com.finance.core.model.TransactionType;
import com.finance.core.model.Wallet;

import java.math.BigDecimal;
import java.util.*;

public class StatisticsService {

    public BigDecimal totalIncome(Wallet w) { return w.totalIncome(); }
    public BigDecimal totalExpense(Wallet w) { return w.totalExpense(); }

    public Map<String, BigDecimal> byCategory(Wallet w, TransactionType type) {
        Map<String, BigDecimal> result = new TreeMap<>();
        for (Transaction t : w.getTransactions()) {
            if (t.getType() == type) result.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
        }
        return result;
    }

    public Map<String, BigDecimal> byCategories(Wallet w, TransactionType type, List<String> categories) {
        if (categories == null || categories.isEmpty())
            throw new ValidationException("Не указаны категории");
        Map<String, BigDecimal> all = byCategory(w, type);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (String c : categories) {
            String match = all.keySet().stream()
                    .filter(k -> k.equalsIgnoreCase(c))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Категория не найдена: " + c));
            result.put(match, all.get(match));
        }
        return result;
    }

    public List<BudgetStatus> budgetStatus(Wallet w) {
        List<BudgetStatus> list = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : w.getBudgets().entrySet()) {
            BigDecimal spent = w.spentIn(e.getKey());
            list.add(new BudgetStatus(e.getKey(), e.getValue(), spent, e.getValue().subtract(spent)));
        }
        return list;
    }
}
