package test.java.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PermanentAssignmentTest {

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testCreatePermanentAssignment() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        Role adminRole = new Role("Admin", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        AssignmentMetadata meta = AssignmentMetadata.now("system_admin", "Initial setup");

        PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);

        assertNotNull(pa.assignmentId());
        assertTrue(pa.isActive());
        assertEquals("PERMANENT", pa.assignmentType());
        assertTrue(pa.summary().contains("PERMANENT"));
    }

    @Test
    void testAssignmentIsActiveByDefault() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        Role adminRole = new Role("Admin", "Full system access");
        AssignmentMetadata meta = AssignmentMetadata.now("system_admin", "Initial setup");

        PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);

        assertTrue(pa.isActive(), "Назначение должно быть активным по умолчанию");
    }

    @Test
    void testRevokeAssignment() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        Role adminRole = new Role("Admin", "Full system access");
        AssignmentMetadata meta = AssignmentMetadata.now("system_admin", "Initial setup");

        PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);
        pa.revoke();

        assertFalse(pa.isActive(), "Назначение должно быть неактивным после отзыва");
        assertTrue(pa.isRevoked(), "Назначение должно быть помечено как отозванное");
        assertTrue(pa.summary().contains("INACTIVE"));
    }

    @Test
    void testGettersReturnCorrectValues() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        Role adminRole = new Role("Admin", "Full system access");
        AssignmentMetadata meta = AssignmentMetadata.now("system_admin", "Initial setup");

        PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);

        assertEquals(testUser, pa.user());
        assertEquals(adminRole, pa.role());
        assertEquals(meta, pa.metadata());
        assertEquals("system_admin", pa.metadata().assignedBy());
    }

    @Test
    void testEqualsAndHashCodeByAssignmentId() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        Role adminRole = new Role("Admin", "Full system access");
        AssignmentMetadata meta = AssignmentMetadata.now("system_admin", "Initial setup");

        String fixedId = "test-p-id-001";
        PermanentAssignment pa1 = new PermanentAssignment(fixedId, testUser, adminRole, meta);
        PermanentAssignment pa2 = new PermanentAssignment(fixedId, testUser, adminRole, meta);

        assertEquals(pa1, pa2, "Объекты с одинаковым assignmentId должны быть равны");
        assertEquals(pa1.hashCode(), pa2.hashCode(), "HashCode должен совпадать для одинаковых assignmentId");
    }
}