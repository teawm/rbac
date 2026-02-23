public class UserFilters {

    public static UserFilter byUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username не может быть null");
        }
        String normalized = username.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }
        return user -> normalized.equals(user.username());
    }

    public static UserFilter byUsernameContains(String substring) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring не может быть null");
        }
        String normalized = substring.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Substring не может быть пустым");
        }
        return user -> user.username().toLowerCase().contains(normalized);
    }

    public static UserFilter byEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email не может быть null");
        }
        String normalized = email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        return user -> normalized.equals(user.email().toLowerCase());
    }

    public static UserFilter byEmailDomain(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain не может быть null");
        }
        String normalized = domain.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Domain не может быть пустым");
        }
        String domainWithAt = normalized.startsWith("@") ? normalized : "@" + normalized;
        return user -> user.email().toLowerCase().endsWith(domainWithAt);
    }

    public static UserFilter byFullNameContains(String substring) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring не может быть null");
        }
        String normalized = substring.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Substring не может быть пустым");
        }
        return user -> user.fullName().toLowerCase().contains(normalized);
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование фильтров пользователей.]\n");

        User user1 = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        User user2 = User.validate("anton_shtirlz", "Anton Shtirlitz", "anton@mail.com");
        User user3 = User.validate("andrey", "Andrey ", "andrey@mail.com");
        User user4 = User.validate("username_", "Firstname Lastname", "username@email.ru");

        User[] users = {user1, user2, user3, user4};

        System.out.println("[Тест 1]: Фильтр byUsername(\"stas_\"):");
        UserFilter filter1 = UserFilters.byUsername("stas_");
        for (User u : users) {
            if (filter1.test(u)) {
                System.out.println("[v] - " + u.format());
            }
        }
        System.out.println();

        System.out.println("[Тест 2]: Фильтр byUsernameContains(\"_\"):");
        UserFilter filter2 = UserFilters.byUsernameContains("_");
        for (User u : users) {
            if (filter2.test(u)) {
                System.out.println("[v] - " + u.format());
            }
        }
        System.out.println();

        System.out.println("[Тест 3]: Фильтр byEmailDomain(\"@mail.com\"):");
        UserFilter filter3 = UserFilters.byEmailDomain("@mail.com");
        for (User u : users) {
            if (filter3.test(u)) {
                System.out.println("[v] - " + u.format());
            }
        }
        System.out.println();

        System.out.println("[Тест 4]: Комбинированный фильтр: username содержит \"_\" AND email содержит \"@mail.com\":");
        UserFilter filter4 = UserFilters.byUsernameContains("_")
                .and(UserFilters.byEmailDomain("@mail.com"));
        for (User u : users) {
            if (filter4.test(u)) {
                System.out.println("[v] - " + u.format());
            }
        }
        System.out.println();

        System.out.println("[Тест 5]: Комбинированный фильтр: полное имя содержит \"Stas\" OR \"Anton\":");
        UserFilter filter5 = UserFilters.byFullNameContains("Stas")
                .or(UserFilters.byFullNameContains("Anton"));
        for (User u : users) {
            if (filter5.test(u)) {
                System.out.println("[v] - " + u.format());
            }
        }
        System.out.println();

        System.out.println("[Тест 6]: Инвертированный фильтр: НЕ из домена \"@mail.com\":");
        UserFilter filter6 = UserFilters.byEmailDomain("@mail.com").negate();
        for (User u : users) {
            if (filter6.test(u)) {
                System.out.println("[v] - " + u.format());
            }
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}