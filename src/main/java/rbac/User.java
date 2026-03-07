package rbac;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
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

    public static User validate(String username, String fullName, String email) {
        String usernameN = username.trim();
        String fullNameN = fullName.trim();
        String emailN = email.trim();
        return new User(usernameN, fullNameN, emailN);
    }

    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
}