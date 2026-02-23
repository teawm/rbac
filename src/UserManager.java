import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User item) {
        if (item == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
        String username = item.username().toLowerCase();
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Пользователь с именем '" + item.username() + "' уже существует");
        }
        users.put(username, item);
    }

    @Override
    public boolean remove(User item) {
        if (item == null) {
            return false;
        }
        return users.remove(item.username().toLowerCase()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id.trim().toLowerCase()));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(username.trim().toLowerCase()));
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalizedEmail = email.trim().toLowerCase();
        return users.values().stream()
                .filter(user -> user.email().toLowerCase().equals(normalizedEmail))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = (filter != null)
                ? findByFilter(filter)
                : findAll();

        if (sorter != null) {
            result = result.stream()
                    .sorted(sorter)
                    .collect(Collectors.toList());
        }

        return result;
    }

    public boolean exists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return users.containsKey(username.trim().toLowerCase());
    }

    public void update(String username, String newFullName, String newEmail) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }

        String key = username.trim().toLowerCase();
        User existingUser = users.get(key);
        if (existingUser == null) {
            throw new IllegalArgumentException("Пользователь '" + username + "' не найден");
        }

        User updatedUser = User.validate(existingUser.username(), newFullName, newEmail);
        users.put(key, updatedUser);
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование UserManager.]\n");

        UserManager manager = new UserManager();

        System.out.println("[Тест 1]: Добавление пользователя:");
        try {
            User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
            manager.add(user1);
            System.out.println("[v] Пользователь добавлен");
            System.out.println(" > Количество пользователей: " + manager.count());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 2]: Поиск по username:");
        try {
            Optional<User> found = manager.findByUsername("stas_");
            if (found.isPresent()) {
                System.out.println("[v] Найден: " + found.get().format());
            } else {
                System.out.println("[x] Пользователь не найден");
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3]: Поиск по email:");
        try {
            Optional<User> found = manager.findByEmail("stas@example.com");
            if (found.isPresent()) {
                System.out.println("[v] Найден: " + found.get().username());
            } else {
                System.out.println("[x] Пользователь не найден");
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4]: Фильтрация по домену email:");
        try {
            User user2 = User.validate("andrey", "Andrey Lastname", "some@mail.com");
            User user3 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");
            manager.add(user2);
            manager.add(user3);

            List<User> filtered = manager.findByFilter(UserFilters.byEmailDomain("@mail.com"));
            System.out.println("[v] Найдено пользователей: " + filtered.size());
            for (User u : filtered) {
                System.out.println(" > " + u.username());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5]: Сортировка по полному имени:");
        try {
            List<User> sorted = manager.findAll(null, UserSorters.byFullName());
            for (User u : sorted) {
                System.out.println(" > " + u.fullName());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 6]: Обновление пользователя:");
        try {
            manager.update("stas_", "Stas Makarov", "stas@example.com");
            Optional<User> updated = manager.findByUsername("stas_");
            if (updated.isPresent()) {
                System.out.println("[v] Обновлен: " + updated.get().format());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 7]: Удаление пользователя:");
        try {
            User toRemove = manager.findByUsername("anton").orElseThrow();
            boolean removed = manager.remove(toRemove);
            System.out.println("[v] Удалён anton: " + removed);
            System.out.println(" > Количество пользователей: " + manager.count());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 8]: Проверка существования:");
        System.out.println("[v] stas_ (существует): " + manager.exists("stas_"));
        System.out.println("[v] username (не существует): " + manager.exists("username"));
        System.out.println();

        System.out.println("[Тест 9]: Комбинированный фильтр и сортировка:");
        try {
            UserFilter filter = UserFilters.byEmailDomain("@mail.com")
                    .or(UserFilters.byUsernameContains("sta"));
            List<User> result = manager.findAll(filter, UserSorters.byUsername());
            System.out.println("[v] Найдено: " + result.size() + " пользователей");
            for (User u : result) {
                System.out.println(" > " + u.username() + " (" + u.email() + ")");
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}