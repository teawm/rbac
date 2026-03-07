import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Даты не могут быть null!");
        }

        LocalDateTime d1 = parseDate(date1);
        LocalDateTime d2 = parseDate(date2);

        return d1.isBefore(d2);
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Даты не могут быть null!");
        }

        LocalDateTime d1 = parseDate(date1);
        LocalDateTime d2 = parseDate(date2);

        return d1.isAfter(d2);
    }

    public static boolean isEqual(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Даты не могут быть null!");
        }

        LocalDateTime d1 = parseDate(date1);
        LocalDateTime d2 = parseDate(date2);

        return d1.isEqual(d2);
    }

    public static String addDays(String date, int days) {
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null!");
        }

        LocalDateTime parsedDate = parseDate(date);
        LocalDateTime result = parsedDate.plusDays(days);

        if (date.length() == 10) {
            return result.toLocalDate().format(DATE_FORMATTER);
        }

        return result.format(DATETIME_FORMATTER);
    }

    public static String subtractDays(String date, int days) {
        return addDays(date, -days);
    }

    public static String formatRelativeTime(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null!");
        }

        LocalDate targetDate = parseDate(date).toLocalDate();
        LocalDate today = LocalDate.now();

        long daysBetween = ChronoUnit.DAYS.between(today, targetDate);

        if (daysBetween == 0) {
            return "today";
        } else if (daysBetween == 1) {
            return "tomorrow";
        } else if (daysBetween == -1) {
            return "yesterday";
        } else if (daysBetween > 0) {
            return "in " + daysBetween + " day" + (daysBetween == 1 ? "" : "s");
        } else {
            return Math.abs(daysBetween) + " day" + (Math.abs(daysBetween) == 1 ? "" : "s") + " ago";
        }
    }

    private static LocalDateTime parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата не может быть пустой!");
        }

        try {
            String trimmed = date.trim();

            if (trimmed.length() == 10) {
                LocalDate localDate = LocalDate.parse(trimmed, DATE_FORMATTER);
                return localDate.atStartOfDay();
            }

            return LocalDateTime.parse(trimmed, DATETIME_FORMATTER);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Неверный формат даты! Используйте 'yyyy-MM-dd' или 'yyyy-MM-dd HH:mm:ss', получено: " + date
            );
        }
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }

        try {
            parseDate(date);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static long daysBetween(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Даты не могут быть null!");
        }

        LocalDate d1 = parseDate(date1).toLocalDate();
        LocalDate d2 = parseDate(date2).toLocalDate();

        return ChronoUnit.DAYS.between(d1, d2);
    }

    public static long hoursBetween(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Даты не могут быть null!");
        }

        LocalDateTime d1 = parseDate(date1);
        LocalDateTime d2 = parseDate(date2);

        return ChronoUnit.HOURS.between(d1, d2);
    }

    public static String formatReadable(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null!");
        }

        LocalDateTime parsed = parseDate(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

        return parsed.toLocalDate().format(formatter);
    }

    public static boolean isFuture(String date) {
        return isAfter(date, getCurrentDateTime());
    }

    public static boolean isPast(String date) {
        return isBefore(date, getCurrentDateTime());
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование DateUtils.]\n");

        System.out.println("[Тест 1] Текущая дата и время:");
        try {
            String currentDate = DateUtils.getCurrentDate();
            String currentDateTime = DateUtils.getCurrentDateTime();
            System.out.println("[v] Текущая дата: " + currentDate);
            System.out.println("[v] Текущее время: " + currentDateTime);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 2] Сравнение дат:");
        try {
            boolean before = DateUtils.isBefore("2025-12-31", "2026-01-01");
            boolean after = DateUtils.isAfter("2026-01-01", "2025-12-31");
            boolean equal = DateUtils.isEqual("2026-01-01", "2026-01-01");

            System.out.println("[v] 2025-12-31 < 2026-01-01: " + before);
            System.out.println("[v] 2026-01-01 > 2025-12-31: " + after);
            System.out.println("[v] 2026-01-01 == 2026-01-01: " + equal);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3] Добавление дней:");
        try {
            String result1 = DateUtils.addDays("2026-01-01", 7);
            String result2 = DateUtils.addDays("2026-01-01", -3);
            System.out.println("[v] 2026-01-01 + 7 дней: " + result1);
            System.out.println("[v] 2026-01-01 - 3 дня: " + result2);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4] Относительное время:");
        try {
            String today = DateUtils.getCurrentDate();
            String tomorrow = DateUtils.addDays(today, 1);
            String yesterday = DateUtils.addDays(today, -1);
            String nextWeek = DateUtils.addDays(today, 7);

            System.out.println("[v] Сегодня: " + DateUtils.formatRelativeTime(today));
            System.out.println("[v] Завтра: " + DateUtils.formatRelativeTime(tomorrow));
            System.out.println("[v] Вчера: " + DateUtils.formatRelativeTime(yesterday));
            System.out.println("[v] Через неделю: " + DateUtils.formatRelativeTime(nextWeek));
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5] Валидация даты:");
        try {
            boolean valid1 = DateUtils.isValidDate("2026-01-15");
            boolean valid2 = DateUtils.isValidDate("2026-01-15 14:30:00");
            boolean invalid = DateUtils.isValidDate("15-01-2026");

            System.out.println("[v] Корректная дата (без времени): " + valid1);
            System.out.println("[v] Корректная дата (с временем): " + valid2);
            System.out.println("[v] Некорректная дата: " + !invalid);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 6] Разница между датами:");
        try {
            long days = DateUtils.daysBetween("2026-01-01", "2026-01-15");
            long hours = DateUtils.hoursBetween("2026-01-01 10:00:00", "2026-01-01 15:30:00");

            System.out.println("[v] Дней между 2026-01-01 и 2026-01-15: " + days);
            System.out.println("[v] Часов между 10:00 и 15:30: " + hours);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 7] Читаемый формат:");
        try {
            String readable = DateUtils.formatReadable("2026-02-15");
            System.out.println("[v] 2026-02-15 -> " + readable);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 8] Проверка будущего/прошлого:");
        try {
            String future = DateUtils.addDays(DateUtils.getCurrentDate(), 1);
            String past = DateUtils.addDays(DateUtils.getCurrentDate(), -1);

            boolean isFuture = DateUtils.isFuture(future);
            boolean isPast = DateUtils.isPast(past);

            System.out.println("[v] Завтра в будущем: " + isFuture);
            System.out.println("[v] Вчера в прошлом: " + isPast);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 9] Невалидный формат даты:");
        try {
            DateUtils.parseDate("15/01/2026");
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Исключение получено: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 10] Вычитание дней:");
        try {
            String result = DateUtils.subtractDays("2026-01-15", 5);
            System.out.println("[v] 2026-01-15 - 5 дней: " + result);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}