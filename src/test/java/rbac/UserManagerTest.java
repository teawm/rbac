package rbac;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

    @Test
    void testAddUser() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");

        manager.add(user1);

        assertEquals(1, manager.count(), "Количество пользователей должно быть 1");
        assertTrue(manager.exists("stas_"), "Пользователь должен существовать");
    }

    @Test
    void testFindByUsername() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        manager.add(user1);

        Optional<User> found = manager.findByUsername("stas_");

        assertTrue(found.isPresent(), "Пользователь должен быть найден");
        assertEquals("Stas Makarov", found.get().fullName());
    }

    @Test
    void testFindByEmail() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        manager.add(user1);

        Optional<User> found = manager.findByEmail("stas@example.com");

        assertTrue(found.isPresent(), "Пользователь должен быть найден");
        assertEquals("stas_", found.get().username());
    }

    @Test
    void testFindByFilterByEmailDomain() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("andrey", "Andrey Lastname", "some@mail.com");
        User user3 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");
        manager.add(user1);
        manager.add(user2);
        manager.add(user3);

        List<User> filtered = manager.findByFilter(UserFilters.byEmailDomain("@mail.com"));

        assertEquals(2, filtered.size(), "Должно быть найдено 2 пользователя");
        assertTrue(filtered.stream().anyMatch(u -> u.username().equals("andrey")));
        assertTrue(filtered.stream().anyMatch(u -> u.username().equals("anton")));
    }

    @Test
    void testSortByFullName() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas", "Stas User", "stas@example.com");
        User user2 = User.validate("anton", "Anton User", "anton@example.com");
        User user3 = User.validate("cedric", "Cedric Character", "cedric@example.com");
        manager.add(user1);
        manager.add(user2);
        manager.add(user3);

        List<User> sorted = manager.findAll(null, UserSorters.byFullName());

        assertEquals("Anton User", sorted.get(0).fullName());
        assertEquals("Cedric Character", sorted.get(1).fullName());
        assertEquals("Stas User", sorted.get(2).fullName());
    }

    @Test
    void testUpdateUser() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        manager.add(user1);

        manager.update("stas_", "Stas Makarov", "stas@example.com");

        Optional<User> updated = manager.findByUsername("stas_");
        assertTrue(updated.isPresent());
        assertEquals("stas@example.com", updated.get().email());
    }

    @Test
    void testRemoveUser() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");
        manager.add(user1);
        manager.add(user2);

        boolean removed = manager.remove(user2);

        assertTrue(removed, "Пользователь должен быть удалён");
        assertEquals(1, manager.count(), "Количество пользователей должно быть 1");
        assertFalse(manager.exists("anton"), "Пользователь не должен существовать");
    }

    @Test
    void testExists() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        manager.add(user1);

        assertTrue(manager.exists("stas_"), "Пользователь должен существовать");
        assertFalse(manager.exists("nonexistent"), "Неизвестный пользователь не должен существовать");
    }

    @Test
    void testCombinedFilterAndSort() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("andrey", "Andrey Lastname", "some@mail.com");
        User user3 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");
        manager.add(user1);
        manager.add(user2);
        manager.add(user3);

        UserFilter filter = UserFilters.byEmailDomain("@mail.com")
                .or(UserFilters.byUsernameContains("sta"));
        List<User> result = manager.findAll(filter, UserSorters.byUsername());

        assertEquals(3, result.size(), "Должно быть найдено 3 пользователя");
        assertEquals("andrey", result.get(0).username());
        assertEquals("anton", result.get(1).username());
        assertEquals("stas_", result.get(2).username());
    }

    @Test
    void testDuplicateUserThrowsException() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        manager.add(user1);

        User duplicate = User.validate("stas_", "Another User", "another@example.com");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.add(duplicate);
        });
    }

    @Test
    void testNullUserThrowsException() {
        UserManager manager = new UserManager();

        assertThrows(IllegalArgumentException.class, () -> {
            manager.add(null);
        });
    }

    @Test
    void testFindById() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        manager.add(user1);

        Optional<User> found = manager.findById("stas_");

        assertTrue(found.isPresent());
        assertEquals("stas_", found.get().username());
    }

    @Test
    void testClear() {
        UserManager manager = new UserManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");
        manager.add(user1);
        manager.add(user2);

        manager.clear();

        assertEquals(0, manager.count(), "Количество пользователей должно быть 0");
    }
}