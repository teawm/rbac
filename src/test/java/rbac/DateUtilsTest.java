package rbac;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    @Test
    void testGetCurrentDate() {
        String currentDate = DateUtils.getCurrentDate();
        assertNotNull(currentDate);
        assertTrue(currentDate.matches("\\d{4}-\\d{2}-\\d{2}"), "Формат даты должен быть yyyy-MM-dd");
    }

    @Test
    void testGetCurrentDateTime() {
        String currentDateTime = DateUtils.getCurrentDateTime();
        assertNotNull(currentDateTime);
        assertTrue(currentDateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "Формат даты должен быть yyyy-MM-dd HH:mm:ss");
    }

    @Test
    void testIsBefore() {
        assertTrue(DateUtils.isBefore("2025-12-31", "2026-01-01"), "2025-12-31 должно быть раньше 2026-01-01");
        assertFalse(DateUtils.isBefore("2026-01-01", "2025-12-31"), "2026-01-01 не должно быть раньше 2025-12-31");
    }

    @Test
    void testIsAfter() {
        assertTrue(DateUtils.isAfter("2026-01-01", "2025-12-31"), "2026-01-01 должно быть позже 2025-12-31");
        assertFalse(DateUtils.isAfter("2025-12-31", "2026-01-01"), "2025-12-31 не должно быть позже 2026-01-01");
    }

    @Test
    void testIsEqual() {
        assertTrue(DateUtils.isEqual("2026-01-01", "2026-01-01"), "Даты должны быть равны");
        assertFalse(DateUtils.isEqual("2026-01-01", "2026-01-02"), "Даты не должны быть равны");
    }

    @Test
    void testAddDays() {
        assertEquals("2026-01-08", DateUtils.addDays("2026-01-01", 7), "Добавление 7 дней");
        assertEquals("2025-12-29", DateUtils.addDays("2026-01-01", -3), "Вычитание 3 дней");
    }

    @Test
    void testSubtractDays() {
        assertEquals("2025-12-25", DateUtils.subtractDays("2026-01-01", 7), "Вычитание 7 дней");
    }

    @Test
    void testFormatRelativeTime() {
        String today = DateUtils.getCurrentDate();
        String tomorrow = DateUtils.addDays(today, 1);
        String yesterday = DateUtils.addDays(today, -1);
        String nextWeek = DateUtils.addDays(today, 7);

        assertEquals("today", DateUtils.formatRelativeTime(today));
        assertEquals("tomorrow", DateUtils.formatRelativeTime(tomorrow));
        assertEquals("yesterday", DateUtils.formatRelativeTime(yesterday));
        assertTrue(DateUtils.formatRelativeTime(nextWeek).contains("in"), "Должно содержать 'in'");
    }

    @Test
    void testIsValidDate() {
        assertTrue(DateUtils.isValidDate("2026-01-15"), "Валидная дата без времени");
        assertTrue(DateUtils.isValidDate("2026-01-15 14:30:00"), "Валидная дата с временем");
        assertFalse(DateUtils.isValidDate("15-01-2026"), "Невалидный формат даты");
        assertFalse(DateUtils.isValidDate(null), "Null не должен быть валидным");
        assertFalse(DateUtils.isValidDate(""), "Пустая строка не должна быть валидной");
    }

    @Test
    void testDaysBetween() {
        assertEquals(14, DateUtils.daysBetween("2026-01-01", "2026-01-15"), "Разница в 14 дней");
        assertEquals(-14, DateUtils.daysBetween("2026-01-15", "2026-01-01"), "Отрицательная разница");
    }

    @Test
    void testHoursBetween() {
        assertEquals(5, DateUtils.hoursBetween("2026-01-01 10:00:00", "2026-01-01 15:00:00"), "Разница в 5 часов");
    }

    @Test
    void testFormatReadable() {
        String readable = DateUtils.formatReadable("2026-02-15");
        assertNotNull(readable);
        assertTrue(readable.contains("February"), "Должно содержать название месяца");
    }

    @Test
    void testIsFuture() {
        String future = DateUtils.addDays(DateUtils.getCurrentDate(), 1);
        assertTrue(DateUtils.isFuture(future), "Завтра должно быть в будущем");
    }

    @Test
    void testIsPast() {
        String past = DateUtils.addDays(DateUtils.getCurrentDate(), -1);
        assertTrue(DateUtils.isPast(past), "Вчера должно быть в прошлом");
    }
}