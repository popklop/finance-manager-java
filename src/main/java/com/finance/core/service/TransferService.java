package com.finance.core.service;

import com.finance.core.exception.NotFoundException;
import com.finance.core.exception.ValidationException;
import com.finance.core.model.Transaction;
import com.finance.core.model.TransactionType;
import com.finance.core.model.User;
import com.finance.core.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferService {
    public static final String TRANSFER_CATEGORY = "Перевод";

    private final UserRepository users;

    public TransferService(UserRepository users) { this.users = users; }

    public void transfer(User from, String toLogin, String amountRaw) {
        if (toLogin == null || toLogin.isBlank())
            throw new ValidationException("Укажите получателя");
        if (from.getLogin().equalsIgnoreCase(toLogin))
            throw new ValidationException("Нельзя переводить самому себе");

        User to = users.findByLogin(toLogin)
                .orElseThrow(() -> new NotFoundException("Получатель не найден: " + toLogin));

        BigDecimal amount = FinanceService.parseAmount(amountRaw);
        if (from.getWallet().getBalance().compareTo(amount) < 0)
            throw new ValidationException("Недостаточно средств для перевода");

        from.getWallet().addTransaction(new Transaction(TransactionType.EXPENSE,
                TRANSFER_CATEGORY, amount, LocalDateTime.now(),
                "Перевод пользователю " + to.getLogin()));
        to.getWallet().addTransaction(new Transaction(TransactionType.INCOME,
                TRANSFER_CATEGORY, amount, LocalDateTime.now(),
                "Перевод от " + from.getLogin()));
    }
}
