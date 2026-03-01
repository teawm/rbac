import java.util.*;
import java.util.Scanner;

public class CommandParser {
    private final Map<String, Command> commands = new HashMap<>();

    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя команды не может быть пустым");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Описание команды не может быть пустым");
        }
        if (command == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }

        String key = name.trim().toLowerCase();
        commands.put(key, command);
        commandDescriptions.put(key, description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        if (commandName == null || commandName.trim().isEmpty()) {
            System.out.println("[x] Ошибка: имя команды не может быть пустым!");
            return;
        }

        String key = commandName.trim().toLowerCase();
        Command command = commands.get(key);

        if (command == null) {
            System.out.println("[!] Неизвестная команда: '" + commandName + "'");
            System.out.println("[?] Введите 'help' для просмотра списка команд");
            return;
        }

        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("[x] Ошибка выполнения команды '" + commandName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void printHelp() {
        System.out.println("\n[/// Справка по командам ///]\n");

        if (commandDescriptions.isEmpty()) {
            System.out.println("\n/Нет команд/\n");
            return;
        }

        List<String> sortedCommands = new ArrayList<>(commandDescriptions.keySet());
        sortedCommands.sort(Comparator.naturalOrder());

        System.out.println("Доступные команды:\n");
        for (String cmd : sortedCommands) {
            System.out.printf("  %-30s %s%n", cmd, commandDescriptions.get(cmd));
        }

        System.out.println("\n[>] Введите команду для ее выполнения");
        System.out.println("[<] Для выхода введите 'exit'\n");
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null) {
            System.out.println("[x] Ошибка: ввод не может быть null");
            return;
        }

        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            return;
        }

        String[] parts = trimmedInput.split("\\s+", 2);
        String commandName = parts[0];

        executeCommand(commandName, scanner, system);
    }

    public boolean checkCommand(String commandName) {
        if (commandName == null || commandName.trim().isEmpty()) {
            return false;
        }
        return commands.containsKey(commandName.trim().toLowerCase());
    }

    public int getCommandCount() {
        return commands.size();
    }

    public List<String> getCommandList() {
        return new ArrayList<>(commands.keySet());
    }

    public void clear() {
        commands.clear();
        commandDescriptions.clear();
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование CommandParser.]\n");

        CommandParser parser = new CommandParser();
        RBACSystem system = new RBACSystem();
        Scanner scanner = new Scanner(System.in);

        System.out.println("[Тест 1] Регистрация команды:");
        try {
            parser.registerCommand("test-command", "Тестовая команда", (sc, sys) -> {
                System.out.println("[v] Команда выполнена успешно!");
            });
            System.out.println("[v] Команда зарегистрирована");
            System.out.println("  > Количество команд: " + parser.getCommandCount());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 2] Выполнение зарегистрированной команды:");
        try {
            parser.executeCommand("test-command", scanner, system);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3] Выполнение несуществующей команды:");
        try {
            parser.executeCommand("nunettakoycommand", scanner, system);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4] Регистрация нескольких команд:");
        try {
            parser.registerCommand("user-list", "Показать список пользователей", (sc, sys) -> {
                System.out.println("Список пользователей...");
            });
            parser.registerCommand("role-list", "Показать список ролей", (sc, sys) -> {
                System.out.println("Список ролей...");
            });
            parser.registerCommand("help", "Показать справку", (sc, sys) -> {
                parser.printHelp();
            });

            System.out.println("[v] Зарегистрировано команд: " + parser.getCommandCount());
            System.out.println("  > Список команд: " + parser.getCommandList());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5] Проверка существования команды:");
        try {
            System.out.println("[v] Команда 'user-list' существует: " + parser.checkCommand("user-list"));
            System.out.println("[v] Команда 'random-command-name' существует: " + parser.checkCommand("random-command-name"));
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 6] Парсинг и выполнение ввода:");
        try {
            System.out.println("Выполнение команды 'role-list':");
            parser.parseAndExecute("role-list", scanner, system);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 7] Парсинг ввода с аргументами:");
        try {
            System.out.println("Выполнение команды с аргументами:");
            parser.parseAndExecute("user-list --filter=admin", scanner, system);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 8] Вывод справки:");
        try {
            parser.printHelp();
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 9] Очистка всех команд:");
        try {
            int before = parser.getCommandCount();
            parser.clear();
            int after = parser.getCommandCount();
            System.out.println("  > Команд до очистки: " + before);
            System.out.println("  > Команд после очистки: " + after);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 10] Регистрация команды с пустым именем:");
        try {
            parser.registerCommand(" ", "Описание", (sc, sys) -> {});
            System.out.println("[v] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Исключение получено: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 11] Регистрация команды с пустым описанием:");
        try {
            parser.registerCommand("test", "", (sc, sys) -> {});
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Исключение получено: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 12] Регистрация команды с null реализацией:");
        try {
            parser.registerCommand("test", "Описание", null);
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Исключение получено: " + e.getMessage());
        }
        System.out.println();

        scanner.close();
        System.out.println("[Все тесты завершены.]");
    }
}