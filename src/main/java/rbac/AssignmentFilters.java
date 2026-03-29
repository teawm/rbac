package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AssignmentFilters {

    public static AssignmentFilter byUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username не может быть null");
        }
        String normalized = username.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }
        return assignment -> assignment.user().username().toLowerCase().equals(normalized);
    }

    public static AssignmentFilter byRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role не может быть null");
        }
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("RoleName не может быть null");
        }
        String normalized = roleName.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("RoleName не может быть пустым");
        }
        return assignment -> assignment.role().name().toLowerCase().equals(normalized);
    }

    public static AssignmentFilter activeOnly() {
        return assignment -> assignment.isActive();
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Type не может быть null");
        }
        String normalized = type.trim().toUpperCase();
        if (!"PERMANENT".equals(normalized) && !"TEMPORARY".equals(normalized)) {
            throw new IllegalArgumentException("Неверный тип назначения. Допустимые значения: PERMANENT, TEMPORARY");
        }
        return assignment -> normalized.equals(assignment.assignmentType());
    }

    public static AssignmentFilter assignedBy(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username не может быть null");
        }
        String normalized = username.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }
        return assignment -> assignment.metadata().assignedBy().toLowerCase().equals(normalized);
    }

    public static AssignmentFilter assignedAfter(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Date не может быть null");
        }
        try {
            LocalDateTime targetDate = LocalDateTime.parse(date.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return assignment -> {
                try {
                    LocalDateTime assignmentDate = LocalDateTime.parse(
                            assignment.metadata().assignedAt(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    );
                    return assignmentDate.isAfter(targetDate);
                } catch (DateTimeParseException e) {
                    return false;
                }
            };
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты! Ожидается 'yyyy-MM-dd HH:mm', получено: " + date);
        }
    }

    public static AssignmentFilter expiringBefore(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Date не может быть null");
        }
        try {
            LocalDateTime targetDate = LocalDateTime.parse(date.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return assignment -> {
               if ("TEMPORARY".equals(assignment.assignmentType())) {
                    try {
                        TemporaryAssignment tempAssignment = (TemporaryAssignment) assignment;
                        LocalDateTime expiryDate = LocalDateTime.parse(
                                tempAssignment.expiresAt(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        );
                        return expiryDate.isBefore(targetDate);
                    } catch (ClassCastException | DateTimeParseException e) {
                        return false;
                    }
                }
                return false;
            };
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты! Ожидается 'yyyy-MM-dd HH:mm', получено: " + date);
        }
    }
}