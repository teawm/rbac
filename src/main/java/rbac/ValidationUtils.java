package rbac;

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
}