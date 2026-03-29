package main.java.rbac;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final BackgroundExecutor backgroundExecutor;
    private final AuditLog auditLog;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.backgroundExecutor = new BackgroundExecutor();
        this.auditLog = new AuditLog();
        this.currentUser = "system";
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым!");
        }
        this.currentUser = username.trim();
    }

    public void initialize() {
        System.out.println("[Инициализация системы RBAC]\n");

        System.out.println("[Создание прав доступа]");
        Permission readUsers = new Permission("READ", "users", "Просмотр пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Создание и редактирование пользователей");
        Permission deleteUsers = new Permission("DELETE", "users", "Удаление пользователей");
        Permission readRoles = new Permission("READ", "roles", "Просмотр ролей");
        Permission writeRoles = new Permission("WRITE", "roles", "Создание и редактирования ролей");
        Permission deleteRoles = new Permission("DELETE", "roles", "Удаление ролей");
        Permission assignRoles = new Permission("ASSIGN", "assignments", "Назначение ролей пользователям");
        Permission revokeRoles = new Permission("REVOKE", "assignments", "Отзыв ролей у пользователей");
        Permission viewReports = new Permission("READ", "reports", "Просмотр отчётов и статистики");

        System.out.println("[Создание ролей]");
        Role adminRole = new Role("Administrator", "Полный доступ ко всем функциям системы");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readRoles);
        adminRole.addPermission(writeRoles);
        adminRole.addPermission(deleteRoles);
        adminRole.addPermission(assignRoles);
        adminRole.addPermission(revokeRoles);
        adminRole.addPermission(viewReports);
        roleManager.add(adminRole);
        System.out.println("[v] Создана роль: Administrator (" + adminRole.getPermissions().size() + " прав)");

        Role managerRole = new Role("Manager", "Управление пользователями и просмотр отчётов");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readRoles);
        managerRole.addPermission(assignRoles);
        managerRole.addPermission(viewReports);
        roleManager.add(managerRole);
        System.out.println("[v] Создана роль: Manager (" + managerRole.getPermissions().size() + " прав)");

        Role viewerRole = new Role("Viewer", "Просмотр данных");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readRoles);
        viewerRole.addPermission(viewReports);
        roleManager.add(viewerRole);
        System.out.println("[v] Создана роль: Viewer (" + viewerRole.getPermissions().size() + " прав)");

        System.out.println("[Создание администратора]");
        try {
            User adminUser = User.validate("admin", "System Administrator", "admin@rbac.local");
            userManager.add(adminUser);
            System.out.println("[v] Создан пользователь: " + adminUser.username());
            AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial system setup");
            PermanentAssignment adminAssignment = new PermanentAssignment(adminUser, adminRole, meta);
            assignmentManager.add(adminAssignment);
            System.out.println("[v] Назначена роль Administrator пользователю admin");
            currentUser = "admin";
        } catch (Exception e) {
            System.out.println("[x] Ошибка при создании администратора: " + e.getMessage());
        }

        System.out.println("\n[Инициализация завершена.]\n");
    }

    public BackgroundExecutor getBackgroundExecutor() {
        return backgroundExecutor;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Статистика системы RBAC.]\n");

        int userCount = userManager.count();
        sb.append("Пользователи:\n");
        sb.append("  > Всего: ").append(userCount).append("\n");

        int roleCount = roleManager.count();
        sb.append("  > Роли:\n");
        sb.append("  > Всего: ").append(roleCount).append("\n");

        int assignmentCount = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();
        sb.append("\nНазначения:\n");
        sb.append("  > Всего: ").append(assignmentCount).append("\n");
        sb.append("  > Активных: ").append(activeAssignments).append("\n");
        sb.append("  > Истёкших: ").append(expiredAssignments).append("\n");

        long permanentCount = assignmentManager.findAll().stream()
                .filter(a -> "PERMANENT".equals(a.assignmentType()))
                .count();
        long temporaryCount = assignmentManager.findAll().stream()
                .filter(a -> "TEMPORARY".equals(a.assignmentType()))
                .count();
        sb.append("Типы назначений:\n");
        sb.append("  > Постоянные: ").append(permanentCount).append("\n");
        sb.append("  > Временные: ").append(temporaryCount).append("\n");

        return sb.toString();
    }

    private static final long CHECK_INTERVAL_SECONDS = 60;

    public void startExpirationChecker() {
        backgroundExecutor.scheduleAtFixedRate(() -> {
            try {
                checkExpiredAssignments();
            } catch (Exception e) {
                System.err.println("Ошибка при проверке истекших назначений: " + e.getMessage());
            }
        }, 0, CHECK_INTERVAL_SECONDS);

        System.out.println("[v] Периодическая проверка истекших назначений запущена (каждые " +
                CHECK_INTERVAL_SECONDS + " секунд)");
    }

    private void checkExpiredAssignments() {
        List<RoleAssignment> allAssignments = assignmentManager.findAll();

        List<TemporaryAssignment> expired = allAssignments.stream()
                .filter(a -> a instanceof TemporaryAssignment)
                .map(a -> (TemporaryAssignment) a)
                .filter(a -> !a.isActive())
                .toList();

        if (!expired.isEmpty()) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("Найдено истёкших назначений: ").append(expired.size()).append("\n");

            for (TemporaryAssignment assignment : expired) {
                logMessage.append("  - ").append(assignment.user().username())
                        .append(" -> ").append(assignment.role().name())
                        .append(" (истекло: ").append(assignment.expiresAt()).append(")\n");
            }

            auditLog.log("EXPIRATION_CHECK", "system", "Temporary Assignments", logMessage.toString());
        }

        String stats = generateStatistics();
        auditLog.log("STATISTICS", "system", "System Stats", stats);
    }
}