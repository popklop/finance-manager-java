package com.finance.core.service;

import com.finance.core.model.Wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class AlertService {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal EIGHTY = BigDecimal.valueOf(80);

    public List<String> checkBudget(Wallet wallet, String category) {
        List<String> alerts = new ArrayList<>();
        BigDecimal limit = wallet.getBudgets().get(category);
        if (limit == null) return alerts;

        BigDecimal spent = wallet.spentIn(category);
        BigDecimal percent = limit.signum() == 0
                ? (spent.signum() == 0 ? BigDecimal.ZERO : HUNDRED)
                : spent.multiply(HUNDRED).divide(limit, 2, RoundingMode.HALF_UP);

        if (spent.compareTo(limit) > 0) {
            alerts.add(String.format("Превышен бюджет по категории '%s': потрачено %s из %s",
                    category, spent.toPlainString(), limit.toPlainString()));
        } else if (percent.compareTo(EIGHTY) >= 0) {
            alerts.add(String.format("Израсходовано %s%% бюджета по категории '%s' (осталось %s)",
                    percent.toPlainString(), category, limit.subtract(spent).toPlainString()));
        }
        return alerts;
    }

    public List<String> checkExpenseExceedsIncome(Wallet wallet) {
        List<String> alerts = new ArrayList<>();
        if (wallet.totalExpense().compareTo(wallet.totalIncome()) > 0) {
            alerts.add("Внимание: расходы превысили доходы");
        }
        return alerts;
    }

    public List<String> checkZeroBalance(Wallet wallet) {
        List<String> alerts = new ArrayList<>();
        if (wallet.getBalance().signum() <= 0) {
            alerts.add("Внимание: баланс кошелька ≤ 0");
        }
        return alerts;
    }
}
