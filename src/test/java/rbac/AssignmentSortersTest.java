package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentSortersTest {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testSortByUsername() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@email.com");
        User user3 = User.validate("andrey", "Andrey Lastnameov", "coolname@mail.ru");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second assignment");
        AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Third assignment");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );
        PermanentAssignment pa2 = new PermanentAssignment(user3, viewerRole, meta3);

        RoleAssignment[] assignments = {pa1, ta1, pa2};
        java.util.Arrays.sort(assignments, AssignmentSorters.byUsername());

        assertEquals("andrey", assignments[0].user().username());
        assertEquals("anton", assignments[1].user().username());
        assertEquals("stas_", assignments[2].user().username());
    }

    @Test
    void testSortByRoleName() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@email.com");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second assignment");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );

        RoleAssignment[] assignments = {pa1, ta1};
        java.util.Arrays.sort(assignments, AssignmentSorters.byRoleName());

        assertEquals("Administrator", assignments[0].role().name());
        assertEquals("Moderator", assignments[1].role().name());
    }

    @Test
    void testSortByAssignmentDate() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@email.com");
        User user3 = User.validate("andrey", "Andrey Lastnameov", "coolname@mail.ru");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second assignment");
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Third assignment");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );
        PermanentAssignment pa2 = new PermanentAssignment(user3, viewerRole, meta3);

        RoleAssignment[] assignments = {pa1, ta1, pa2};
        java.util.Arrays.sort(assignments, AssignmentSorters.byAssignmentDate());

        assertTrue(pa1.metadata().assignedAt().compareTo(ta1.metadata().assignedAt()) <= 0);
        assertTrue(ta1.metadata().assignedAt().compareTo(pa2.metadata().assignedAt()) <= 0);
    }

    @Test
    void testSortByType() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@email.com");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second assignment");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );

        RoleAssignment[] assignments = {pa1, ta1};
        java.util.Arrays.sort(assignments, AssignmentSorters.byType());

        assertEquals("PERMANENT", assignments[0].assignmentType());
        assertEquals("TEMPORARY", assignments[1].assignmentType());
    }

    @Test
    void testSortByActiveStatus() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@email.com");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second assignment");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                "2025-02-16 00:00"
        );

        RoleAssignment[] assignments = {pa1, ta1};
        java.util.Arrays.sort(assignments, AssignmentSorters.byActiveStatus());

        assertTrue(assignments[0].isActive());
        assertFalse(assignments[1].isActive());
    }

    @Test
    void testCombinedSortByUsernameThenRoleName() {
        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@email.com");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second assignment");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DATE_FORMATTER)
        );

        RoleAssignment[] assignments = {pa1, ta1};
        java.util.Arrays.sort(assignments,
                AssignmentSorters.byUsername().thenComparing(AssignmentSorters.byRoleName()));

        assertEquals("anton", assignments[0].user().username());
        assertEquals("stas_", assignments[1].user().username());
    }
}