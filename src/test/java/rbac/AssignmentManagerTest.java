package test.java.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentManagerTest {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testAddPermanentAssignment() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        assertEquals(1, manager.count(), "Количество назначений должно быть 1");
    }

    @Test
    void testDuplicateAssignmentThrowsException() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        PermanentAssignment pa2 = new PermanentAssignment(user1, adminRole, meta2);

        assertThrows(IllegalArgumentException.class, () -> {
            manager.add(pa2);
        });
    }

    @Test
    void testFindByUser() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.ru");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );
        manager.add(ta1);

        List<RoleAssignment> userAssignments = manager.findByUser(user2);

        assertEquals(1, userAssignments.size(), "Должно быть найдено 1 назначение для пользователя");
        assertEquals("Moderator", userAssignments.get(0).role().name());
    }

    @Test
    void testGetActiveAndExpiredAssignments() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        List<RoleAssignment> active = manager.getActiveAssignments();
        List<RoleAssignment> expired = manager.getExpiredAssignments();

        assertEquals(1, active.size(), "Должно быть 1 активное назначение");
        assertEquals(0, expired.size(), "Не должно быть истёкших назначений");
    }

    @Test
    void testUserHasRole() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        assertTrue(manager.userHasRole(user1, adminRole), "Пользователь должен иметь роль Administrator");
        assertFalse(manager.userHasRole(user1, new Role("Viewer", "Read-only access")),
                "Пользователь не должен иметь роль Viewer");
    }

    @Test
    void testUserHasPermission() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        assertTrue(manager.userHasPermission(user1, "READ", "users"),
                "Пользователь должен иметь право READ on users");
        assertFalse(manager.userHasPermission(user1, "DELETE", "users"),
                "Пользователь не должен иметь право DELETE on users");
    }

    @Test
    void testGetUserPermissions() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        Set<Permission> permissions = manager.getUserPermissions(user1);

        assertEquals(2, permissions.size(), "Пользователь должен иметь 2 права");
        assertTrue(permissions.stream().anyMatch(p -> p.name().equals("READ") && p.resource().equals("users")));
        assertTrue(permissions.stream().anyMatch(p -> p.name().equals("WRITE") && p.resource().equals("users")));
    }

    @Test
    void testRevokePermanentAssignment() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        manager.revokeAssignment(pa1.assignmentId());

        assertFalse(manager.userHasRole(user1, adminRole), "Пользователь не должен иметь роль после отзыва");
        assertEquals(0, manager.getActiveAssignments().size(), "Не должно быть активных назначений");
    }

    @Test
    void testExtendTemporaryAssignment() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Content access");

        String initial = LocalDateTime.now().plusHours(1).format(DATE_FORMATTER);
        TemporaryAssignment ta1 = new TemporaryAssignment(user1, moderRole, meta1, initial);
        manager.add(ta1);

        String extended = LocalDateTime.now().plusDays(30).format(DATE_FORMATTER);
        manager.extendTemporaryAssignment(ta1.assignmentId(), extended);

        Optional<RoleAssignment> updated = manager.findById(ta1.assignmentId());
        assertTrue(updated.isPresent());
    }

    @Test
    void testGetStatistics() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.ru");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );
        manager.add(ta1);

        String stats = manager.getStatistics();

        assertTrue(stats.contains("Всего назначений: 2"), "Статистика должна содержать общее количество");
        assertTrue(stats.contains("Активных: 2"), "Статистика должна содержать количество активных");
        assertTrue(stats.contains("Постоянных: 1"), "Статистика должна содержать количество постоянных");
        assertTrue(stats.contains("Временных: 1"), "Статистика должна содержать количество временных");
    }

    @Test
    void testFindByFilterWithType() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.ru");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );
        manager.add(ta1);

        List<RoleAssignment> temporary = manager.findByFilter(
                AssignmentFilters.byType("TEMPORARY")
        );

        assertEquals(1, temporary.size(), "Должно быть найдено 1 временное назначение");
        assertEquals("TEMPORARY", temporary.get(0).assignmentType());
    }

    @Test
    void testCombinedFilterAndSort() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.ru");
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );
        manager.add(ta1);

        AssignmentFilter filter = AssignmentFilters.byType("TEMPORARY")
                .and(AssignmentFilters.activeOnly());
        List<RoleAssignment> result = manager.findAll(
                filter,
                AssignmentSorters.byUsername()
        );

        assertEquals(1, result.size(), "Должно быть найдено 1 назначение");
        assertEquals("anton", result.get(0).user().username());
    }

    @Test
    void testNullAssignmentThrowsException() {
        AssignmentManager manager = new AssignmentManager();

        assertThrows(IllegalArgumentException.class, () -> {
            manager.add(null);
        });
    }

    @Test
    void testFindById() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        Optional<RoleAssignment> found = manager.findById(pa1.assignmentId());

        assertTrue(found.isPresent());
        assertEquals(pa1.assignmentId(), found.get().assignmentId());
    }

    @Test
    void testClear() {
        AssignmentManager manager = new AssignmentManager();
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        manager.add(pa1);

        manager.clear();

        assertEquals(0, manager.count(), "Количество назначений должно быть 0");
    }
}