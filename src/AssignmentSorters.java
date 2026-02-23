import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

public class AssignmentSorters {

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(assignment -> assignment.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(assignment -> assignment.role().name());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return (a1, a2) -> {
            try {
                LocalDateTime date1 = LocalDateTime.parse(a1.metadata().assignedAt(), formatter);
                LocalDateTime date2 = LocalDateTime.parse(a2.metadata().assignedAt(), formatter);
                return date1.compareTo(date2);
            } catch (DateTimeParseException e) {
                return 0;
            }
        };
    }

    public static Comparator<RoleAssignment> byAssignmentDateDesc() {
        return byAssignmentDate().reversed();
    }

    public static Comparator<RoleAssignment> byType() {
        return Comparator.comparing(RoleAssignment::assignmentType);
    }

    public static Comparator<RoleAssignment> byActiveStatus() {
        return Comparator.comparing(RoleAssignment::isActive).reversed();
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование сортировки назначений.]\n");

        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@email.com");
        User user3 = User.validate("andrey", "Andrey Lastnameov", "coolname@mail.ru");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second assignment");
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Third assignment");

        PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user2,
                moderRole,
                meta2,
                LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        PermanentAssignment pa2 = new PermanentAssignment(user3, viewerRole, meta3);

        RoleAssignment[] assignments = {pa1, ta1, pa2};

        System.out.println("[Тест 1]: Сортировка по имени пользователя:");
        java.util.Arrays.sort(assignments, AssignmentSorters.byUsername());
        for (RoleAssignment a : assignments) {
            System.out.println(a.user().username() + " -> " + a.role().name());
        }
        System.out.println();

        System.out.println("[Тест 2]: Сортировка по названию роли:");
        java.util.Arrays.sort(assignments, AssignmentSorters.byRoleName());
        for (RoleAssignment a : assignments) {
            System.out.println(a.role().name() + " -> " + a.user().username());
        }
        System.out.println();

        System.out.println("[Тест 3]: Сортировка по дате назначения (от старых к новым):");
        java.util.Arrays.sort(assignments, AssignmentSorters.byAssignmentDate());
        for (RoleAssignment a : assignments) {
            System.out.println(a.metadata().assignedAt() + " -> " + a.summary());
        }
        System.out.println();

        System.out.println("[Тест 4]: Сортировка по типу назначения:");
        java.util.Arrays.sort(assignments, AssignmentSorters.byType());
        for (RoleAssignment a : assignments) {
            System.out.println(a.assignmentType() + " -> " + a.summary());
        }
        System.out.println();

        System.out.println("[Тест 5]: Сортировка по активности (сначала активные):");
        java.util.Arrays.sort(assignments, AssignmentSorters.byActiveStatus());
        for (RoleAssignment a : assignments) {
            System.out.println((a.isActive() ? "ACTIVE" : "INACTIVE") + " -> " + a.summary());
        }
        System.out.println();

        System.out.println("[Тест 6]: Комбинированная сортировка: по имени пользователя и по названию роли:");
        java.util.Arrays.sort(assignments, AssignmentSorters.byUsername().thenComparing(AssignmentSorters.byRoleName()));
        for (RoleAssignment a : assignments) {
            System.out.println(a.user().username() + " -> " + a.role().name());
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}