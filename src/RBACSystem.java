import java.util.*;
import java.util.stream.Collectors;

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

    public void initialize() {
        System.out.println("[Инициализация системы RBAC]\n");

        System.out.println("[Создание прав доступа]");

        Permission readUsers = new Permission("READ", "users", "Просмотр пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Создание и редактирование пользователей");
        Permission deleteUsers = new Permission("DELETE", "users", "Удаление пользователей");

        Permission readRoles = new Permission("READ", "roles", "Просмотр ролей");
        Permission writeRoles = new Permission("WRITE", "roles", "Создание и редактирование ролей");
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

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Статистика системы RBAC.]\n\n");

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
        sb.append("  > Истекших: ").append(expiredAssignments).append("\n");

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

    public static void main(String[] args) {
        System.out.println("[Тестирование RBACSystem.]\n");

        System.out.println("[Тест 1] Создание системы:");
        RBACSystem system = new RBACSystem();
        System.out.println("[v] Система создана");
        System.out.println("  > Текущий пользователь: " + system.getCurrentUser());
        System.out.println();

        System.out.println("[Тест 2] Инициализация системы:");
        try {
            system.initialize();
            System.out.println("[v] Инициализация завершена");
            System.out.println("  > Пользователей: " + system.getUserManager().count());
            System.out.println("  > Ролей: " + system.getRoleManager().count());
            System.out.println("  > Назначений: " + system.getAssignmentManager().count());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3] Проверка существования администратора:");
        try {
            Optional<User> admin = system.getUserManager().findByUsername("admin");
            if (admin.isPresent()) {
                System.out.println("[v] Администратор найден: " + admin.get().format());
            } else {
                System.out.println("[x] Администратор не найден");
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4] Проверка существования ролей:");
        try {
            System.out.println("  > Administrator существует: " + system.getRoleManager().exists("Administrator"));
            System.out.println("  > Manager существует: " + system.getRoleManager().exists("Manager"));
            System.out.println("  > Viewer существует: " + system.getRoleManager().exists("Viewer"));
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5] Проверка назначения роли администратору:");
        try {
            Optional<User> admin = system.getUserManager().findByUsername("admin");
            Optional<Role> adminRole = system.getRoleManager().findByName("Administrator");

            if (admin.isPresent() && adminRole.isPresent()) {
                boolean hasRole = system.getAssignmentManager().userHasRole(admin.get(), adminRole.get());
                System.out.println("[v] Администратор имеет роль Administrator: " + hasRole);

                if (hasRole) {
                    Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(admin.get());
                    System.out.println("  > Прав у администратора: " + permissions.size());
                }
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 6] Генерация статистики:");
        try {
            String stats = system.generateStatistics();
            System.out.println(stats);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 7] Изменение текущего пользователя:");
        try {
            system.setCurrentUser("admin");
            System.out.println("[v] Текущий пользователь изменён на: " + system.getCurrentUser());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 8] Создание пользователя и назначение роли:");
        try {
            User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
            system.getUserManager().add(testUser);
            System.out.println("[v] Пользователь создан: " + testUser.username());

            Optional<Role> viewerRole = system.getRoleManager().findByName("Viewer");
            if (viewerRole.isPresent()) {
                AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test assignment");
                PermanentAssignment assignment = new PermanentAssignment(testUser, viewerRole.get(), meta);
                system.getAssignmentManager().add(assignment);
                System.out.println("[v] Роль назначена: " + assignment.summary());
            }

            boolean hasRole = system.getAssignmentManager().userHasRole(testUser, viewerRole.get());
            System.out.println("[v] Пользователь имеет роль Viewer: " + hasRole);

            boolean canViewReports = system.getAssignmentManager().userHasPermission(
                    testUser, "READ", "reports"
            );
            System.out.println("[v] Пользователь может просматривать отчёты: " + canViewReports);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 9] Обновлённая статистика:");
        try {
            System.out.println(system.generateStatistics());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}