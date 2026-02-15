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

    public static void main(String[] args) {
        System.out.println("\n[Тестирование PermanentAssignment].\n");

        User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        Role adminRole = new Role("Admin", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        AssignmentMetadata meta = AssignmentMetadata.now("system_admin", "Initial setup");

        try {
            PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);
            System.out.println("[v] Тест 1: Постоянное назначение создано:\n");
            System.out.println("\tID: " + pa.assignmentId().substring(0, 8) + "...");
            System.out.println("\tSummary: " + pa.summary() + "\n");
        } catch (Exception error) {
            System.out.println("[x] Тест 1: " + error.getMessage() + "\n");
        }

        try {
            PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);
            if (pa.isActive()) {
                System.out.println("[v] Тест 2: Назначение активно (по умолчанию)\n");
            } else {
                System.out.println("[x] Тест 2: Назначение должно быть активным по умолчанию!\n");
            }
        } catch (Exception err) {
            System.out.println("[x] Тест 2: " + err.getMessage() + "\n");
        }

        try {
            PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);
            pa.revoke();

            if (!pa.isActive() && pa.isRevoked()) {
                System.out.println("[v] Тест 3: Назначение отменено");
                System.out.println("\tStatus after revoking: " + (pa.isActive() ? "ACTIVE" : "INACTIVE"));
                System.out.println("\tSummary: " + pa.summary() + "\n");
            } else {
                System.out.println("[x] Тест 3: Ошибка при отмене назначения!\n");
            }
        } catch (Exception err) {
            System.out.println("[x] Тест 3: " + err.getMessage() + "\n");
        }

        try {
            PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);

            boolean userMatch = pa.user().equals(testUser);
            boolean roleMatch = pa.role().equals(adminRole);
            boolean metaMatch = pa.metadata().assignedBy().equals("system_admin");

            if (userMatch && roleMatch && metaMatch) {
                System.out.println("[v] Тест 4: Все геттеры работают корректно\n");
            } else {
                System.out.println("[x] Тест 4: Ошибка в работе геттеров!\n");
            }
        } catch (Exception err) {
            System.out.println("[x] Тест 4: " + err.getMessage() + "\n");
        }

        try {
            String fixedId = "test-p-id-001";
            PermanentAssignment pa1 = new PermanentAssignment(fixedId, testUser, adminRole, meta);
            PermanentAssignment pa2 = new PermanentAssignment(fixedId, testUser, adminRole, meta);

            if (pa1.equals(pa2) && pa1.hashCode() == pa2.hashCode()) {
                System.out.println("[v] Тест 5: equals/hashCode работают по assignmentId\n");
            } else {
                System.out.println("[x] Тест 5: equals/hashCode не совпадают!\n");
            }
        } catch (Exception err) {
            System.out.println("[x] Тест 5: " + err.getMessage() + "\n");
        }

        System.out.println("\n[Все тесты завершены].\n\n");
    }
}