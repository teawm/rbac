package test.java.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoleFiltersTest {

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testByName() {
        Role adminRole = new Role("Administrator", "Full system access");
        Role moderRole = new Role("Moderator", "Edit content");
        Role viewerRole = new Role("Viewer", "Read-only access");

        RoleFilter filter = RoleFilters.byName("Administrator");

        assertTrue(filter.test(adminRole), "Роль с именем 'Administrator' должна проходить фильтр");
        assertFalse(filter.test(moderRole), "Роль с другим именем не должна проходить фильтр");
        assertFalse(filter.test(viewerRole), "Роль с другим именем не должна проходить фильтр");
    }

    @Test
    void testByNameContains() {
        Role adminRole = new Role("Administrator", "Full system access");
        Role moderRole = new Role("Moderator", "Edit content");

        RoleFilter filter = RoleFilters.byNameContains("Moder");

        assertTrue(filter.test(moderRole), "Роль с 'Moder' в названии должна проходить фильтр");
        assertFalse(filter.test(adminRole), "Роль без 'Moder' не должна проходить фильтр");
    }

    @Test
    void testHasPermissionByObject() {
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));

        Permission readUsers = new Permission("READ", "users", "View users");
        RoleFilter filter = RoleFilters.hasPermission(readUsers);

        assertTrue(filter.test(adminRole), "Роль с правом READ on users должна проходить фильтр");
        assertFalse(filter.test(viewerRole), "Роль без этого права не должна проходить фильтр");
    }

    @Test
    void testHasPermissionByNameAndResource() {
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

        RoleFilter filter = RoleFilters.hasPermission("WRITE", "users");

        assertTrue(filter.test(adminRole), "Роль с правом WRITE on users должна проходить фильтр");
    }

    @Test
    void testHasAtLeastNPermissions() {
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        adminRole.addPermission(new Permission("DELETE", "users", "Delete users"));
        adminRole.addPermission(new Permission("READ", "logs", "View logs"));

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));

        RoleFilter filter = RoleFilters.hasAtLeastNPermissions(3);

        assertTrue(filter.test(adminRole), "Роль с 4 правами должна проходить фильтр");
        assertFalse(filter.test(viewerRole), "Роль с 1 правом не должна проходить фильтр");
    }

    @Test
    void testCombinedFilterWithAnd() {
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        RoleFilter filter = RoleFilters.byNameContains("Admin")
                .and(RoleFilters.hasPermission("READ", "users"));

        assertTrue(filter.test(adminRole), "Роль с 'Admin' в названии и правом READ on users должна проходить фильтр");
    }

    @Test
    void testCombinedFilterWithOr() {
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        RoleFilter filter = RoleFilters.hasPermission("WRITE", "users")
                .or(RoleFilters.hasPermission("WRITE", "articles"));

        assertTrue(filter.test(adminRole), "Роль с правом WRITE on users должна проходить фильтр");
        assertTrue(filter.test(moderRole), "Роль с правом WRITE on articles должна проходить фильтр");
    }

    @Test
    void testNegatedFilter() {
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

        RoleFilter filter = RoleFilters.hasAtLeastNPermissions(2).negate();

        assertFalse(filter.test(adminRole), "Роль с 2+ правами не должна проходить инвертированный фильтр");
    }

    @Test
    void testNullNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RoleFilters.byName(null);
        });
    }

    @Test
    void testEmptyNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RoleFilters.byName("");
        });
    }

    @Test
    void testNullPermissionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RoleFilters.hasPermission((Permission) null);
        });
    }

    @Test
    void testNegativeNThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RoleFilters.hasAtLeastNPermissions(-1);
        });
    }
}