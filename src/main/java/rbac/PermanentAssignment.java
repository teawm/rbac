package main.java.rbac;

public class PermanentAssignment extends AbstractRoleAssignment {
    private boolean revoked = false;

    public PermanentAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
    }

    PermanentAssignment(String assignmentId, User user, Role role, AssignmentMetadata metadata) {
        super(assignmentId, user, role, metadata);
    }

    @Override
    public boolean isActive() {
        return !revoked;
    }

    @Override
    public String assignmentType() {
        return "PERMANENT";
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isRevoked() {
        return revoked;
    }

    @Override
    public String summary() {
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
            stringB.append("\n\tReason: ").append(metadata().reason());
        }
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        stringB.append("\n\tStatus: ").append(status);
        return stringB.toString();
    }
}