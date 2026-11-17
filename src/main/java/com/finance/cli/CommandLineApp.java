package com.finance.cli;

import com.finance.core.exception.AppException;
import com.finance.core.exception.ValidationException;
import com.finance.core.model.BudgetStatus;
import com.finance.core.model.TransactionType;
import com.finance.core.model.User;
import com.finance.core.model.Wallet;
import com.finance.core.repository.UserRepository;
import com.finance.core.service.AuthService;
import com.finance.core.service.FinanceService;
import com.finance.core.service.StatisticsService;
import com.finance.core.service.TransferService;
import com.finance.core.storage.UserRecord;
import com.finance.core.storage.UserStorage;
import com.finance.core.storage.WalletStorage;
import com.finance.infra.CsvExporter;
import com.finance.infra.CsvImporter;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;

public class CommandLineApp {
    private final AuthService auth;
    private final FinanceService finance;
    private final StatisticsService stats;
    private final TransferService transfers;
    private final UserRepository users;
    private final UserStorage userStorage;
    private final WalletStorage walletStorage;
    private final CsvExporter exporter = new CsvExporter();
    private final CsvImporter importer = new CsvImporter();
    private final PrintStream out;
    private boolean running = true;

    public CommandLineApp(AuthService auth,
                          FinanceService finance,
                          StatisticsService stats,
                          TransferService transfers,
                          UserRepository users,
                          UserStorage userStorage,
                          WalletStorage walletStorage,
                          PrintStream out) {
        this.auth = auth;
        this.finance = finance;
        this.stats = stats;
        this.transfers = transfers;
        this.users = users;
        this.userStorage = userStorage;
        this.walletStorage = walletStorage;
        this.out = out;
    }

