package rbac;

import java.util.*;

/**
 * Главный класс системы управления доступом RBAC
 */
public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
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

    /**
     * Инициализация системы с начальными данными
     */
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

    /**
     * Генерация статистики системы
     * @return форматированная строка со статистикой
     */
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
}