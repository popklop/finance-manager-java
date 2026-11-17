package com.finance;

import com.finance.core.exception.NotFoundException;
import com.finance.core.exception.ValidationException;
import com.finance.core.model.User;
import com.finance.core.model.Wallet;
import com.finance.core.repository.UserRepository;
import com.finance.core.service.AlertService;
import com.finance.core.service.FinanceService;
import com.finance.core.service.TransferService;
import com.finance.infra.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {
    private TransferService transfer;
    private FinanceService finance;
    private User alice, bob;

    @BeforeEach void setUp() {
        UserRepository repo = new InMemoryUserRepository();
        transfer = new TransferService(repo);
        finance = new FinanceService(new AlertService());
        alice = new User("alice", "h", new Wallet());
        bob = new User("bob", "h", new Wallet());
        repo.save(alice);
        repo.save(bob);
        finance.addIncome(alice, "Salary", "1000", "");
    }

    @Test void transfersBetweenUsers() {
        transfer.transfer(alice, "bob", "300");
        assertEquals(0, new BigDecimal("700").compareTo(alice.getWallet().getBalance()));
        assertEquals(0, new BigDecimal("300").compareTo(bob.getWallet().getBalance()));
    }

    @Test void rejectsInsufficientFunds() {
        assertThrows(ValidationException.class, () -> transfer.transfer(alice, "bob", "5000"));
    }

    @Test void rejectsUnknownRecipient() {
        assertThrows(NotFoundException.class, () -> transfer.transfer(alice, "charlie", "10"));
    }

    @Test void rejectsSelfTransfer() {
        assertThrows(ValidationException.class, () -> transfer.transfer(alice, "alice", "10"));
    }
}
