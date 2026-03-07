package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class TemporaryAssignmentTest {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Очищаем статические данные перед каждым тестом
    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testCreateTemporaryAssignmentWithFutureDate() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@email.example");
        Role auditorRole = new Role("Security", "Security audit role");
        auditorRole.addPermission(new Permission("READ", "logs", "View audit logs"));
        AssignmentMetadata meta = AssignmentMetadata.now("security_employee", "Quarterly audit");

        String future = LocalDateTime.now().plusHours(1).format(DATE_FORMATTER);
        TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, future);

        assertNotNull(ta.assignmentId());
        assertTrue(ta.isActive(), "Назначение должно быть активным");
        assertEquals("TEMPORARY", ta.assignmentType());
        assertEquals(future, ta.expiresAt());
        assertTrue(ta.summary().contains("TEMPORARY"));
    }

    @Test
    void testExpiredTemporaryAssignment() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@email.example");
        Role auditorRole = new Role("Security", "Security audit role");
        AssignmentMetadata meta = AssignmentMetadata.now("security_employee", "Quarterly audit");

        String past = "2025-02-16 00:00";
        TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, past);

        assertFalse(ta.isActive(), "Назначение с прошедшей датой должно быть неактивным");
        assertTrue(ta.isExpired(), "Назначение должно быть помечено как истёкшее");
        assertTrue(ta.summary().contains("EXPIRED"));
    }

    @Test
    void testExtendTemporaryAssignment() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@email.example");
        Role auditorRole = new Role("Security", "Security audit role");
        AssignmentMetadata meta = AssignmentMetadata.now("security_employee", "Quarterly audit");

        String initial = LocalDateTime.now().plusHours(1).format(DATE_FORMATTER);
        String extended = LocalDateTime.now().plusDays(7).format(DATE_FORMATTER);

        TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, initial);
        ta.extend(extended);

        assertEquals(extended, ta.expiresAt(), "Дата истечения должна быть обновлена");
    }

    @Test
    void testInvalidDateFormatThrowsException() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@email.example");
        Role auditorRole = new Role("Security", "Security audit role");
        AssignmentMetadata meta = AssignmentMetadata.now("security_employee", "Quarterly audit");

        assertThrows(IllegalArgumentException.class, () -> {
            new TemporaryAssignment(testUser, auditorRole, meta, "2026/02/15, 19:45");
        });
    }

    @Test
    void testTimeRemainingCalculation() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@email.example");
        Role auditorRole = new Role("Security", "Security audit role");
        AssignmentMetadata meta = AssignmentMetadata.now("security_employee", "Quarterly audit");

        String future = LocalDateTime.now().plusDays(2).plusHours(5).format(DATE_FORMATTER);
        TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, future);

        String remaining = ta.getTimeRemaining();
        assertTrue(remaining.contains("day"), "Должно содержать 'day'");
        assertTrue(remaining.contains("hour"), "Должно содержать 'hour'");
    }

    @Test
    void testAutoRenewFlag() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@email.example");
        Role auditorRole = new Role("Security", "Security audit role");
        AssignmentMetadata meta = AssignmentMetadata.now("security_employee", "Quarterly audit");

        String future = LocalDateTime.now().plusHours(1).format(DATE_FORMATTER);
        TemporaryAssignment ta = new TemporaryAssignment("test-id", testUser, auditorRole, meta, future, true);

        assertTrue(ta.autoRenew(), "Флаг автопродления должен быть установлен");
    }

    @Test
    void testGettersReturnCorrectValues() {
        User testUser = User.validate("stas_", "Stas Makarov", "stas@email.example");
        Role auditorRole = new Role("Security", "Security audit role");
        AssignmentMetadata meta = AssignmentMetadata.now("security_employee", "Quarterly audit");

        String future = LocalDateTime.now().plusHours(1).format(DATE_FORMATTER);
        TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, future);

        assertEquals(testUser, ta.user());
        assertEquals(auditorRole, ta.role());
        assertEquals(meta, ta.metadata());
        assertEquals(future, ta.expiresAt());
    }
}