package test.java.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RoleManagerTest {

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testAddRole() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

        manager.add(adminRole);

        assertEquals(1, manager.count(), "Количество ролей должно быть 1");
        assertTrue(manager.exists("Administrator"), "Роль должна существовать");
    }

    @Test
    void testFindByName() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        manager.add(adminRole);

        Optional<Role> found = manager.findByName("Administrator");

        assertTrue(found.isPresent(), "Роль должна быть найдена");
        assertEquals("Full system access", found.get().description());
    }

    @Test
    void testFindByFilterWithPermission() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        manager.add(adminRole);

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));
        manager.add(moderRole);

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
        manager.add(viewerRole);

        List<Role> filtered = manager.findByFilter(
                RoleFilters.hasPermission("WRITE", "articles")
        );

        assertEquals(1, filtered.size(), "Должна быть найдена 1 роль");
        assertEquals("Moderator", filtered.get(0).name());
    }

    @Test
    void testSortByPermissionCountDescending() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        adminRole.addPermission(new Permission("DELETE", "users", "Delete users"));
        adminRole.addPermission(new Permission("READ", "logs", "View logs"));
        manager.add(adminRole);

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));
        manager.add(moderRole);

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
        manager.add(viewerRole);

        List<Role> sorted = manager.findAll(null, RoleSorters.byPermissionCountDesc());

        assertEquals(4, sorted.get(0).getPermissions().size());
        assertEquals(2, sorted.get(1).getPermissions().size());
        assertEquals(1, sorted.get(2).getPermissions().size());
    }

    @Test
    void testAddPermissionToExistingRole() {
        RoleManager manager = new RoleManager();
        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
        manager.add(viewerRole);

        manager.addPermissionToRole("Viewer", new Permission("READ", "logs", "View logs"));

        Optional<Role> updated = manager.findByName("Viewer");
        assertTrue(updated.isPresent());
        assertEquals(2, updated.get().getPermissions().size());
    }

    @Test
    void testFindRolesWithPermission() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        manager.add(adminRole);

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
        manager.add(viewerRole);

        List<Role> rolesWithRead = manager.findRolesWithPermission("READ", "users");

        assertEquals(1, rolesWithRead.size(), "Должна быть найдена 1 роль");
        assertEquals("Administrator", rolesWithRead.get(0).name());
    }

    @Test
    void testRemoveRole() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        manager.add(adminRole);
        Role viewerRole = new Role("Viewer", "Read-only access");
        manager.add(viewerRole);

        Optional<Role> toRemove = manager.findByName("Viewer");
        assertTrue(toRemove.isPresent());

        boolean removed = manager.remove(toRemove.get());

        assertTrue(removed, "Роль должна быть удалена");
        assertEquals(1, manager.count(), "Количество ролей должно быть 1");
        assertFalse(manager.exists("Viewer"), "Роль не должна существовать");
    }

    @Test
    void testCombinedFilterAndSort() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        manager.add(adminRole);

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));
        manager.add(moderRole);

        RoleFilter filter = RoleFilters.byNameContains("admin")
                .or(RoleFilters.hasAtLeastNPermissions(2));
        List<Role> result = manager.findAll(filter, RoleSorters.byName());

        assertEquals(2, result.size(), "Должно быть найдено 2 роли");
        assertEquals("Administrator", result.get(0).name());
        assertEquals("Moderator", result.get(1).name());
    }

    @Test
    void testExists() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        manager.add(adminRole);

        assertTrue(manager.exists("Administrator"), "Роль должна существовать");
        assertFalse(manager.exists("NonExistent"), "Неизвестная роль не должна существовать");
    }

    @Test
    void testNullRoleThrowsException() {
        RoleManager manager = new RoleManager();

        assertThrows(IllegalArgumentException.class, () -> {
            manager.add(null);
        });
    }

    @Test
    void testFindById() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        manager.add(adminRole);

        Optional<Role> found = manager.findById(adminRole.id());

        assertTrue(found.isPresent());
        assertEquals("Administrator", found.get().name());
    }

    @Test
    void testClear() {
        RoleManager manager = new RoleManager();
        Role adminRole = new Role("Administrator", "Full system access");
        Role viewerRole = new Role("Viewer", "Read-only access");
        manager.add(adminRole);
        manager.add(viewerRole);

        manager.clear();

        assertEquals(0, manager.count(), "Количество ролей должно быть 0");
    }
}