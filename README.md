# Finance Manager

Консольное приложение для учёта личных финансов на Java. Позволяет вести доходы и расходы,
ставить бюджеты по категориям, смотреть статистику, делать переводы между пользователями
и сохранять данные в файлы.

## Что нужно для запуска

- Java 17
- Maven

## Сборка и запуск

mvn clean package
java -jar target/finance-manager-1.0.0.jar

По умолчанию данные пишутся в папку data рядом с тем местом, откуда запускаете.
Можно указать другую папку:

java -Dfinance.data.dir=my_data -jar target/finance-manager-1.0.0.jar

## Как пользоваться

После запуска появляется приглашение, куда вводятся команды. Сначала нужно зарегистрироваться
или войти. Потом можно добавлять операции, ставить бюджеты и смотреть статистику.
Команда help показывает список всех команд, exit — сохраняет данные и выходит.

Пример:

register alice secret
login alice secret
income Salary 20000
expense Food 300
budget set Food 4000
stats
exit

## Команды

Учётная запись:
- register <login> <password> — зарегистрировать пользователя
- login <login> <password> — войти
- logout — выйти
- whoami — показать, кто сейчас вошёл

Доходы и расходы:
- income <категория> <сумма> [описание] — добавить доход
- expense <категория> <сумма> [описание] — добавить расход

Бюджеты:
- budget set <категория> <лимит> — поставить бюджет
- budget edit <категория> <лимит> — изменить
- budget remove <категория> — удалить
- budget list — показать все бюджеты

Категории:
- category rename <старое> <новое> — переименовать категорию везде

Статистика:
- stats — общий доход, расход, баланс и разбивка по категориям
- stats budgets — состояние бюджетов (лимит, потрачено, остаток)
- stats by <income|expense> <кат1> <кат2> ... — подсчёт по выбранным категориям

Переводы:
- transfer <login> <сумма> — перевести другому пользователю

Экспорт и импорт:
- export <file.csv> — сохранить операции в CSV
- import <file.csv> — загрузить операции из CSV

Прочее:
- help — справка
- exit — сохранить и выйти

## Формат CSV

type,category,amount,timestamp,description
EXPENSE,Food,300,2024-01-01T10:00:00,lunch
INCOME,Salary,20000,2024-01-05T09:00:00,monthly

## Где хранятся данные

В папке data (или той, что указана в finance.data.dir):

data/
  users.json          — список пользователей
  wallets/
    alice.json        — кошелёк пользователя alice
    bob.json          — кошелёк пользователя bob

При старте всё это читается обратно в память. При выходе — записывается снова.

## Структура проекта

src/main/java/com/finance/
  Main.java             — точка входа
  cli/                  — консольный интерфейс (CommandLineApp)
  core/
    model/              — User, Wallet, Transaction, BudgetStatus, TransactionType
    service/            — AuthService, FinanceService, StatisticsService, AlertService, TransferService
    repository/         — интерфейс UserRepository
    storage/            — интерфейсы UserStorage, WalletStorage и UserRecord
    exception/          — свои исключения (AppException и наследники)
  infra/                — реализации хранилищ и CSV (FileUserStorage, FileWalletStorage,
                          CsvExporter, CsvImporter, InMemoryUserRepository, JsonFactory)

Бизнес-логика ничего не знает про файлы и консоль — работает только с интерфейсами.
Реализации хранилищ лежат в infra. CLI просто разбирает ввод и вызывает сервисы.

## Тесты

mvn test

Тестами покрыты: авторизация, добавление операций, бюджеты и оповещения, статистика,
переводы, сохранение и загрузка кошельков, экспорт и импорт CSV.

## Зависимости

- Gson 2.10.1 — для работы с JSON
- JUnit 5.10.2 — для тестов

Обе подтягиваются Maven'ом автоматически, отдельно ставить не надо.
