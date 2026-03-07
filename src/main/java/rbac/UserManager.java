package rbac;

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
}