package main.java.rbac;

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
}