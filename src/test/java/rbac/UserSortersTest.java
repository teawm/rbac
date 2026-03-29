package test.java.rbac;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class UserSortersTest {

    @Test
    void testSortByUsername() {
        User user1 = User.validate("stas", "Stas User", "stas@example.com");
        User user2 = User.validate("anton", "Anton User", "anton@example.com");
        User user3 = User.validate("cedric", "Cedric Character", "cedric@example.com");

        User[] users = {user1, user2, user3};
        Arrays.sort(users, UserSorters.byUsername());

        assertEquals("anton", users[0].username());
        assertEquals("cedric", users[1].username());
        assertEquals("stas", users[2].username());
    }

    @Test
    void testSortByFullName() {
        User user1 = User.validate("stas", "Stas User", "stas@example.com");
        User user2 = User.validate("anton", "Anton User", "anton@example.com");
        User user3 = User.validate("cedric", "Cedric Character", "cedric@example.com");

        User[] users = {user1, user2, user3};
        Arrays.sort(users, UserSorters.byFullName());

        assertEquals("Anton User", users[0].fullName());
        assertEquals("Cedric Character", users[1].fullName());
        assertEquals("Stas User", users[2].fullName());
    }

    @Test
    void testSortByEmail() {
        User user1 = User.validate("stas", "Stas User", "stas@example.com");
        User user2 = User.validate("anton", "Anton User", "anton@example.com");
        User user3 = User.validate("cedric", "Cedric Character", "cedric@example.com");

        User[] users = {user1, user2, user3};
        Arrays.sort(users, UserSorters.byEmail());

        assertEquals("anton@example.com", users[0].email());
        assertEquals("cedric@example.com", users[1].email());
        assertEquals("stas@example.com", users[2].email());
    }

    @Test
    void testSortByUsernameLength() {
        User user1 = User.validate("stas", "Stas User", "stas@example.com");
        User user2 = User.validate("anton", "Anton User", "anton@example.com");
        User user3 = User.validate("cedric", "Cedric Character", "cedric@example.com");

        User[] users = {user1, user2, user3};
        Arrays.sort(users, UserSorters.byUsernameLength());

        assertEquals(4, users[0].username().length());
        assertEquals(5, users[1].username().length());
        assertEquals(6, users[2].username().length());
    }

    @Test
    void testCombinedSortByFullNameThenUsername() {
        User user1 = User.validate("stas", "Stas User", "stas@example.com");
        User user2 = User.validate("anton", "Anton User", "anton@example.com");
        User user3 = User.validate("cedric", "Cedric Character", "cedric@example.com");

        User[] users = {user1, user2, user3};
        Arrays.sort(users, UserSorters.byFullName().thenComparing(UserSorters.byUsername()));

        assertEquals("Anton User", users[0].fullName());
        assertEquals("Cedric Character", users[1].fullName());
        assertEquals("Stas User", users[2].fullName());
    }
}