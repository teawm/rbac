import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    // Формат даты: "yyyy-MM-dd HH:mm"
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Компактный конструктор с валидацией
    public AssignmentMetadata {
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Поле assignedBy не может быть пустым!");
        }
        if (assignedAt == null || assignedAt.trim().isEmpty()) {
            throw new IllegalArgumentException("Поле assignedAt не может быть пустым!");
        }
        assignedBy = assignedBy.trim();
        assignedAt = assignedAt.trim();
        reason = (reason != null) ? reason.trim() : null;
    }
    public static AssignmentMetadata now(String assignedBy, String reason) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        return new AssignmentMetadata(assignedBy, timestamp, reason);
    }
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Назначено: ").append(assignedAt).append("\n");
        sb.append("Назначил: ").append(assignedBy);
        if (reason != null && !reason.isEmpty()) {
            sb.append("\nПричина: ").append(reason);
        }
        return sb.toString();
    }

    // Тестовые случаи
    public static void main(String[] args) {
        System.out.println("\n[Тестирование AssignmentMetadata].\n");

        // Тест 1
        try {
            AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
            System.out.println("[v] Тест 1: Созданы метаданные (с причиной)");
            System.out.println(meta1.format() + "\n");
        } catch (Exception e) {
            System.out.println("[x] Тест 1: " + e.getMessage() + "\n");
        }

        // Тест 2
        try {
            AssignmentMetadata meta2 = AssignmentMetadata.now("system_admin", null);
            System.out.println("[v] Тест 2: Созданы метаданные (без причины)");
            System.out.println(meta2.format() + "\n");
        } catch (Exception e) {
            System.out.println("[x] Тест 2: " + e.getMessage() + "\n");
        }

        // Тест 3
        try {
            AssignmentMetadata meta3 = AssignmentMetadata.now("", "Test reason");
            System.out.println("[x] Тест 3: ожидалось исключение из-за пустого параметра\n");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Тест 3: " + e.getMessage() + "\n");
        }

        // Тест 4
        try {
            AssignmentMetadata meta4 = new AssignmentMetadata(
                    "Administrator",
                    "2026-02-15 23:00",
                    "Important reasons"
            );
            System.out.println("[v] Тест 4: созданы метаданные с заданной датой");
            System.out.println(meta4.format() + "\n");
        } catch (Exception e) {
            System.out.println("[x] Тест 4: " + e.getMessage() + "\n");
        }

        // Тест 5
        try {
            AssignmentMetadata meta5 = new AssignmentMetadata(
                    " Moderator",
                    "2026-02-15 23:01 ",
                    " Fake reason just for testing "
            );
            System.out.println("[v] Тест 5: нормализованы все поля:");
            System.out.println("assignedBy: '" + meta5.assignedBy() + "'");
            System.out.println("assignedAt: '" + meta5.assignedAt() + "'");
            System.out.println("reason: '" + meta5.reason() + "'\n");
        } catch (Exception e) {
            System.out.println("[x] Тест 5: " + e.getMessage() + "\n");
        }

        System.out.println("\n[Все тесты завершены].\n\n");
    }
}