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

    public static void main(String[] args) {
        System.out.println("\n\n[Тестирование TemporaryAssignment].\n");

        User testUser = User.validate("stas_", "Stas Makarov", "stas@email.example");
        Role auditorRole = new Role("Security", "Security audit role");
        auditorRole.addPermission(new Permission("READ", "logs", "View audit logs"));
        AssignmentMetadata meta = AssignmentMetadata.now("security_employee", "Quarterly audit");

        try {
            // Дата на 1 час вперед
            String future = LocalDateTime.now().plusHours(1).format(DATE_FORMATTER);
            TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, future);

            System.out.println("[v] Тест 1: Временное назначение создано:\n");
            System.out.println("\tID: " + ta.assignmentId().substring(0, 8) + "...");
            System.out.println("\tLeft: " + ta.expiresAt());
            System.out.println("\tSummary: " + ta.summary() + "\n");
        } catch (Exception error) {
            System.out.println("[x] Тест 1: " + error.getMessage() + "\n");
        }

        try {
            String past = "2025-02-16 00:00";
            TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, past);

            if (!ta.isActive() && ta.isExpired()) {
                System.out.println("[v] Тест 2: Назначение с прошедшей датой неактивно");
                System.out.println("\tSummary: " + ta.summary() + "\n");
            } else {
                System.out.println("[x] Тест 2: Назначение с прошедшей датой должно быть неактивным!\n");
            }
        } catch (Exception err) {
            System.out.println("[x] Тест 2: " + err.getMessage() + "\n");
        }

        try {
            String initial = LocalDateTime.now().plusHours(1).format(DATE_FORMATTER);
            String extended = LocalDateTime.now().plusDays(7).format(DATE_FORMATTER);

            TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, initial);
            ta.extend(extended);

            if (ta.expiresAt().equals(extended)) {
                System.out.println("[v] Тест 3: Назначение успешно продлено!");
                System.out.println("\tНовая дата истечения: " + ta.expiresAt() + "\n");
            } else {
                System.out.println("[x] Тест 3: Ошибка при продлении назначения!\n");
            }
        } catch (Exception err) {
            System.out.println("[x] Тест 3: " + err.getMessage() + "\n");
        }

        try {
            new TemporaryAssignment(testUser, auditorRole, meta, "2026/02/15, 19:45");
            System.out.println("[x] Тест 4: ожидалось исключение из-за неверного формата даты и времени\n");
        } catch (IllegalArgumentException err) {
            System.out.println("[v] Тест 4: " + err.getMessage() + "\n");
        }

        try {
            String future = LocalDateTime.now().plusDays(2).plusHours(5).format(DATE_FORMATTER);
            TemporaryAssignment ta = new TemporaryAssignment(testUser, auditorRole, meta, future);

            String remaining = ta.getTimeRemaining();
            boolean hasDays = remaining.contains("day");
            boolean hasHours = remaining.contains("hour");

            if (hasDays && hasHours) {
                System.out.println("[v] Тест 5: Расчёт оставшегося времени работает");
                System.out.println("\tОсталось: " + remaining + "\n");
            } else {
                System.out.println("[ ] Тест 5: Оставшееся время: " + remaining + "\n");
            }
        } catch (Exception err) {
            System.out.println("[x] Тест 5: " + err.getMessage() + "\n");
        }

        System.out.println("\n[Все тесты завершены].\n\n");
    }
}