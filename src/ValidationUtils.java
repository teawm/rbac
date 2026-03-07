import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2})?$");

    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        String trimmed = username.trim();
        return trimmed.length() >= 3 &&
                trimmed.length() <= 20 &&
                USERNAME_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String trimmed = email.trim();
        return !trimmed.isEmpty() && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidDate(String date) {
        if (date == null) {
            return false;
        }
        String trimmed = date.trim();
        return DATE_PATTERN.matcher(trimmed).matches();
    }

    public static String normalizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Поле '" + fieldName + "' не может быть пустым!");
        }
    }

    public static void validateUsername(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        if (!isValidUsername(value)) {
            throw new IllegalArgumentException(
                    "Поле '" + fieldName + "' может содержать только латинские буквы, цифры и подчеркивание, " +
                            "и должен иметь длину от 3 до 20 символов!"
            );
        }
    }

    public static void validateEmail(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        if (!isValidEmail(value)) {
            throw new IllegalArgumentException(
                    "Поле '" + fieldName + "' должно содержать корректный email-адрес!"
            );
        }
    }

    public static void validateDate(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        if (!isValidDate(value)) {
            throw new IllegalArgumentException(
                    "Поле '" + fieldName + "' должно быть в формате 'yyyy-MM-dd' или 'yyyy-MM-dd HH:mm'!"
            );
        }
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование ValidationUtils.]\n");

        System.out.println("[Тест 1] Валидное имя пользователя:");
        try {
            ValidationUtils.validateUsername("stas", "username");
            System.out.println("[v] Валидное имя прошло проверку");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 2] Невалидное имя пользователя (слишком короткое):");
        try {
            ValidationUtils.validateUsername("an", "username");
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3] Невалидное имя пользователя (недопустимые символы):");
        try {
            ValidationUtils.validateUsername("stas@name", "username");
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4] Валидный email:");
        try {
            ValidationUtils.validateEmail("stas@example.com", "email");
            System.out.println("[v] Валидный email прошел проверку");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5] Невалидный email:");
        try {
            ValidationUtils.validateEmail("name@com", "email");
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 6] Валидная дата:");
        try {
            ValidationUtils.validateDate("2026-02-15", "date");
            ValidationUtils.validateDate("2026-02-15 19:45", "date");
            System.out.println("[v] Валидная дата прошла проверку");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 7] Невалидная дата:");
        try {
            ValidationUtils.validateDate("15-02-2026", "date");
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 8] Нормализация строки:");
        String input = "  stas   makarov  ";
        String normalized = ValidationUtils.normalizeString(input);
        System.out.println("  > Вход: '" + input + "'");
        System.out.println("  > Выход: '" + normalized + "'");
        System.out.println("  > Длина: " + normalized.length());
        System.out.println();

        System.out.println("[Тест 9] Проверка пустой строки:");
        try {
            ValidationUtils.requireNonEmpty("", "username");
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}