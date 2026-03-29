package rbac;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserFiltersTest {

    @Test
    void testByUsername() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        User user2 = User.validate("anton_shtirlz", "Anton Shtirlitz", "anton@mail.com");

        UserFilter filter = UserFilters.byUsername("stas_");

        assertTrue(filter.test(user1), "Пользователь с именем 'stas_' должен проходить фильтр");
        assertFalse(filter.test(user2), "Пользователь с другим именем не должен проходить фильтр");
    }

    @Test
    void testByUsernameContains() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        User user2 = User.validate("anton_shtirlz", "Anton Shtirlitz", "anton@mail.com");
        User user3 = User.validate("username_", "Firstname Lastname", "username@email.ru");

        UserFilter filter = UserFilters.byUsernameContains("_");

        assertTrue(filter.test(user1), "Пользователь с '_' в имени должен проходить фильтр");
        assertTrue(filter.test(user2), "Пользователь с '_' в имени должен проходить фильтр");
        assertTrue(filter.test(user3), "Пользователь с '_' в имени должен проходить фильтр");
    }

    @Test
    void testByEmailDomain() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        User user2 = User.validate("anton_shtirlz", "Anton Shtirlitz", "anton@mail.com");
        User user3 = User.validate("username_", "Firstname Lastname", "username@email.ru");

        UserFilter filter = UserFilters.byEmailDomain("@mail.com");

        assertTrue(filter.test(user1), "Пользователь с доменом @mail.com должен проходить фильтр");
        assertTrue(filter.test(user2), "Пользователь с доменом @mail.com должен проходить фильтр");
        assertFalse(filter.test(user3), "Пользователь с другим доменом не должен проходить фильтр");
    }

    @Test
    void testCombinedFilterWithAnd() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        User user2 = User.validate("anton_shtirlz", "Anton Shtirlitz", "anton@mail.com");
        User user3 = User.validate("username_", "Firstname Lastname", "username@email.ru");

        UserFilter filter = UserFilters.byUsernameContains("_")
                .and(UserFilters.byEmailDomain("@mail.com"));

        assertTrue(filter.test(user1), "Пользователь с '_' в имени и @mail.com должен проходить фильтр");
        assertTrue(filter.test(user2), "Пользователь с '_' в имени и @mail.com должен проходить фильтр");
        assertFalse(filter.test(user3), "Пользователь без @mail.com не должен проходить фильтр");
    }

    @Test
    void testCombinedFilterWithOr() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        User user2 = User.validate("anton_shtirlz", "Anton Shtirlitz", "anton@mail.com");
        User user3 = User.validate("username_", "Firstname Lastname", "username@email.ru");

        UserFilter filter = UserFilters.byFullNameContains("Stas")
                .or(UserFilters.byFullNameContains("Anton"));

        assertTrue(filter.test(user1), "Пользователь с 'Stas' в полном имени должен проходить фильтр");
        assertTrue(filter.test(user2), "Пользователь с 'Anton' в полном имени должен проходить фильтр");
        assertFalse(filter.test(user3), "Пользователь без 'Stas' или 'Anton' не должен проходить фильтр");
    }

    @Test
    void testNegatedFilter() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        User user2 = User.validate("anton_shtirlz", "Anton Shtirlitz", "anton@mail.com");
        User user3 = User.validate("username_", "Firstname Lastname", "username@email.ru");

        UserFilter filter = UserFilters.byEmailDomain("@mail.com").negate();

        assertFalse(filter.test(user1), "Пользователь с @mail.com не должен проходить инвертированный фильтр");
        assertFalse(filter.test(user2), "Пользователь с @mail.com не должен проходить инвертированный фильтр");
        assertTrue(filter.test(user3), "Пользователь без @mail.com должен проходить инвертированный фильтр");
    }

    @Test
    void testByFullNameContains() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        User user2 = User.validate("anton_shtirlz", "Anton Shtirlitz", "anton@mail.com");

        UserFilter filter = UserFilters.byFullNameContains("Stas");

        assertTrue(filter.test(user1), "Пользователь с 'Stas' в полном имени должен проходить фильтр");
        assertFalse(filter.test(user2), "Пользователь без 'Stas' не должен проходить фильтр");
    }

    @Test
    void testNullUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            UserFilters.byUsername(null);
        });
    }

    @Test
    void testEmptyUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            UserFilters.byUsername("");
        });
    }

    @Test
    void testNullEmailDomainThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            UserFilters.byEmailDomain(null);
        });
    }
}