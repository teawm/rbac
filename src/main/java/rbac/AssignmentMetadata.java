package main.java.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
}