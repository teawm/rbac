package rbac;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testCreateRoleWithPermissions() {
        Role admin = new Role("Administrator", "Full system access");
        admin.addPermission(new Permission("READ", "users", "View user list"));
        admin.addPermission(new Permission("WRITE", "users", "Create/edit users"));
        admin.addPermission(new Permission("DELETE", "users", "Delete users"));

        assertEquals("Administrator", admin.name());
        assertEquals("Full system access", admin.description());
        assertEquals(3, admin.getPermissions().size());
        assertTrue(admin.hasPermission("READ", "users"));
        assertTrue(admin.hasPermission("WRITE", "users"));
        assertTrue(admin.hasPermission("DELETE", "users"));
    }

    @Test
    void testDuplicateRoleNameThrowsException() {
        new Role("Administrator", "First admin");

        assertThrows(IllegalArgumentException.class, () -> {
            new Role("Administrator", "Second admin");
        });
    }

    @Test
    void testCheckPermissionByObject() {
        Role viewer = new Role("Viewer", "Read-only access");
        Permission readUsers = new Permission("READ", "users", "View users");
        viewer.addPermission(readUsers);

        assertTrue(viewer.hasPermission(readUsers));
        assertFalse(viewer.hasPermission(new Permission("WRITE", "users", "Edit users")));
    }

    @Test
    void testCheckPermissionByNameAndResource() {
        Role editor = new Role("Editor", "Content editor");
        editor.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        assertTrue(editor.hasPermission("WRITE", "articles"));
        assertFalse(editor.hasPermission("DELETE", "articles"));
    }

    @Test
    void testEmptyDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Role("TestRole", "   ");
        });
    }

    @Test
    void testUnmodifiablePermissionsSet() {
        Role role = new Role("Tester", "Test role");
        role.addPermission(new Permission("TEST", "system", "Run tests"));

        Set<Permission> perms = role.getPermissions();
        assertThrows(UnsupportedOperationException.class, () -> {
            perms.add(new Permission("HACK", "system", "fail"));
        });
    }

    @Test
    void testChangeRoleName() {
        Role oldRole = new Role("OldName", "Description");
        oldRole.setName("NewName");

        assertEquals("NewName", oldRole.name());
    }

    @Test
    void testChangeRoleNameToDuplicateThrowsException() {
        new Role("ExistingRole", "Description");
        Role role = new Role("UniqueRole", "Description");

        assertThrows(IllegalArgumentException.class, () -> {
            role.setName("ExistingRole");
        });
    }

    @Test
    void testEqualsAndHashCode() {
        // Используем разные имена, так как одинаковые имена запрещены
        Role role1 = new Role("Role1", "Description");
        Role role2 = new Role("Role2", "Another description");
        Role role3 = new Role("Role3", "Description");

        // Проверяем, что разные объекты не равны (по уникальному id)
        assertNotEquals(role1, role2);
        assertNotEquals(role1, role3);

        // Проверяем, что объект равен сам себе
        assertEquals(role1, role1);
        assertEquals(role1.hashCode(), role1.hashCode());
    }

    @Test
    void testFormatOutput() {
        Role role = new Role("TestRole", "Test description");
        role.addPermission(new Permission("READ", "data", "Access data"));

        String formatted = role.format();
        assertTrue(formatted.contains("TestRole"));
        assertTrue(formatted.contains("Test description"));
        assertTrue(formatted.contains("READ on data"));
    }

    @Test
    void testNullPermissionThrowsException() {
        Role role = new Role("TestRole", "Description");

        assertThrows(IllegalArgumentException.class, () -> {
            role.addPermission(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            role.removePermission(null);
        });
    }

    @Test
    void testEmptyNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Role("", "Description");
        });
    }

    @Test
    void testNullNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Role(null, "Description");
        });
    }

    @Test
    void testNullDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Role("TestRole", null);
        });
    }
}