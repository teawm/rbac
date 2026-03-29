package main.java.rbac;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public static String generateUserReportParallel(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("  ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ (ПАРАЛЛЕЛЬНЫЙ)\n");
        sb.append("========================================\n\n");

        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            sb.append("<!> Нет зарегистрированных пользователей.\n");
            return sb.toString();
        }

        sb.append("Всего пользователей: ").append(users.size()).append("\n\n");
        sb.append("Детальная информация:\n");
        sb.append("========================================\n\n");

        users.sort(Comparator.comparing(User::username));

        List<String> userReports = users.parallelStream()
                .map(user -> generateUserReportEntry(user, assignmentManager))
                .collect(Collectors.toList());

        for (String report : userReports) {
            sb.append(report);
        }

        sb.append("\n========================================\n");
        sb.append("          СТАТИСТИКА                    \n");
        sb.append("========================================\n\n");

        double avgRoles = users.parallelStream()
                .mapToDouble(u -> assignmentManager.findByUser(u).stream()
                        .filter(RoleAssignment::isActive)
                        .count())
                .average()
                .orElse(0.0);

        long usersWithNoRoles = users.parallelStream()
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

    private static String generateUserReportEntry(User user, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("Пользователь: ").append(user.username()).append("\n");
        sb.append("> Полное имя: ").append(user.fullName()).append("\n");
        sb.append("> Email: ").append(user.email()).append("\n");

        List<RoleAssignment> assignments = assignmentManager.findByUser(user);
        List<RoleAssignment> activeAssignments = assignments.stream()
                .filter(RoleAssignment::isActive)
                .toList();

        sb.append("  Активных ролей: ").append(activeAssignments.size()).append("\n");

        if (activeAssignments.isEmpty()) {
            sb.append("> Роли: <не назначены>\n");
        } else {
            sb.append("> Роли:\n");
            for (RoleAssignment assignment : activeAssignments) {
                sb.append("\t> ").append(assignment.role().name())
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

            sb.append("  Права по ресурсам:\n");
            for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
                sb.append("\t").append(entry.getKey()).append(":\n");
                for (Permission p : entry.getValue()) {
                    sb.append("\t  > ").append(p.name()).append("\n");
                }
            }
        }

        sb.append("\n----------------------------------------\n\n");
        return sb.toString();
    }

    public static String generatePermissionMatrixParallel(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("   МАТРИЦА ПРАВ ДОСТУПА (ПАРАЛЛЕЛЬНАЯ)\n");
        sb.append("========================================\n\n");

        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            sb.append("<!> Нет зарегистрированных пользователей.\n");
            return sb.toString();
        }

        Set<String> allResources = users.parallelStream()
                .flatMap(user -> assignmentManager.getUserPermissions(user).stream())
                .map(Permission::resource)
                .collect(Collectors.toCollection(TreeSet::new));

        if (allResources.isEmpty()) {
            sb.append("<!> Нет прав доступа в системе.\n");
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

        matrixRows = users.parallelStream()
                .map(user -> {
                    String[] row = new String[allResources.size() + 1];
                    row[0] = user.username();

                    Set<Permission> permissions = assignmentManager.getUserPermissions(user);

                    for (int i = 0; i < resourceList.size(); i++) {
                        String resource = resourceList.get(i);
                        boolean hasAccess = permissions.stream()
                                .anyMatch(p -> p.resource().equals(resource));
                        row[i + 1] = hasAccess ? "-" : "!";
                    }

                    return row;
                })
                .collect(Collectors.toList());

        sb.append(FormatUtils.formatTable(headers, matrixRows));

        sb.append("\n========================================\n");
        sb.append("         СТАТИСТИКА ПО РЕСУРСАМ         \n");
        sb.append("========================================\n\n");

        for (String resource : resourceList) {
            long usersWithAccess = users.parallelStream()
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

    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ          \n");
        sb.append("========================================\n\n");

        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            sb.append("<!> Нет зарегистрированных пользователей.\n");
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
                sb.append("> Роли:\n");
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

                sb.append("  Права по ресурсам:\n");
                for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
                    sb.append("\t").append(entry.getKey()).append(":\n");
                    for (Permission p : entry.getValue()) {
                        sb.append("\t  > ").append(p.name()).append("\n");
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
            sb.append("<!> Нет зарегистрированных ролей.\n");
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
                        sb.append("\t  - ").append(p.name()).append(" (").append(p.description()).append(")\n");
                    }
                }
            }

            sb.append("\n----------------------------------------\n\n");
        }

        sb.append("\n========================================\n");
        sb.append("    ТОП-3 РОЛЕЙ ПО ИСПОЛЬЗОВАНИЮ       \n");
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
            sb.append("<!> Нет зарегистрированных пользователей.\n");
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
            sb.append("<!> Нет прав доступа в системе.\n");
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
                row[i + 1] = hasAccess ? "-" : "!";
            }

            matrixRows.add(row);
        }

        sb.append(FormatUtils.formatTable(headers, matrixRows));

        sb.append("\n========================================\n");
        sb.append("         СТАТИСТИКА ПО РЕСУРСАМ         \n");
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
}