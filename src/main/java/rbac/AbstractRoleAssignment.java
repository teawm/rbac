package rbac;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    protected AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null!");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role не может быть null!");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("AssignmentMetadata не может быть null!");
        }
        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    protected AbstractRoleAssignment(String assignmentId, User user, Role role, AssignmentMetadata metadata) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("assignmentId не может быть пустым!");
        }
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null!");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role не может быть null!");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("AssignmentMetadata не может быть null!");
        }
        this.assignmentId = assignmentId.trim();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public String summary() {
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        return String.format(
                "[%s] %s assigned to %s by %s at %s\n" +
                        "Reason: %s\n" +
                        "Status: %s",
                assignmentType(),
                role.name(),
                user.username(),
                metadata.assignedBy(),
                metadata.assignedAt(),
                metadata.reason(),
                status
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRoleAssignment that)) return false;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    @Override
    public String toString() {
        return "AbstractRoleAssignment{" +
                "assignmentId='" + assignmentId + '\'' +
                ", user=" + user.username() +
                ", role=" + role.name() +
                ", type=" + assignmentType() +
                ", active=" + isActive() +
                '}';
    }
}