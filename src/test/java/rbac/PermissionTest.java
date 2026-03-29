package rbac;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PermissionTest {

    @Test
    void testValidPermissionWithNormalization() {
        Permission perm = new Permission("read", "Users", "Can view user list");
        assertEquals("READ", perm.name());
        assertEquals("users", perm.resource());
        assertEquals("Can view user list", perm.description());
        assertEquals("READ on users: Can view user list", perm.format());
    }

    @Test
    void testWhitespaceRemovalInName() {
        Permission perm = new Permission("WRITE ACCESS", "reports", "Full report access");
        assertEquals("WRITEACCESS", perm.name());
        assertEquals("reports", perm.resource());
        assertEquals("Full report access", perm.description());
    }

    @Test
    void testEmptyDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Permission("DELETE", "settings", "   ");
        });
    }

    @Test
    void testPatternMatching() {
        Permission perm = new Permission("USER_ADMIN", "users", "Full user management");

        assertTrue(perm.matches("ADMIN", null), "Должно находить 'ADMIN' в названии");
        assertTrue(perm.matches(null, "user"), "Должно находить 'user' в ресурсе");
        assertFalse(perm.matches("WRONG", "settings"), "Не должно находить неверные параметры");
    }

    @Test
    void testInvalidCharactersInName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Permission("READ!", "data", "Access to data");
        });
    }

    @Test
    void testEmptyResourceThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Permission("VIEW", "", "View only");
        });
    }

    @Test
    void testNullFieldsThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Permission(null, "users", "description");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Permission("READ", null, "description");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Permission("READ", "users", null);
        });
    }

    @Test
    void testEmptyNameAfterTrimming() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Permission("   ", "users", "description");
        });
    }
}