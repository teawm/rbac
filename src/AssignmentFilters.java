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

    public static void main(String[] args) {
        System.out.println("[Тестирование фильтров назначений]\n");

        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.com");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("security_officer", "Security audit");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        RoleAssignment[] assignments = {pa1, ta1};

        System.out.println("[Тест 1]: Фильтр byUser(stas_):");
        AssignmentFilter filter1 = AssignmentFilters.byUser(user1);
        for (RoleAssignment a : assignments) {
            if (filter1.test(a)) {
                System.out.println("[v] " + a.summary());
            }
        }
        System.out.println();

        System.out.println("[Тест 2]: Фильтр byUsername(\"anton\"):");
        AssignmentFilter filter2 = AssignmentFilters.byUsername("anton");
        for (RoleAssignment a : assignments) {
            if (filter2.test(a)) {
                System.out.println("[v] " + a.summary());
            }
        }
        System.out.println();

        System.out.println("[Тест 3]: Фильтр byRole(\"Administrator\"):");
        AssignmentFilter filter3 = AssignmentFilters.byRoleName("Administrator");
        for (RoleAssignment a : assignments) {
            if (filter3.test(a)) {
                System.out.println("[v] " + a.summary());
            }
        }
        System.out.println();

        System.out.println("[Тест 4]: Фильтр activeOnly():");
        AssignmentFilter filter4 = AssignmentFilters.activeOnly();
        for (RoleAssignment a : assignments) {
            if (filter4.test(a)) {
                System.out.println("[v] " + a.summary());
            }
        }
        System.out.println();

        System.out.println("[Тест 5]: Фильтр byType(\"TEMPORARY\"):");
        AssignmentFilter filter5 = AssignmentFilters.byType("TEMPORARY");
        for (RoleAssignment a : assignments) {
            if (filter5.test(a)) {
                System.out.println("[v] " + a.summary());
            }
        }
        System.out.println();

        System.out.println("[Тест 6]: Фильтр assignedBy(\"admin\"):");
        AssignmentFilter filter6 = AssignmentFilters.assignedBy("admin");
        for (RoleAssignment a : assignments) {
            if (filter6.test(a)) {
                System.out.println("[v] " + a.summary());
            }
        }
        System.out.println();

        System.out.println("[Тест 7]: Комбинированный фильтр: активное назначение для пользователя anton:");
        AssignmentFilter filter7 = AssignmentFilters.byUsername("anton")
                .and(AssignmentFilters.activeOnly());
        for (RoleAssignment a : assignments) {
            if (filter7.test(a)) {
                System.out.println("[v] " + a.summary());
            }
        }
        System.out.println();

        System.out.println("[Тест 8]: Комбинированный фильтр: назначение от admin ИЛИ временные назначения:");
        AssignmentFilter filter8 = AssignmentFilters.assignedBy("admin")
                .or(AssignmentFilters.byType("TEMPORARY"));
        for (RoleAssignment a : assignments) {
            if (filter8.test(a)) {
                System.out.println("[v] " + a.summary());
            }
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}