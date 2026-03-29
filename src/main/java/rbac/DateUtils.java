package main.java.rbac;

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
}