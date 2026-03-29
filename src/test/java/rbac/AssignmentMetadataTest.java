package rbac;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AssignmentMetadataTest {

    @Test
    void testCreateMetadataWithReason() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");

        assertEquals("admin", meta.assignedBy());
        assertNotNull(meta.assignedAt());
        assertEquals("Initial setup", meta.reason());

        assertTrue(meta.assignedAt().length() >= 16);
    }

    @Test
    void testCreateMetadataWithoutReason() {
        AssignmentMetadata meta = AssignmentMetadata.now("system_admin", null);

        assertEquals("system_admin", meta.assignedBy());
        assertNull(meta.reason());
    }

    @Test
    void testEmptyAssignedByThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            AssignmentMetadata.now("", "Test reason");
        });
    }

    @Test
    void testNullAssignedByThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            AssignmentMetadata.now(null, "Test reason");
        });
    }

    @Test
    void testCreateMetadataWithCustomDate() {
        AssignmentMetadata meta = new AssignmentMetadata(
                "Administrator",
                "2026-02-15 23:00",
                "Important reasons"
        );

        assertEquals("Administrator", meta.assignedBy());
        assertEquals("2026-02-15 23:00", meta.assignedAt());
        assertEquals("Important reasons", meta.reason());
    }

    @Test
    void testWhitespaceNormalization() {
        AssignmentMetadata meta = new AssignmentMetadata(
                " Moderator ",
                "2026-02-15 23:01 ",
                " Fake reason just for testing "
        );

        assertEquals("Moderator", meta.assignedBy());
        assertEquals("2026-02-15 23:01", meta.assignedAt());
        assertEquals("Fake reason just for testing", meta.reason());
    }

    @Test
    void testFormatWithReason() {
        AssignmentMetadata meta = new AssignmentMetadata(
                "admin",
                "2026-02-15 19:45",
                "Initial setup"
        );

        String formatted = meta.format();
        assertTrue(formatted.contains("Назначено: 2026-02-15 19:45"));
        assertTrue(formatted.contains("Назначил: admin"));
        assertTrue(formatted.contains("Причина: Initial setup"));
    }

    @Test
    void testFormatWithoutReason() {
        AssignmentMetadata meta = new AssignmentMetadata(
                "system",
                "2026-02-15 19:45",
                null
        );

        String formatted = meta.format();
        assertTrue(formatted.contains("Назначено: 2026-02-15 19:45"));
        assertTrue(formatted.contains("Назначил: system"));
        assertFalse(formatted.contains("Причина:"));
    }

    @Test
    void testEmptyAssignedAtThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AssignmentMetadata("admin", "", "reason");
        });
    }

    @Test
    void testNullAssignedAtThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AssignmentMetadata("admin", null, "reason");
        });
    }
}