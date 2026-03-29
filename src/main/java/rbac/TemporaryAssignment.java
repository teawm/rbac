package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt) {
        super(user, role, metadata);
        validateExpirationDate(expiresAt);
        this.expiresAt = expiresAt.trim();
        this.autoRenew = false;
    }

    TemporaryAssignment(String assignmentId, User user, Role role, AssignmentMetadata metadata,
                        String expiresAt, boolean autoRenew) {
        super(assignmentId, user, role, metadata);
        validateExpirationDate(expiresAt);
        this.expiresAt = expiresAt.trim();
        this.autoRenew = autoRenew;
    }

    private void validateExpirationDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата истечения не может быть пустой!");
        }
        try {
            LocalDateTime.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException err) {
            throw new IllegalArgumentException(
                    "Неверный формат даты! Ожидается 'yyyy-MM-dd HH:mm', получено: '" + dateStr + "'"
            );
        }
    }

    @Override
    public boolean isActive() {
        try {
            LocalDateTime expiry = LocalDateTime.parse(expiresAt, DATE_FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            return now.isBefore(expiry) || now.isEqual(expiry);
        } catch (DateTimeParseException err) {
            return false;
        }
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        validateExpirationDate(newExpirationDate);
        this.expiresAt = newExpirationDate.trim();
    }

    public boolean isExpired() {
        return !isActive();
    }

    public String getTimeRemaining() {
        try {
            LocalDateTime expiry = LocalDateTime.parse(expiresAt, DATE_FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            if (!now.isBefore(expiry)) {
                return "EXPIRED";
            }
            long seconds = java.time.Duration.between(now, expiry).getSeconds();
            long days = seconds / 86400;
            long hours = (seconds % 86400) / 3600;
            long minutes = (seconds % 3600) / 60;
            StringBuilder stringB = new StringBuilder();
            if (days > 0) stringB.append(days).append(" day").append(days > 1 ? "s " : " ");
            if (hours > 0) stringB.append(hours).append(" hour").append(hours > 1 ? "s " : " ");
            if (minutes > 0 || stringB.length() == 0) stringB.append(minutes).append(" min").append(minutes != 1 ? "s" : "");
            return stringB.toString().trim();
        } catch (DateTimeParseException err) {
            return "INVALID DATE";
        }
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public String expiresAt() {
        return expiresAt;
    }

    public boolean autoRenew() {
        return autoRenew;
    }

    @Override
    public String summary() {
        String status = isActive() ? "ACTIVE" : "EXPIRED";
        StringBuilder stringB = new StringBuilder();
        stringB.append(String.format(
                "[%s] %s assigned to %s by %s at %s",
                assignmentType(),
                role().name(),
                user().username(),
                metadata().assignedBy(),
                metadata().assignedAt()
        ));
        if (metadata().reason() != null && !metadata().reason().trim().isEmpty()) {
            stringB.append("\nReason: ").append(metadata().reason());
        }
        stringB.append("\nExpires: ").append(expiresAt);
        stringB.append("\nStatus: ").append(status);
        return stringB.toString();
    }
}