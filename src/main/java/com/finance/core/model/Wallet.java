package com.finance.core.model;

import java.math.BigDecimal;
import java.util.*;


public class Wallet {
    private BigDecimal balance = BigDecimal.ZERO;
    private List<Transaction> transactions = new ArrayList<>();
    private Map<String, BigDecimal> budgets = new LinkedHashMap<>();

    public BigDecimal getBalance() { return balance; }

    public void addTransaction(Transaction t) {
        transactions.add(t);
        if (t.getType() == TransactionType.INCOME) {
            balance = balance.add(t.getAmount());
        } else {
            balance = balance.subtract(t.getAmount());
        }
    }

    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public Map<String, BigDecimal> getBudgets() { return Collections.unmodifiableMap(budgets); }

    public void setBudget(String category, BigDecimal limit) { budgets.put(category, limit); }

    public boolean removeBudget(String category) { return budgets.remove(category) != null; }

    public boolean hasBudget(String category) { return budgets.containsKey(category); }

    public BigDecimal spentIn(String category) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalExpense() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = new ArrayList<>(transactions); }
    public void setBudgets(Map<String, BigDecimal> budgets) { this.budgets = new LinkedHashMap<>(budgets); }
}
