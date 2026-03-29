package test.java.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentFiltersTest {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testByUser() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");
        PermanentAssignment pa = new PermanentAssignment(user1, adminRole, meta);

        AssignmentFilter filter = AssignmentFilters.byUser(user1);

        assertTrue(filter.test(pa), "Назначение пользователя user1 должно проходить фильтр");
    }

    @Test
    void testByUsername() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");

        Role adminRole = new Role("Administrator", "Full system access");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");
        PermanentAssignment pa = new PermanentAssignment(user1, adminRole, meta);

        AssignmentFilter filter = AssignmentFilters.byUsername("stas_");

        assertTrue(filter.test(pa), "Назначение пользователя 'stas_' должно проходить фильтр");
    }

    @Test
    void testByRole() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");
        Role viewerRole = new Role("Viewer", "Read-only access");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");
        PermanentAssignment pa = new PermanentAssignment(user1, adminRole, meta);

        AssignmentFilter filter = AssignmentFilters.byRole(adminRole);

        assertTrue(filter.test(pa), "Назначение роли Administrator должно проходить фильтр");
    }

    @Test
    void testActiveOnly() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");
        PermanentAssignment pa = new PermanentAssignment(user1, adminRole, meta);

        AssignmentFilter filter = AssignmentFilters.activeOnly();

        assertTrue(filter.test(pa), "Активное назначение должно проходить фильтр");
    }

    @Test
    void testByTypeTemporary() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role moderRole = new Role("Moderator", "Edit content");

        AssignmentMetadata meta = AssignmentMetadata.now("security_officer", "Security audit");
        String expiresAt = LocalDateTime.now().plusDays(7).format(DATE_FORMATTER);
        TemporaryAssignment ta = new TemporaryAssignment(user1, moderRole, meta, expiresAt);

        AssignmentFilter filter = AssignmentFilters.byType("TEMPORARY");

        assertTrue(filter.test(ta), "Временное назначение должно проходить фильтр");
    }

    @Test
    void testAssignedBy() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");
        PermanentAssignment pa = new PermanentAssignment(user1, adminRole, meta);

        AssignmentFilter filter = AssignmentFilters.assignedBy("admin");

        assertTrue(filter.test(pa), "Назначение от 'admin' должно проходить фильтр");
    }

    @Test
    void testCombinedFilterWithAnd() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role adminRole = new Role("Administrator", "Full system access");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");
        PermanentAssignment pa = new PermanentAssignment(user1, adminRole, meta);

        AssignmentFilter filter = AssignmentFilters.byUsername("stas_")
                .and(AssignmentFilters.activeOnly());

        assertTrue(filter.test(pa), "Назначение должно проходить комбинированный фильтр");
    }

    @Test
    void testCombinedFilterWithOr() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");

        Role adminRole = new Role("Administrator", "Full system access");
        Role moderRole = new Role("Moderator", "Edit content");

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("security_officer", "Security audit");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        String expiresAt = LocalDateTime.now().plusDays(7).format(DATE_FORMATTER);
        TemporaryAssignment ta1 = new TemporaryAssignment(user2, moderRole, meta2, expiresAt);

        AssignmentFilter filter = AssignmentFilters.assignedBy("admin")
                .or(AssignmentFilters.byType("TEMPORARY"));

        assertTrue(filter.test(pa1), "Назначение от 'admin' должно проходить фильтр");
        assertTrue(filter.test(ta1), "Временное назначение должно проходить фильтр");
    }

    @Test
    void testExpiringBefore() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        Role moderRole = new Role("Moderator", "Edit content");

        AssignmentMetadata meta = AssignmentMetadata.now("security_officer", "Security audit");
        String expiresAt = "2025-02-16 00:00"; // Прошедшая дата
        TemporaryAssignment ta = new TemporaryAssignment(user1, moderRole, meta, expiresAt);

        AssignmentFilter filter = AssignmentFilters.expiringBefore("2025-02-17 00:00");

        assertTrue(filter.test(ta), "Истёкшее назначение должно проходить фильтр");
    }

    @Test
    void testNullUserThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            AssignmentFilters.byUser(null);
        });
    }

    @Test
    void testEmptyUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            AssignmentFilters.byUsername("");
        });
    }

    @Test
    void testInvalidTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            AssignmentFilters.byType("INVALID");
        });
    }
}