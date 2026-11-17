package com.finance.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private TransactionType type;
    private String category;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String description;

    public Transaction() { }

    public Transaction(TransactionType type, String category, BigDecimal amount,
                       LocalDateTime timestamp, String description) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public TransactionType getType() { return type; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
}
