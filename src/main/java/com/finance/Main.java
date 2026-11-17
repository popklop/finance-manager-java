package com.finance;

import com.finance.cli.CommandLineApp;
import com.finance.core.model.User;
import com.finance.core.model.Wallet;
import com.finance.core.repository.UserRepository;
import com.finance.core.service.AlertService;
import com.finance.core.service.AuthService;
import com.finance.core.service.FinanceService;
import com.finance.core.service.StatisticsService;
import com.finance.core.service.TransferService;
import com.finance.core.storage.UserRecord;
import com.finance.core.storage.UserStorage;
import com.finance.core.storage.WalletStorage;
import com.finance.infra.FileUserStorage;
import com.finance.infra.FileWalletStorage;
import com.finance.infra.InMemoryUserRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Path dataDir = Paths.get(System.getProperty("finance.data.dir", "data"));
        UserStorage userStorage = new FileUserStorage(dataDir.resolve("users.json"));
        WalletStorage walletStorage = new FileWalletStorage(dataDir.resolve("wallets"));

        UserRepository users = new InMemoryUserRepository();
        List<UserRecord> records = userStorage.loadAll();
        for (UserRecord r : records) {
            Wallet w = walletStorage.load(r.login()).orElseGet(Wallet::new);
            users.save(new User(r.login(), r.passwordHash(), w));
        }

        AuthService auth = new AuthService(users);
        FinanceService finance = new FinanceService(new AlertService());
        StatisticsService stats = new StatisticsService();
        TransferService transfers = new TransferService(users);

        new CommandLineApp(auth, finance, stats, transfers, users,
                userStorage, walletStorage, System.out).run();
    }
}
