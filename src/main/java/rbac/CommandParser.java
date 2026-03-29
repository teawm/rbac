package main.java.rbac;

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
void printHelp() {
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
}