    public void run() {
        out.println("=== Finance Manager ===");
        out.println("Введите 'help' для списка команд.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (running && scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                try {
                    dispatch(line);
                } catch (AppException e) {
                    out.println("[ОШИБКА] " + e.getMessage());
                } catch (Exception e) {
                    out.println("[ОШИБКА] " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
        shutdown();
    }

    private void dispatch(String line) {
        String[] p = line.split("\\s+");
        String cmd = p[0].toLowerCase();
        switch (cmd) {
            case "help" -> printHelp();
            case "register" -> handleRegister(p);
            case "login" -> handleLogin(p);
            case "logout" -> { auth.logout(); out.println("Выход выполнен"); }
            case "whoami" -> out.println(auth.current().map(User::getLogin).orElse("(не авторизован)"));
            case "income" -> handleTransaction(p, TransactionType.INCOME);
            case "expense" -> handleTransaction(p, TransactionType.EXPENSE);
            case "budget" -> handleBudget(p);
            case "category" -> handleCategory(p);
            case "stats" -> handleStats(p);
            case "transfer" -> handleTransfer(p);
            case "export" -> handleExport(p);
            case "import" -> handleImport(p);
            case "exit", "quit" -> { running = false; out.println("Сохранение и выход..."); }
            default -> out.println("Неизвестная команда: " + cmd + ". Введите 'help'.");
        }
    }

    private void handleRegister(String[] p) {
        if (p.length != 3) throw new ValidationException("Использование: register <login> <password>");
        auth.register(p[1], p[2]);
        out.println("Зарегистрирован пользователь: " + p[1]);
    }

    private void handleLogin(String[] p) {
        if (p.length != 3) throw new ValidationException("Использование: login <login> <password>");
        User u = auth.login(p[1], p[2]);
        out.println("Вход выполнен: " + u.getLogin());
        out.println("Баланс: " + u.getWallet().getBalance().toPlainString());
    }

    private void handleTransaction(String[] p, TransactionType type) {
        if (p.length < 3)
            throw new ValidationException("Использование: " + type.name().toLowerCase() + " <category> <amount> [description]");
        User user = auth.require();
        String category = p[1];
        String amount = p[2];
        String desc = p.length > 3 ? String.join(" ", Arrays.copyOfRange(p, 3, p.length)) : "";
        List<String> alerts = type == TransactionType.INCOME
                ? finance.addIncome(user, category, amount, desc)
                : finance.addExpense(user, category, amount, desc);
        out.println("Добавлено: " + type + " " + category + " " + amount);
        for (String a : alerts) out.println("[ALERT] " + a);
    }

    private void handleBudget(String[] p) {
        User user = auth.require();
        if (p.length < 2)
            throw new ValidationException("Использование: budget set|edit|remove|list ...");
        String sub = p[1].toLowerCase();
        switch (sub) {
            case "set" -> {
                if (p.length != 4) throw new ValidationException("Использование: budget set <category> <limit>");
                finance.setBudget(user, p[2], p[3]);
                out.println("Бюджет установлен: " + p[2] + " = " + p[3]);
            }
            case "edit" -> {
                if (p.length != 4) throw new ValidationException("Использование: budget edit <category> <limit>");
                finance.editBudget(user, p[2], p[3]);
                out.println("Бюджет изменён: " + p[2] + " = " + p[3]);
            }
            case "remove" -> {
                if (p.length != 3) throw new ValidationException("Использование: budget remove <category>");
                if (finance.removeBudget(user, p[2])) out.println("Бюджет удалён: " + p[2]);
                else out.println("Бюджет не найден: " + p[2]);
            }
            case "list" -> printBudgets(user);
            default -> throw new ValidationException("Неизвестная подкоманда budget: " + sub);
        }
    }

    private void handleCategory(String[] p) {
        User user = auth.require();
        if (p.length != 4 || !p[1].equalsIgnoreCase("rename"))
            throw new ValidationException("Использование: category rename <old> <new>");
        finance.renameCategory(user, p[2], p[3]);
        out.println("Категория переименована: " + p[2] + " -> " + p[3]);
    }

    private void handleStats(String[] p) {
        User user = auth.require();
        Wallet w = user.getWallet();
        if (p.length == 1) { printGeneral(w); return; }
        String sub = p[1].toLowerCase();
        if (sub.equals("budgets")) { printBudgets(user); return; }
        if (sub.equals("by")) {
            if (p.length < 4)
                throw new ValidationException("Использование: stats by <income|expense> <cat1> <cat2> ...");
            TransactionType type = p[2].equalsIgnoreCase("income") ? TransactionType.INCOME
                    : p[2].equalsIgnoreCase("expense") ? TransactionType.EXPENSE
                    : null;
            if (type == null) throw new ValidationException("Тип должен быть income или expense");
            List<String> cats = Arrays.asList(p).subList(3, p.length);
            Map<String, BigDecimal> res = stats.byCategories(w, type, cats);
            out.printf("%s по выбранным категориям:%n", type == TransactionType.INCOME ? "Доходы" : "Расходы");
            res.forEach((k, v) -> out.printf("  %s: %s%n", k, v.toPlainString()));
            BigDecimal total = res.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            out.printf("  Итого: %s%n", total.toPlainString());
            return;
        }
        throw new ValidationException("Неизвестная подкоманда stats: " + sub);
    }

    private void handleTransfer(String[] p) {
        User user = auth.require();
        if (p.length != 3) throw new ValidationException("Использование: transfer <login> <amount>");
        transfers.transfer(user, p[1], p[2]);
        out.println("Перевод " + p[2] + " -> " + p[1] + " выполнен");
        out.println("Текущий баланс: " + user.getWallet().getBalance().toPlainString());
    }

    private void handleExport(String[] p) {
        User user = auth.require();
        if (p.length != 2) throw new ValidationException("Использование: export <file.csv>");
        exporter.export(Path.of(p[1]), user.getWallet());
        out.println("Экспортировано в " + p[1]);
    }

    private void handleImport(String[] p) {
        User user = auth.require();
        if (p.length != 2) throw new ValidationException("Использование: import <file.csv>");
        int n = importer.importFrom(Path.of(p[1]), user.getWallet());
        out.println("Импортировано операций: " + n);
    }

    private void printGeneral(Wallet w) {
        out.println("Общий доход: " + stats.totalIncome(w).toPlainString());
        Map<String, BigDecimal> income = stats.byCategory(w, TransactionType.INCOME);
        if (!income.isEmpty()) {
            out.println("Доходы по категориям:");
            income.forEach((k, v) -> out.printf("  %s: %s%n", k, v.toPlainString()));
        }
        out.println("Общие расходы: " + stats.totalExpense(w).toPlainString());
        Map<String, BigDecimal> expense = stats.byCategory(w, TransactionType.EXPENSE);
        if (!expense.isEmpty()) {
            out.println("Расходы по категориям:");
            expense.forEach((k, v) -> out.printf("  %s: %s%n", k, v.toPlainString()));
        }
        out.println("Баланс: " + w.getBalance().toPlainString());
    }

    private void printBudgets(User user) {
        List<BudgetStatus> list = stats.budgetStatus(user.getWallet());
        if (list.isEmpty()) { out.println("Бюджеты не установлены"); return; }
        out.println("Бюджет по категориям:");
        for (BudgetStatus b : list) {
            out.printf("  %s: %s, Потрачено: %s, Оставшийся бюджет: %s%n",
                    b.category(), b.limit().toPlainString(), b.spent().toPlainString(),
                    b.remaining().toPlainString());
        }
    }

    private void shutdown() {
        try {
            userStorage.saveAll(users.findAll().stream()
                    .map(u -> new UserRecord(u.getLogin(), u.getPasswordHash()))
                    .toList());
            for (User u : users.findAll()) walletStorage.save(u.getLogin(), u.getWallet());
            out.println("Данные сохранены. До свидания!");
        } catch (Exception e) {
            out.println("Ошибка при сохранении: " + e.getMessage());
        }
    }

    private void printHelp() {
        out.println("""
                Доступные команды:
                  register <login> <password>              — регистрация пользователя
                  login <login> <password>                 — вход
                  logout                                   — выход
                  whoami                                   — текущий пользователь

                  income <cat> <amount> [desc]             — добавить доход
                  expense <cat> <amount> [desc]            — добавить расход

                  budget set <cat> <limit>                 — установить бюджет
                  budget edit <cat> <limit>                — изменить бюджет
                  budget remove <cat>                      — удалить бюджет
                  budget list                              — список бюджетов

                  category rename <old> <new>              — переименовать категорию

                  stats                                    — общая статистика
                  stats budgets                            — статус бюджетов
                  stats by <income|expense> <c1> <c2> ...  — подсчёт по нескольким категориям

                  transfer <login> <amount>                — перевод другому пользователю

                  export <file.csv>                        — экспорт операций
                  import <file.csv>                        — импорт операций

                  help                                     — справка
                  exit                                     — выход с сохранением
                """);
    }
}
