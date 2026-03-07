import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ          \n");
        sb.append("========================================\n\n");

        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            sb.append("[!] Нет зарегистрированных пользователей.\n");
            return sb.toString();
        }

        sb.append("Всего пользователей: ").append(users.size()).append("\n\n");
        sb.append("Детальная информация:\n");
        sb.append("========================================\n\n");

        users.sort(Comparator.comparing(User::username));

        for (User user : users) {
            sb.append("Пользователь: ").append(user.username()).append("\n");
            sb.append("> Полное имя: ").append(user.fullName()).append("\n");
            sb.append("> Email: ").append(user.email()).append("\n");

            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            List<RoleAssignment> activeAssignments = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .toList();

            sb.append("> Активных ролей: ").append(activeAssignments.size()).append("\n");

            if (activeAssignments.isEmpty()) {
                sb.append("> Роли: <не назначены>\n");
            } else {
                sb.append("  Роли:\n");
                for (RoleAssignment assignment : activeAssignments) {
                    sb.append("\t- ").append(assignment.role().name())
                            .append(" (").append(assignment.assignmentType()).append(")\n");
                }
            }

            Set<Permission> permissions = assignmentManager.getUserPermissions(user);
            sb.append("> Всего прав: ").append(permissions.size()).append("\n");

            if (!permissions.isEmpty()) {
                Map<String, List<Permission>> grouped = new TreeMap<>();
                for (Permission p : permissions) {
                    grouped.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
                }

                sb.append("> Права по ресурсам:\n");
                for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
                    sb.append("\t").append(entry.getKey()).append(":\n");
                    for (Permission p : entry.getValue()) {
                        sb.append("\t  - ").append(p.name()).append("\n");
                    }
                }
            }

            sb.append("\n----------------------------------------\n\n");
        }

        sb.append("\n========================================\n");
        sb.append("          СТАТИСТИКА                    \n");
        sb.append("========================================\n\n");

        double avgRoles = users.stream()
                .mapToDouble(u -> assignmentManager.findByUser(u).stream()
                        .filter(RoleAssignment::isActive)
                        .count())
                .average()
                .orElse(0.0);

        long usersWithNoRoles = users.stream()
                .filter(u -> assignmentManager.findByUser(u).stream()
                        .filter(RoleAssignment::isActive)
                        .count() == 0)
                .count();

        sb.append("Среднее количество ролей на пользователя: ").append(String.format("%.2f", avgRoles)).append("\n");
        sb.append("Пользователей без ролей: ").append(usersWithNoRoles).append("\n");
        sb.append("Пользователей с ролями: ").append(users.size() - usersWithNoRoles).append("\n");

        sb.append("\n========================================\n");

        return sb.toString();
    }

    public static String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("          ОТЧЕТ ПО РОЛЯМ               \n");
        sb.append("========================================\n\n");

        List<Role> roles = roleManager.findAll();

        if (roles.isEmpty()) {
            sb.append("[!] Нет зарегистрированных ролей.\n");
            return sb.toString();
        }

        sb.append("Всего ролей: ").append(roles.size()).append("\n");

        roles.sort((r1, r2) -> {
            int count1 = assignmentManager.findByRole(r1).size();
            int count2 = assignmentManager.findByRole(r2).size();
            return Integer.compare(count2, count1);
        });

        sb.append("\nРоли (отсортированы по популярности):\n");
        sb.append("========================================\n\n");

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            long activeCount = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .count();

            sb.append("Роль: ").append(role.name()).append("\n");
            sb.append("> Описание: ").append(role.description()).append("\n");
            sb.append("> Всего прав: ").append(role.getPermissions().size()).append("\n");
            sb.append("> Назначений: ").append(assignments.size()).append("\n");
            sb.append("> Активных назначений: ").append(activeCount).append("\n");

            if (!assignments.isEmpty()) {
                sb.append("> Пользователи с этой ролью:\n");

                Map<String, List<RoleAssignment>> groupedByType = assignments.stream()
                        .collect(Collectors.groupingBy(RoleAssignment::assignmentType));

                for (Map.Entry<String, List<RoleAssignment>> entry : groupedByType.entrySet()) {
                    sb.append("\t").append(entry.getKey()).append(" (").append(entry.getValue().size()).append("):\n");
                    for (RoleAssignment assignment : entry.getValue()) {
                        String status = assignment.isActive() ? "-" : "!";
                        sb.append("\t  ").append(status).append(" ").append(assignment.user().username());

                        if (!assignment.isActive() && assignment instanceof TemporaryAssignment temp) {
                            sb.append(" [истекло: ").append(temp.expiresAt()).append("]");
                        }

                        sb.append("\n");
                    }
                }
            }

            if (!role.getPermissions().isEmpty()) {
                sb.append("> Права:\n");

                Map<String, List<Permission>> grouped = new TreeMap<>();
                for (Permission p : role.getPermissions()) {
                    grouped.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
                }

                for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
                    sb.append("\t").append(entry.getKey()).append(":\n");
                    for (Permission p : entry.getValue()) {
                        sb.append("\t- ").append(p.name()).append(" (").append(p.description()).append(")\n");
                    }
                }
            }

            sb.append("\n----------------------------------------\n\n");
        }

        sb.append("\n========================================\n");
        sb.append("        ТОП-3 РОЛЕЙ ПО НАЗНАЧЕНИЯМ       \n");
        sb.append("========================================\n\n");

        roles.stream()
                .limit(3)
                .forEach(role -> {
                    long count = assignmentManager.findByRole(role).stream()
                            .filter(RoleAssignment::isActive)
                            .count();
                    sb.append("  ").append(role.name())
                            .append(" - ").append(count)
                            .append(" пользователей (").append(role.getPermissions().size())
                            .append(" прав)\n");
                });

        sb.append("\n========================================\n");

        return sb.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        МАТРИЦА ПРАВ ДОСТУПА            \n");
        sb.append("========================================\n\n");

        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            sb.append("[!] Нет зарегистрированных пользователей.\n");
            return sb.toString();
        }

        Set<String> allResources = new TreeSet<>();
        for (User user : users) {
            Set<Permission> permissions = assignmentManager.getUserPermissions(user);
            for (Permission p : permissions) {
                allResources.add(p.resource());
            }
        }

        if (allResources.isEmpty()) {
            sb.append("[!] Нет прав доступа в системе.\n");
            return sb.toString();
        }

        sb.append("Всего пользователей: ").append(users.size()).append("\n");
        sb.append("Всего ресурсов: ").append(allResources.size()).append("\n\n");

        List<String[]> matrixRows = new ArrayList<>();
        String[] headers = new String[allResources.size() + 1];
        headers[0] = "Username";

        int col = 1;
        List<String> resourceList = new ArrayList<>(allResources);
        for (String resource : resourceList) {
            headers[col++] = resource;
        }

        users.sort(Comparator.comparing(User::username));

        for (User user : users) {
            String[] row = new String[allResources.size() + 1];
            row[0] = user.username();

            Set<Permission> permissions = assignmentManager.getUserPermissions(user);

            for (int i = 0; i < resourceList.size(); i++) {
                String resource = resourceList.get(i);
                boolean hasAccess = permissions.stream()
                        .anyMatch(p -> p.resource().equals(resource));
                row[i + 1] = hasAccess ? "v" : "x";
            }

            matrixRows.add(row);
        }

        sb.append(FormatUtils.formatTable(headers, matrixRows,null));

        sb.append("\n========================================\n");
        sb.append("    СТАТИСТИКА ПО РЕСУРСАМ              \n");
        sb.append("========================================\n\n");

        for (String resource : resourceList) {
            long usersWithAccess = users.stream()
                    .filter(user -> {
                        Set<Permission> perms = assignmentManager.getUserPermissions(user);
                        return perms.stream().anyMatch(p -> p.resource().equals(resource));
                    })
                    .count();

            sb.append("  ").append(resource).append(": ")
                    .append(usersWithAccess).append(" пользователей имеют доступ (")
                    .append(String.format("%.1f", (double) usersWithAccess / users.size() * 100))
                    .append("%)\n");
        }

        sb.append("\n========================================\n");

        return sb.toString();
    }

    public static void exportToFile(String report, String filename) throws IOException {
        if (report == null) {
            throw new IllegalArgumentException("Отчет не может быть null");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report);
            writer.write("\n");

            System.out.println("[v] Отчет успешно сохранен в файл: " + filename);
        }
    }

    public static void exportToFileWithTimestamp(String report, String baseFilename) throws IOException {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = baseFilename + "_" + timestamp + ".txt";
        exportToFile(report, filename);
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование ReportGenerator.]\n");

        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        try {
            userManager.add(User.validate("admin", "System Admin", "admin@rbac.local"));
            userManager.add(User.validate("stas_", "Stas Makarov", "stas@example.com"));
            userManager.add(User.validate("andrey", "Andrey Lastname", "andrey@worker.com"));
            userManager.add(User.validate("tea", "Чай Поставьте", "tea@wm.com"));
        } catch (Exception e) {
            System.out.println("[x] Ошибка при создании пользователей: " + e.getMessage());
        }

        try {
            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(new Permission("READ", "users", "View users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
            adminRole.addPermission(new Permission("DELETE", "users", "Delete users"));
            adminRole.addPermission(new Permission("READ", "roles", "View roles"));
            roleManager.add(adminRole);

            Role editorRole = new Role("ContentEditor", "Edit content");
            editorRole.addPermission(new Permission("READ", "articles", "View articles"));
            editorRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));
            roleManager.add(editorRole);

            Role viewerRole = new Role("Viewer", "Read-only access");
            viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
            viewerRole.addPermission(new Permission("READ", "articles", "View articles"));
            roleManager.add(viewerRole);
        } catch (Exception e) {
            System.out.println("[x] Ошибка при создании ролей: " + e.getMessage());
        }

        try {
            Optional<User> admin = userManager.findByUsername("admin");
            Optional<User> john = userManager.findByUsername("john_doe");
            Optional<User> jane = userManager.findByUsername("jane_smith");
            Optional<User> bob = userManager.findByUsername("bob_jones");

            Optional<Role> adminRole = roleManager.findByName("Administrator");
            Optional<Role> editorRole = roleManager.findByName("ContentEditor");
            Optional<Role> viewerRole = roleManager.findByName("Viewer");

            if (admin.isPresent() && adminRole.isPresent()) {
                AssignmentMetadata meta1 = AssignmentMetadata.now("system", "Initial setup");
                assignmentManager.add(new PermanentAssignment(admin.get(), adminRole.get(), meta1));
            }

            if (john.isPresent() && editorRole.isPresent()) {
                AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");
                assignmentManager.add(new PermanentAssignment(john.get(), editorRole.get(), meta2));
            }

            if (jane.isPresent() && viewerRole.isPresent()) {
                AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Read access");
                assignmentManager.add(new PermanentAssignment(jane.get(), viewerRole.get(), meta3));
            }

            if (bob.isPresent() && viewerRole.isPresent()) {
                AssignmentMetadata meta4 = AssignmentMetadata.now("admin", "Read access");
                assignmentManager.add(new PermanentAssignment(bob.get(), viewerRole.get(), meta4));
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка при создании назначений: " + e.getMessage());
        }

        System.out.println("[Тест 1] Генерация отчета по пользователям:");
        try {
            String userReport = ReportGenerator.generateUserReport(userManager, assignmentManager);
            System.out.println("[v] Отчет сгенерирован");
            System.out.println("\t  Длина отчета: " + userReport.length() + " символов");

            ReportGenerator.exportToFile(userReport, "report_users_test.txt");
            System.out.println("[v] Отчет сохранен в файл");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 2] Генерация отчета по ролям:");
        try {
            String roleReport = ReportGenerator.generateRoleReport(roleManager, assignmentManager);
            System.out.println("[v] Отчет сгенерирован");
            System.out.println("\t  Длина отчета: " + roleReport.length() + " символов");

            ReportGenerator.exportToFile(roleReport, "report_roles_test.txt");
            System.out.println("[v] Отчет сохранен в файл");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3] Генерация матрицы прав:");
        try {
            String matrixReport = ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);
            System.out.println("[v] Матрица сгенерирована");
            System.out.println("\t  Длина отчета: " + matrixReport.length() + " символов");

            ReportGenerator.exportToFile(matrixReport, "report_matrix_test.txt");
            System.out.println("[v] Матрица сохранена в файл");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4] Сохранение с временной меткой:");
        try {
            String testReport = "Тестовый отчет для проерки временной метки";
            ReportGenerator.exportToFileWithTimestamp(testReport, "report_test");
            System.out.println("[v] Отчет с временной меткой сохранен");
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5] Проверка содержимого отчета:");
        try {
            String userReport = ReportGenerator.generateUserReport(userManager, assignmentManager);

            boolean hasAdmin = userReport.contains("admin");
            boolean hasStas = userReport.contains("stas_");
            boolean hasAndrey = userReport.contains("andrey");
            boolean hasTea = userReport.contains("tea");
            boolean hasTeaRus = userReport.contains("чай");

            System.out.println("[v ] Содержит 'admin': " + hasAdmin);
            System.out.println("[v ] Содержит 'stas_': " + hasStas);
            System.out.println("[v ] Содержит 'andrey': " + hasAndrey);
            System.out.println("[v ] Содержит 'tea': " + hasTea);
            System.out.println("[:(] Содержит 'чай': " + hasTeaRus); // жалко что false, но регистр имеет значение
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}