import java.util.Comparator;

public class UserSorters {

    public static Comparator<User> byUsername() {
        return Comparator.comparing(User::username);
    }

    public static Comparator<User> byFullName() {
        return Comparator.comparing(User::fullName);
    }

    public static Comparator<User> byEmail() {
        return Comparator.comparing(User::email);
    }

    public static Comparator<User> byUsernameLength() {
        return Comparator.comparing(user -> user.username().length());
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование сортировки пользователей.]\n");

        User user1 = User.validate("stas", "Stas User", "stas@example.com");
        User user2 = User.validate("anton", "Anton User", "anton@example.com");
        User user3 = User.validate("cedric", "Cedric Character", "cedric@example.com");
        User user4 = User.validate("nikita", "Nikita Person", "nikita@example.com");
        User user5 = User.validate("andrey", "Andrey Creature", "andrey@example.com");

        User[] users = {user1, user2, user3, user4, user5};

        System.out.println("[Тест 1]: Сортировка по username:");
        java.util.Arrays.sort(users, UserSorters.byUsername());
        for (User u : users) {
            System.out.println(u.username());
        }
        System.out.println();

        System.out.println("[Тест 2]: Сортировка по полному имени:");
        java.util.Arrays.sort(users, UserSorters.byFullName());
        for (User u : users) {
            System.out.println(u.fullName());
        }
        System.out.println();

        System.out.println("[Тест 3]: Сортировка по email:");
        java.util.Arrays.sort(users, UserSorters.byEmail());
        for (User u : users) {
            System.out.println(u.email());
        }
        System.out.println();

        System.out.println("[Тест 4]: Сортировка по длине username (от короткого к длинному):");
        java.util.Arrays.sort(users, UserSorters.byUsernameLength());
        for (User u : users) {
            System.out.println(u.username() + " (" + u.username().length() + " символов)");
        }
        System.out.println();

        System.out.println("[Тест 5]: Комбинированная сортировка: по полному имени, затем по username:");
        java.util.Arrays.sort(users, UserSorters.byFullName().thenComparing(UserSorters.byUsername()));
        for (User u : users) {
            System.out.println(u.fullName() + " (" + u.username() + ")");
        }
        System.out.println();

        System.out.println("[Все тесты завершены]");
    }
}