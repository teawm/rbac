import java.util.Objects;
import java.util.regex.Pattern;

// Запись Permission с полями String name, String resource, String description
public record Permission(String name, String resource, String description) {

    // Паттерн для проверки верхнего регистра без пробелов
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    //  Валидация и нормализация
    public Permission {
        if (name == null || resource == null || description == null) {
            throw new IllegalArgumentException("Все поля обязательны!");
        }

        // Преобразование в верхний регистр без пробелов
        name = name.trim().replaceAll("\\s+", "").toUpperCase();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Название права не может быть пустым!");
        }
        if (!PERMISSION_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Название права должно содержать только буквы, цифры и подчеркивание!"
            );
        }

        // Нижний регистр
        resource = resource.trim().toLowerCase();
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым!");
        }

        // Проверка description
        description = description.trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Описание права не может быть пустым!");
        }
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }
    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = namePattern == null ||
                name.contains(namePattern.toUpperCase().trim());
        boolean resourceMatches = resourcePattern == null ||
                resource.contains(resourcePattern.toLowerCase().trim());
        return nameMatches && resourceMatches;
    }

    public static void main(String[] args) {
        System.out.println("\n[Тестирование Permission].\n");
        // Тест 1: Валидное право с автоматической нормализацией
        try {
            Permission perm1 = new Permission("read", "Users", "Can view user list");
            System.out.println("[v] Тест 1: " + perm1.format());
            System.out.println("Проверка: name='" + perm1.name() +
                    "', resource='" + perm1.resource() + "'\n");
        } catch (Exception e) {
            System.out.println("[x] Тест 1: " + e.getMessage() + "\n");
        }
        // Тест 2: Удаление пробелов из name
        try {
            Permission perm2 = new Permission("WRITE ACCESS", "reports", "Full report access");
            System.out.println("[v] Тест 2: " + perm2.format() + "\n");
        } catch (Exception e) {
            System.out.println("[x] Тест 2: " + e.getMessage() + "\n");
        }
        // Тест 3: Пустое описание
        try {
            new Permission("DELETE", "settings", "   ");
            System.out.println("[x] Тест 3: пустой параметр прошел проверку.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Тест 3: " + e.getMessage() + "\n");
        }
        // Тест 4: Поиск по шаблонам
        try {
            Permission perm4 = new Permission("USER_ADMIN", "users", "Full user management");
            boolean match1 = perm4.matches("ADMIN", null);
            boolean match2 = perm4.matches(null, "user");
            boolean match3 = perm4.matches("WRONG", "settings");

            System.out.println("[v] Тест 4.1 - поиск 'ADMIN' в name: " + match1);
            System.out.println("[v] Тест 4.2 - поиск 'user' в resource: " + match2);
            System.out.println("[v] Тест 4.3 - неверные параметры: " + match3 + "\n");
        } catch (Exception err) {
            System.out.println("[x] Тест 4: " + err.getMessage() + "\n");
        }
        // Тест 5: Недопустимые символы в name (после нормализации)
        try {
            new Permission("READ!", "data", "Access to data");
            System.out.println("[x] Тест 5: недопустимый символ в параметре прошел проверку\n");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Тест 5: " + e.getMessage() + "\n");
        }

        // Тест 6: Пустой ресурс
        try {
            new Permission("VIEW", "", "View only");
            System.out.println("[x] Тест 6: пустой параметр прошел проверку\n");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Тест 6: " + e.getMessage() + "\n");
        }

        System.out.println("[Все тесты завершены].\n");
    }
}