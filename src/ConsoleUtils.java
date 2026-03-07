import java.util.*;
import java.util.regex.Pattern;

public class ConsoleUtils {

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);

            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    if (required) {
                        System.out.println("[!] Это поле обязательно для заполнения!");
                        continue;
                    } else {
                        return "";
                    }
                }

                return input;
            }
        }
    }

    public static String promptStringWithPattern(Scanner scanner, String message, boolean required,
                                                 Pattern pattern, String errorMessage) {
        while (true) {
            String input = promptString(scanner, message, required);

            if (!required && input.isEmpty()) {
                return input;
            }

            if (pattern.matcher(input).matches()) {
                return input;
            }

            System.out.println("[!] " + errorMessage);
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);

            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();

                if (value < min || value > max) {
                    System.out.println("[!] Значение должно быть в диапазоне от " + min + " до " + max);
                    continue;
                }

                return value;
            } else {
                System.out.println("[!] Введите корректное целое число!");
                scanner.nextLine();
            }
        }
    }

    public static double promptDouble(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);

            if (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();
                scanner.nextLine();
                return value;
            } else {
                System.out.println("[!] Введите корректное число!");
                scanner.nextLine();
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (yes/no): ");

            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim().toLowerCase();

                if (input.equals("yes") || input.equals("y")) {
                    return true;
                } else if (input.equals("no") || input.equals("n")) {
                    return false;
                } else {
                    System.out.println("[!] Введите 'yes' / 'no'");
                }
            }
        }
    }

    public static boolean promptYesNoCustom(Scanner scanner, String message,
                                            Set<String> trueValues, Set<String> falseValues) {
        while (true) {
            System.out.print(message + ": ");

            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim().toLowerCase();

                if (trueValues.contains(input)) {
                    return true;
                } else if (falseValues.contains(input)) {
                    return false;
                } else {
                    System.out.println("[!] Неверный ввод. Допустимые значения: " +
                            String.join("/", trueValues) + " или " + String.join("/", falseValues));
                }
            }
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Список вариантов не может быть пустым!");
        }

        if (options.size() == 1) {
            return options.get(0);
        }

        while (true) {
            System.out.println(message);

            for (int i = 0; i < options.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + options.get(i).toString());
            }

            int choice = promptInt(scanner, "\nВыберите номер: ", 1, options.size());

            return options.get(choice - 1);
        }
    }

    public static <T> T promptChoiceWithCancel(Scanner scanner, String message,
                                               List<T> options, boolean allowCancel) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Список вариантов не может быть пустым!");
        }

        while (true) {
            System.out.println(message);

            if (allowCancel) {
                System.out.println("  0. Отмена");
            }

            for (int i = 0; i < options.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + options.get(i).toString());
            }

            int max = options.size();
            if (allowCancel) {
                max++;
            }

            int choice = promptInt(scanner, "\nВыберите номер: ", allowCancel ? 0 : 1, max);

            if (allowCancel && choice == 0) {
                return null;
            }

            return options.get(choice - 1);
        }
    }

    public static String promptPassword(Scanner scanner, String message) {
        System.out.print(message);

        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }

        return "";
    }

    public static String promptEmail(Scanner scanner, String message) {
        Pattern emailPattern = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

        while (true) {
            String email = promptString(scanner, message, true);

            if (emailPattern.matcher(email).matches()) {
                return email;
            }

            System.out.println("[!] Неверный формат email! Пример: user@example.com");
        }
    }

    public static String promptDate(Scanner scanner, String message, boolean allowEmpty) {
        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2})?$");

        while (true) {
            String date = promptString(scanner, message, !allowEmpty);

            if (allowEmpty && date.isEmpty()) {
                return date;
            }

            if (datePattern.matcher(date).matches()) {
                return date;
            }

            System.out.println("[!] Неверный формат даты! Используйте 'yyyy-MM-dd' или 'yyyy-MM-dd HH:mm'");
        }
    }

    public static void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    public static void printSeparator(int length) {
        System.out.println("-".repeat(length));
    }

    public static void printSectionHeader(String title) {
        System.out.println();
        printSeparator(50);
        System.out.println("  " + title);
        printSeparator(50);
        System.out.println();
    }

    public static void printError(String message) {
        System.out.println("[x] " + message);
    }

    public static void printSuccess(String message) {
        System.out.println("[v] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("[!] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[i] " + message);
    }

    public static void waitForEnter(Scanner scanner, String message) {
        System.out.print(message);
        scanner.nextLine();
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование ConsoleUtils]\n");

        System.out.println("[Тест 0] Очистка экрана и разделители:");
        System.out.println("Перед очисткой... Засорим экран (немного)");
        System.out.println("ФЫФЫАСФЦСРФЦШДГФРГДФРЦГАЗЖУФЩАШЫЫАЫА");
        System.out.println("ЖЩУФШПАФОЭУШЛПМЭТЫДЩЛАИА");
        System.out.println("ФЗХЩАОШЗЩФГУРПАЗФШЫУПРЫШГПА");
        System.out.println("ФЫФЫАСФЦСРФЦШДГФРГДФРЦГАЗЖУФЩАШЫЫАЫА");
        clearScreen();
        printSeparator(10);
        printSectionHeader("Тест");
        printSeparator(10);


        Scanner scanner = new Scanner(System.in);

        System.out.println("[Тест 2] Запрос обязательной строки:");
        try {
            String name = promptString(scanner, "Введите имя: ", true);
            System.out.println("[v] Введено: '" + name + "'");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3] Запрос необязательной строки:");
        try {
            String optional = promptString(scanner, "Введите опциональное поле (или нажмите Enter): ", false);
            System.out.println("[v] Введено: '" + (optional.isEmpty() ? "<пусто>" : optional) + "'");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4] Запрос числа в диапазоне 1-10:");
        try {
            int number = promptInt(scanner, "Введите число от 1 до 10: ", 1, 10);
            System.out.println("[v] Введено: " + number);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5] Запрос подтверждения:");
        try {
            boolean confirmed = promptYesNo(scanner, "Вы уверены?");
            System.out.println("[v] Результат: " + (confirmed ? "Да" : "Нет"));
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 6] Выбор из списка:");
        try {
            List<String> options = Arrays.asList("Первый вариант", "Второй вариант", "Третий вариант");
            String choice = promptChoice(scanner, "Выберите вариант:", options);
            System.out.println("[v] Выбрано: " + choice);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 7] Выбор с возможностью отмены:");
        try {
            List<String> options = Arrays.asList("Вариант 1", "Вариант 2", "Вариант 3");
            String choice = promptChoiceWithCancel(scanner, "Выберите (или 0 для отмены):", options, true);
            System.out.println("[v] Выбрано: " + (choice == null ? "Отменено" : choice));
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 8] Запрос email:");
        try {
            String email = promptEmail(scanner, "Введите email: ");
            System.out.println("[v] Введено: " + email);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 9] Запрос даты:");
        try {
            String date = promptDate(scanner, "Введите дату (гггг-мм-дд): ", false);
            System.out.println("[v] Введено: " + date);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 10] Вывод сообщений:");
        printSuccess("Выполнено успешно!");
        printError("Произошла ошибка!");
        printWarning("Внимание!");
        printInfo("Спасибо за внимание!");
        System.out.println();

        scanner.close();
        System.out.println("[Все тесты завершены.]");
    }
}