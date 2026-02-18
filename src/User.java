import java.util.Objects;
import java.util.regex.Pattern;

// record User
// Запись с полями String username, String fullName, String email
public record User(String username, String fullName, String email) {
    // Валидация regex, 3-20 символов, латиница, цифры и _
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    // Базовый формат email: @ + .
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public User {
        if (username == null || fullName == null || email == null) {
            throw new IllegalArgumentException("Все поля обязательны!");
        }
        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("Поле username не может быть пустым!");
        }
        if (fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Поле fullName не может быть пустым!");
        }
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Поле email не может быть пустым!");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Имя пользователя может содержать только латинские буквы, цифры и знак подчеркивания, " +
                            "и должно иметь длину от 3 до 20 символов!"
            );
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Адрес эл. почты должен содержать символ '@' и точку после него!"
            );
        }
    }
    // User.validate
    public static User validate(String username, String fullName, String email) {
        // Удаление пробелов в начале и в конце
        String usernameN = username.trim();
        String fullNameN = fullName.trim();
        String emailN = email.trim();

        return new User(usernameN, fullNameN, emailN);
    }

    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }

    public static void main(String[] args) {
        System.out.println("\n\n[Тестирование валидации User].\n");
        // Корректные данные
        try {
            System.out.print("Тест 1: ");
            User user_fine = User.validate("stas_", "Stas Makarov", "stas_email@exam.ple");
            System.out.println("[v] Данные корректны.");
        } catch (Exception err) {
            System.out.println("[x] " + err.getMessage());
        }
        // Неверный username
        try {
            System.out.print("Тест 2: ");
            User user_un = User.validate("aa", "Антон Штирлиц", "anton@email.com");
            System.out.println("[x] Данные корректны");
        } catch (IllegalArgumentException err) {
            System.out.println("[v] " + err.getMessage());
        }
        // Неверный email
        try {
            System.out.print("Тест 3: ");
            User user_em = User.validate("andrey", "Andrey Lastname", "wrong@email");
            System.out.println("[x] Данные корректны");
        } catch (IllegalArgumentException err) {
            System.out.println("[v] " + err.getMessage());
        }
        // Корректные данные с пробелами
        try {
            System.out.print("Тест 4: ");
            User tmspace = User.validate(" user ", "  Firstname Lastname ", " email@example.com    ");
            System.out.println("[v] Данные корректны.");
        } catch (IllegalArgumentException err) {
            System.out.println("[x] " + err.getMessage());
        }
        // Пустое поле username
        try {
            System.out.print("Тест 5: ");
            User tmspace = User.validate("     ", "Имя Фамилия", "email@example.com");
            System.out.println("[x] Данные корректны.");
        } catch (IllegalArgumentException err) {
            System.out.println("[v] " + err.getMessage());
        }

        System.out.println("\n\n[Все тесты завершены].\n");
    }
}