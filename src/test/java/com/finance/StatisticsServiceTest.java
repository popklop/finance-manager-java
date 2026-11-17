package com.finance;

import com.finance.core.exception.NotFoundException;
import com.finance.core.model.BudgetStatus;
import com.finance.core.model.TransactionType;
import com.finance.core.model.User;
import com.finance.core.model.Wallet;
import com.finance.core.service.AlertService;
import com.finance.core.service.FinanceService;
import com.finance.core.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsServiceTest {
    private Wallet wallet;
    private StatisticsService stats;
    private FinanceService finance;

    @BeforeEach void setUp() {
        wallet = new Wallet();
        stats = new StatisticsService();
        finance = new FinanceService(new AlertService());
    }

    private void seed() {
        User u = new User("x", "h", wallet);
        finance.addIncome(u, "Salary", "20000", "");
        finance.addIncome(u, "Salary", "40000", "");
        finance.addIncome(u, "Bonus", "3000", "");
        finance.addExpense(u, "Food", "300", "");
        finance.addExpense(u, "Food", "500", "");
        finance.addExpense(u, "Fun", "3000", "");
        finance.addExpense(u, "Utilities", "3000", "");
        finance.addExpense(u, "Taxi", "1500", "");
    }

    @Test void totalIncome() {
        seed();
        assertEquals(0, new BigDecimal("63000").compareTo(stats.totalIncome(wallet)));
    }

    @Test void totalExpense() {
        seed();
        assertEquals(0, new BigDecimal("8300").compareTo(stats.totalExpense(wallet)));
    }

    @Test void byCategoryIncome() {
        seed();
        Map<String, BigDecimal> m = stats.byCategory(wallet, TransactionType.INCOME);
        assertEquals(0, new BigDecimal("60000").compareTo(m.get("Salary")));
    }

    @Test void byMultipleCategoriesExpense() {
        seed();
        Map<String, BigDecimal> m = stats.byCategories(wallet, TransactionType.EXPENSE,
                List.of("Food", "Fun"));
        assertEquals(0, new BigDecimal("800").compareTo(m.get("Food")));
        assertEquals(0, new BigDecimal("3000").compareTo(m.get("Fun")));
    }

    @Test void unknownCategoryThrows() {
        seed();
        assertThrows(NotFoundException.class,
                () -> stats.byCategories(wallet, TransactionType.EXPENSE, List.of("Unknown")));
    }

    @Test void budgetStatusRemaining() {
        User u = new User("x", "h", wallet);
        finance.setBudget(u, "Food", "4000");
        finance.addExpense(u, "Food", "800", "");
        BudgetStatus b = stats.budgetStatus(wallet).get(0);
        assertEquals(0, new BigDecimal("3200").compareTo(b.remaining()));
    }
}
