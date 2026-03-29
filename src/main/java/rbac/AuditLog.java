package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

public class AuditLog {

    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {
        public String format() {
            return String.format(
                    "[%s] %s by %s on %s: %s",
                    timestamp,
                    action,
                    performer,
                    target,
                    details != null ? details : "<no details>"
            );
        }
    }

    private final List<AuditEntry> entries = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
    }

    public void log(String action, String performer, String target) {
        log(action, performer, target, null);
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        if (performer == null || performer.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String normalized = performer.trim().toLowerCase();
        return entries.stream()
                .filter(entry -> entry.performer().toLowerCase().contains(normalized))
                .toList();
    }

    public List<AuditEntry> getByAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String normalized = action.trim().toUpperCase();
        return entries.stream()
                .filter(entry -> entry.action().toUpperCase().contains(normalized))
                .toList();
    }

    public List<AuditEntry> getByDateRange(String startDate, String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate, DATE_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endDate, DATE_FORMATTER);

            return entries.stream()
                    .filter(entry -> {
                        try {
                            LocalDateTime entryTime = LocalDateTime.parse(entry.timestamp(), DATE_FORMATTER);
                            return !entryTime.isBefore(start) && !entryTime.isAfter(end);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<AuditEntry> getRecent(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }

        int size = entries.size();
        int fromIndex = Math.max(0, size - count);

        return new ArrayList<>(entries.subList(fromIndex, size));
    }

    public void clear() {
        entries.clear();
    }

    public int count() {
        return entries.size();
    }

    public void printLog() {
        System.out.println("\n[Аудит-лог системы.]\n");

        if (entries.isEmpty()) {
            System.out.println("[!] Лог пуст.\n");
            return;
        }

        System.out.println("Всего записей: " + entries.size());
        System.out.println("Последняя запись: " + entries.get(entries.size() - 1).timestamp());
        System.out.println("\n--- Последние 20 записей ---\n");

        List<AuditEntry> recent = getRecent(20);
        for (int i = recent.size() - 1; i >= 0; i--) {
            System.out.println(recent.get(i).format());
        }

        System.out.println("\n==========================\n");
    }

    public void printLog(int page, int pageSize) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }

        int totalEntries = entries.size();
        int totalPages = (int) Math.ceil((double) totalEntries / pageSize);

        if (page > totalPages && totalEntries > 0) {
            page = totalPages;
        }

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalEntries);

        System.out.println("\n[Аудит-лог системы.]\n");
        System.out.println("Страница " + page + " из " + totalPages);
        System.out.println("Записи " + (fromIndex + 1) + "-" + toIndex + " из " + totalEntries);
        System.out.println();

        if (totalEntries == 0) {
            System.out.println("[!] Лог пуст.\n");
            return;
        }

        for (int i = fromIndex; i < toIndex; i++) {
            System.out.println(entries.get(i).format());
        }

        System.out.println("\n==========================\n");
    }

    public void saveToFile(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("[Аудит-лог системы.]\n");
            writer.write("Всего записей: " + entries.size() + "\n");
            writer.write("Дата экспорта: " + LocalDateTime.now().format(DATE_FORMATTER) + "\n\n");

            for (AuditEntry entry : entries) {
                writer.write(entry.format());
                writer.write("\n");
            }

            System.out.println("[v] Лог сохранён в файл: " + filename);
        }
    }

    public void loadFromFile(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        entries.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int entryCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("===") || line.startsWith("Всего записей:") ||
                        line.startsWith("Дата экспорта:")) {
                    continue;
                }

                if (line.startsWith("[")) {
                    try {
                        int timestampEnd = line.indexOf("]");
                        if (timestampEnd > 0) {
                            String timestamp = line.substring(1, timestampEnd).trim();

                            String rest = line.substring(timestampEnd + 1).trim();
                            String[] parts = rest.split(" by ", 2);

                            if (parts.length == 2) {
                                String action = parts[0].trim();

                                String[] performerParts = parts[1].split(" on ", 2);
                                if (performerParts.length == 2) {
                                    String performer = performerParts[0].trim();

                                    String[] targetParts = performerParts[1].split(": ", 2);
                                    String target = targetParts[0].trim();
                                    String details = targetParts.length > 1 ? targetParts[1].trim() : null;

                                    entries.add(new AuditEntry(timestamp, action, performer, target, details));
                                    entryCount++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            System.out.println("[v] Лог загружен из файла: " + filename);
            System.out.println("  > Загружено записей: " + entryCount);
        }
    }

    public String getStatistics() {
        Map<String, Long> actionStats = entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.action().toUpperCase(),
                        Collectors.counting()
                ));

        Map<String, Long> performerStats = entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.performer(),
                        Collectors.counting()
                ));

        StringBuilder sb = new StringBuilder();
        sb.append("\n[Статистика аудит-лога.]\n");
        sb.append("Всего записей: ").append(entries.size()).append("\n");

        if (!entries.isEmpty()) {
            sb.append("Первая запись: ").append(entries.get(0).timestamp()).append("\n");
            sb.append("Последняя запись: ").append(entries.get(entries.size() - 1).timestamp()).append("\n");

            sb.append("Статистика по действиям:\n");
            actionStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry -> {
                        sb.append("> ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    });

            sb.append("\nСтатистика по пользователям:\n");
            performerStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        sb.append("> ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    });
        }

        sb.append("\n==========================\n");

        return sb.toString();
    }
}