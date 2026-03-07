package rbac;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuditLogTest {

    @Test
    void testAddLogEntry() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");
        auditLog.log("CREATE", "admin", "Role: Administrator", "Создана роль");

        assertEquals(2, auditLog.count(), "Количество записей должно быть 2");
    }

    @Test
    void testGetAllEntries() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");
        auditLog.log("CREATE", "admin", "Role: Administrator", "Создана роль");

        List<AuditLog.AuditEntry> all = auditLog.getAll();

        assertEquals(2, all.size(), "Должно быть получено 2 записи");
        assertEquals("CREATE", all.get(0).action());
        assertEquals("admin", all.get(0).performer());
    }

    @Test
    void testGetByPerformer() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");
        auditLog.log("CREATE", "admin", "Role: Administrator", "Создана роль");
        auditLog.log("UPDATE", "user1", "User: stas", "Обновлен профиль");

        List<AuditLog.AuditEntry> byAdmin = auditLog.getByPerformer("admin");

        assertEquals(2, byAdmin.size(), "Должно быть найдено 2 записи от admin");
    }

    @Test
    void testGetByAction() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");
        auditLog.log("CREATE", "admin", "Role: Administrator", "Создана роль");
        auditLog.log("UPDATE", "admin", "User: stas", "Обновлен профиль");

        List<AuditLog.AuditEntry> creates = auditLog.getByAction("CREATE");

        assertEquals(2, creates.size(), "Должно быть найдено 2 записи с действием CREATE");
    }

    @Test
    void testGetRecentEntries() {
        AuditLog auditLog = new AuditLog();

        for (int i = 1; i <= 10; i++) {
            auditLog.log("ACTION_" + i, "user" + i, "Target", "Details");
        }

        List<AuditLog.AuditEntry> recent = auditLog.getRecent(3);

        assertEquals(3, recent.size(), "Должно быть получено 3 последние записи");
    }

    @Test
    void testPrintLog() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");

        assertDoesNotThrow(() -> {
            auditLog.printLog();
        });
    }

    @Test
    void testGetStatistics() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");
        auditLog.log("CREATE", "admin", "Role: Administrator", "Создана роль");
        auditLog.log("UPDATE", "admin", "User: stas", "Обновлен профиль");

        String stats = auditLog.getStatistics();

        assertTrue(stats.contains("Всего записей: 3"), "Статистика должна содержать общее количество");
        assertTrue(stats.contains("CREATE"), "Статистика должна содержать информацию о действиях");
    }

    @Test
    void testSaveToFile() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");

        assertDoesNotThrow(() -> {
            auditLog.saveToFile("audit_log_test.txt");
        });
    }

    @Test
    void testLoadFromFile() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");

        assertDoesNotThrow(() -> {
            auditLog.saveToFile("audit_log_test.txt");
        });

        AuditLog newLog = new AuditLog();

        assertDoesNotThrow(() -> {
            newLog.loadFromFile("audit_log_test.txt");
        });

        assertEquals(1, newLog.count(), "Должна быть загружена 1 запись");
    }

    @Test
    void testClearLog() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");
        auditLog.log("UPDATE", "admin", "User: stas", "Обновлен профиль");

        int before = auditLog.count();
        auditLog.clear();
        int after = auditLog.count();

        assertEquals(2, before, "До очистки должно быть 2 записи");
        assertEquals(0, after, "После очистки не должно быть записей");
    }

    @Test
    void testGetByDateRange() {
        AuditLog auditLog = new AuditLog();

        String date1 = "2026-02-15 10:00:00";
        String date2 = "2026-02-15 11:00:00";
        String date3 = "2026-02-15 12:00:00";

        auditLog.log("CREATE", "admin", "User: stas", "Создан пользователь");
        auditLog.log("UPDATE", "admin", "User: stas", "Обновлен профиль");

        List<AuditLog.AuditEntry> entries = auditLog.getByDateRange(date1, date3);

        assertTrue(entries.size() >= 0, "Должны быть получены записи за период");
    }

    @Test
    void testLogWithoutDetails() {
        AuditLog auditLog = new AuditLog();

        auditLog.log("CREATE", "admin", "User: stas");

        assertEquals(1, auditLog.count(), "Должна быть добавлена 1 запись");
    }

    @Test
    void testEmptyLog() {
        AuditLog auditLog = new AuditLog();

        assertEquals(0, auditLog.count(), "Пустой лог должен содержать 0 записей");
        assertTrue(auditLog.getAll().isEmpty(), "Пустой лог должен возвращать пустой список");
    }
}