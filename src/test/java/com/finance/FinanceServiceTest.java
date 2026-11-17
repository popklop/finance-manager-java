package com.finance;

import com.finance.core.exception.NotFoundException;
import com.finance.core.exception.ValidationException;
import com.finance.core.model.User;
import com.finance.core.model.Wallet;
import com.finance.core.service.AlertService;
import com.finance.core.service.FinanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {
    private FinanceService finance;
    private User user;

    @BeforeEach void setUp() {
        finance = new FinanceService(new AlertService());
        user = new User("alice", "hash", new Wallet());
    }

    @Test void incomeIncreasesBalance() {
        finance.addIncome(user, "Salary", "1000", "monthly");
        assertEquals(0, new BigDecimal("1000").compareTo(user.getWallet().getBalance()));
    }

    @Test void expenseDecreasesBalance() {
        finance.addIncome(user, "Salary", "1000", "");
        finance.addExpense(user, "Food", "300", "");
        assertEquals(0, new BigDecimal("700").compareTo(user.getWallet().getBalance()));
    }

    @Test void rejectsNegativeAmount() {
        assertThrows(ValidationException.class, () -> finance.addIncome(user, "Salary", "-100", ""));
    }

    @Test void rejectsNonNumericAmount() {
        assertThrows(ValidationException.class, () -> finance.addExpense(user, "Food", "abc", ""));
    }

    @Test void budgetExceededProducesAlert() {
        finance.setBudget(user, "Food", "100");
        List<String> alerts = finance.addExpense(user, "Food", "150", "");
        assertTrue(alerts.stream().anyMatch(a -> a.contains("Превышен бюджет")));
    }

    @Test void budget80PercentWarns() {
        finance.setBudget(user, "Food", "1000");
        List<String> alerts = finance.addExpense(user, "Food", "850", "");
        assertTrue(alerts.stream().anyMatch(a -> a.contains("80")));
    }

    @Test void expensesExceedIncomeAlert() {
        finance.addIncome(user, "Salary", "100", "");
        List<String> alerts = finance.addExpense(user, "Food", "200", "");
        assertTrue(alerts.stream().anyMatch(a -> a.contains("расходы превысили доходы")));
    }

    @Test void editBudgetRequiresExisting() {
        assertThrows(NotFoundException.class, () -> finance.editBudget(user, "Food", "100"));
    }

    @Test void renameCategoryUpdatesTransactionsAndBudget() {
        finance.setBudget(user, "Food", "100");
        finance.addExpense(user, "Food", "50", "");
        finance.renameCategory(user, "Food", "Meals");
        assertTrue(user.getWallet().getBudgets().containsKey("Meals"));
        assertFalse(user.getWallet().getBudgets().containsKey("Food"));
        assertTrue(user.getWallet().getTransactions().stream().anyMatch(t -> t.getCategory().equals("Meals")));
    }
}
