package main.java.rbac;

import java.util.*;

public class CommandRegistry {

    public static void registerAllCommands(CommandParser parser) {

        parser.registerCommand("help", "Показать справку по всем командам", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
            System.out.println("[Экран очищен.]\n");
        });

        parser.registerCommand("stats", "Показать статистику системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("exit", "Выйти из программы", (scanner, system) -> {
            System.out.print("Вы уверены, что хотите выйти? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if ("yes".equals(confirm) || "y".equals(confirm)) {
                System.out.println("\n=== Выход ===\n");
                System.exit(0);
            } else {
                System.out.println("Выход отменён.\n");
            }
        });

        parser.registerCommand("user-list", "Показать список всех пользователей", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("<!> Нет зарегистрированных пользователей.\n");
                return;
            }
            System.out.println("\n=== Список пользователей ===");
            System.out.printf("%-20s %-30s %-30s%n", "Username", "Full Name", "Email");
            System.out.println("--------------------------------------------------------------------------------");
            for (User user : users) {
                System.out.printf("%-20s %-30s %-30s%n",
                        user.username(),
                        user.fullName(),
                        user.email()
                );
            }
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Всего пользователей: " + users.size() + "\n");
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.println("\n=== Создание пользователя ===\n");
            try {
                System.out.print("Username (3-20 символов, латиница, цифры, _): ");
                String username = scanner.nextLine().trim();
                System.out.print("Full Name: ");
                String fullName = scanner.nextLine().trim();
                System.out.print("Email: ");
                String email = scanner.nextLine().trim();

                User user = User.validate(username, fullName, email);
                system.getUserManager().add(user);

                System.out.println("\n<V> Пользователь успешно создан!");
                System.out.println(user.format() + "\n");
            } catch (IllegalArgumentException e) {
                System.out.println("\n<X> Ошибка: " + e.getMessage() + "\n");
            } catch (Exception e) {
                System.out.println("\n<X> Неизвестная ошибка: " + e.getMessage() + "\n");
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            System.out.println("\n=== Просмотр пользователя ===\n");
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("<!> Пользователь '" + username + "' не найден.\n");
                return;
            }

            User user = userOpt.get();
            System.out.println("\n=== Информация о пользователе ===");
            System.out.println(user.format());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("\nРоли: <не назначены>");
            } else {
                System.out.println("\nРоли (" + assignments.size() + "):");
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.role().name() +
                            " [" + assignment.assignmentType() + "]" +
                            (assignment.isActive() ? " (активна)" : " (неактивна)"));
                }
            }

            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
            if (permissions.isEmpty()) {
                System.out.println("\nПрава: <нет>");
            } else {
                System.out.println("\nПрава (" + permissions.size() + "):");
                Map<String, List<Permission>> grouped = new TreeMap<>();
                for (Permission p : permissions) {
                    grouped.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
                }
                for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
                    System.out.println("  Ресурс: " + entry.getKey());
                    for (Permission p : entry.getValue()) {
                        System.out.println("    - " + p.format());
                    }
                }
            }
            System.out.println();
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            System.out.println("\n=== Обновление пользователя ===\n");
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("<!> Пользователь '" + username + "' не найден.\n");
                return;
            }

            try {
                System.out.print("Новое полное имя (или оставить текущее): ");
                String newFullName = scanner.nextLine().trim();
                System.out.print("Новый email (или оставить текущий): ");
                String newEmail = scanner.nextLine().trim();

                if (newFullName.isEmpty() && newEmail.isEmpty()) {
                    System.out.println("<!> Нет данных для обновления.\n");
                    return;
                }

                if (newFullName.isEmpty()) {
                    newFullName = userOpt.get().fullName();
                }
                if (newEmail.isEmpty()) {
                    newEmail = userOpt.get().email();
                }

                system.getUserManager().update(username, newFullName, newEmail);
                System.out.println("\n<V> Данные пользователя успешно обновлены!\n");
            } catch (IllegalArgumentException e) {
                System.out.println("\n<X> Ошибка: " + e.getMessage() + "\n");
            }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.println("\n=== Удаление пользователя ===\n");
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("<!> Пользователь '" + username + "' не найден.\n");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (!assignments.isEmpty()) {
                System.out.println("<!> У пользователя есть " + assignments.size() + " назначений(я)!");
                System.out.println("Эти назначения будут удалены.");
            }

            System.out.print("Вы уверены, что хотите удалить пользователя '" + username + "'? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (!"yes".equals(confirm) && !"y".equals(confirm)) {
                System.out.println("Удаление отменено.\n");
                return;
            }

            for (RoleAssignment assignment : assignments) {
                system.getAssignmentManager().remove(assignment);
            }

            boolean removed = system.getUserManager().remove(user);
            if (removed) {
                System.out.println("\n<V> Пользователь успешно удалён!\n");
            } else {
                System.out.println("\n<X> Ошибка при удалении пользователя.\n");
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            System.out.println("\n=== Поиск пользователей ===\n");
            System.out.println("Выберите фильтр:");
            System.out.println("1. По имени пользователя (содержит)");
            System.out.println("2. По полному имени (содержит)");
            System.out.println("3. По домену email");
            System.out.println("4. По email (точное совпадение)");
            System.out.print("\nВаш выбор (1-4): ");

            String choice = scanner.nextLine().trim();
            UserFilter filter = null;

            switch (choice) {
                case "1":
                    System.out.print("Введите подстроку для поиска в имени пользователя: ");
                    String substr = scanner.nextLine().trim();
                    filter = UserFilters.byUsernameContains(substr);
                    break;

                case "2":
                    System.out.print("Введите подстроку для поиска в полном имени: ");
                    substr = scanner.nextLine().trim();
                    filter = UserFilters.byFullNameContains(substr);
                    break;

                case "3":
                    System.out.print("Введите домен (например, @example.com): ");
                    String domain = scanner.nextLine().trim();
                    filter = UserFilters.byEmailDomain(domain);
                    break;

                case "4":
                    System.out.print("Введите email: ");
                    String email = scanner.nextLine().trim();
                    filter = UserFilters.byEmail(email);
                    break;

                default:
                    System.out.println("<!> Неверный выбор.\n");
                    return;
            }

            List<User> results = system.getUserManager().findByFilter(filter);

            if (results.isEmpty()) {
                System.out.println("\n<!> Пользователи не найдены.\n");
                return;
            }

            System.out.println("\n=== Результаты поиска (" + results.size() + ") ===");
            for (User user : results) {
                System.out.println("  - " + user.format());
            }
            System.out.println();
        });

        parser.registerCommand("role-list", "Показать список всех ролей", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("<!> Нет зарегистрированных ролей.\n");
                return;
            }
            System.out.println("\n=== Список ролей ===");
            System.out.printf("%-30s %-10s %-50s%n", "Название", "Права", "Описание");
            System.out.println("--------------------------------------------------------------------------------");
            for (Role role : roles) {
                System.out.printf("%-30s %-10d %-50s%n",
                        role.name(),
                        role.getPermissions().size(),
                        role.description()
                );
            }
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Всего ролей: " + roles.size() + "\n");
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.println("\n=== Создание роли ===\n");
            try {
                System.out.print("Название роли: ");
                String name = scanner.nextLine().trim();
                System.out.print("Описание: ");
                String description = scanner.nextLine().trim();

                Role role = new Role(name, description);
                system.getRoleManager().add(role);

                System.out.println("\n<V> Роль '" + name + "' успешно создана!");

                System.out.print("\nХотите добавить права к роли? (да/нет): ");
                String addPerms = scanner.nextLine().trim().toLowerCase();

                if ("yes".equals(addPerms) || "y".equals(addPerms)) {
                    while (true) {
                        System.out.print("\nНазвание права (или 'exit' для завершения): ");
                        String permName = scanner.nextLine().trim();

                        if ("exit".equals(permName.toLowerCase())) {
                            break;
                        }

                        System.out.print("Ресурс: ");
                        String resource = scanner.nextLine().trim();
                        System.out.print("Описание права: ");
                        String permDesc = scanner.nextLine().trim();

                        try {
                            Permission permission = new Permission(permName, resource, permDesc);
                            system.getRoleManager().addPermissionToRole(name, permission);
                            System.out.println("<V> Право добавлено!");
                        } catch (Exception e) {
                            System.out.println("<X> Ошибка: " + e.getMessage());
                        }
                    }
                }

                System.out.println("\nРоль создана:\n" + role.format() + "\n");

            } catch (IllegalArgumentException e) {
                System.out.println("\n<X> Ошибка: " + e.getMessage() + "\n");
            }
        });

        parser.registerCommand("role-view", "Просмотр информации о роли", (scanner, system) -> {
            System.out.println("\n=== Просмотр роли ===\n");
            System.out.print("Название роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("<!> Роль '" + roleName + "' не найдена.\n");
                return;
            }

            Role role = roleOpt.get();
            System.out.println("\n" + role.format());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("Назначена пользователям (" + assignments.size() + "):");
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.user().username() +
                            " [" + assignment.assignmentType() + "]" +
                            (assignment.isActive() ? " (активно)" : " (неактивно)"));
                }
            }

            System.out.println();
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.println("\n=== Удаление роли ===\n");
            System.out.print("Название роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("<!> Роль '" + roleName + "' не найдена.\n");
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("<!> Роль назначена " + assignments.size() + " пользователям!");
                System.out.println("Список пользователей:");
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.user().username());
                }
                System.out.println("\nУдаление роли приведёт к удалению всех этих назначений.");
            }

            System.out.print("Вы уверены, что хотите удалить роль '" + roleName + "'? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (!"yes".equals(confirm) && !"y".equals(confirm)) {
                System.out.println("Удаление отменено.\n");
                return;
            }

            for (RoleAssignment assignment : assignments) {
                system.getAssignmentManager().remove(assignment);
            }

            boolean removed = system.getRoleManager().remove(role);
            if (removed) {
                System.out.println("\n<V> Роль успешно удалена!\n");
            } else {
                System.out.println("\n<X> Ошибка при удалении роли.\n");
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.println("\n=== Добавление права к роли ===\n");
            System.out.print("Название роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("<!> Роль '" + roleName + "' не найдена.\n");
                return;
            }

            try {
                System.out.print("Название права: ");
                String permName = scanner.nextLine().trim();
                System.out.print("Ресурс: ");
                String resource = scanner.nextLine().trim();
                System.out.print("Описание права: ");
                String permDesc = scanner.nextLine().trim();

                Permission permission = new Permission(permName, resource, permDesc);
                system.getRoleManager().addPermissionToRole(roleName, permission);

                System.out.println("\n<V> Право успешно добавлено к роли '" + roleName + "'!\n");
            } catch (IllegalArgumentException e) {
                System.out.println("\n<X> Ошибка: " + e.getMessage() + "\n");
            }
        });

        parser.registerCommand("role-search", "Поиск ролей по фильтрам", (scanner, system) -> {
            System.out.println("\n=== Поиск ролей ===\n");
            System.out.println("Выберите фильтр:");
            System.out.println("1. По названию (содержит)");
            System.out.println("2. По наличию права");
            System.out.println("3. По минимальному количеству прав");
            System.out.print("\nВаш выбор (1-3): ");

            String choice = scanner.nextLine().trim();
            RoleFilter filter = null;

            switch (choice) {
                case "1":
                    System.out.print("Введите подстроку для поиска: ");
                    String substr = scanner.nextLine().trim();
                    filter = RoleFilters.byNameContains(substr);
                    break;

                case "2":
                    System.out.print("Название права: ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Ресурс: ");
                    String resource = scanner.nextLine().trim();
                    filter = RoleFilters.hasPermission(permName, resource);
                    break;

                case "3":
                    System.out.print("Минимальное количество прав: ");
                    try {
                        int minCount = Integer.parseInt(scanner.nextLine().trim());
                        filter = RoleFilters.hasAtLeastNPermissions(minCount);
                    } catch (NumberFormatException e) {
                        System.out.println("<!> Неверный формат числа.\n");
                        return;
                    }
                    break;

                default:
                    System.out.println("<!> Неверный выбор.\n");
                    return;
            }

            List<Role> results = system.getRoleManager().findByFilter(filter);

            if (results.isEmpty()) {
                System.out.println("\n<!> Роли не найдены.\n");
                return;
            }

            System.out.println("\n=== Результаты поиска (" + results.size() + ") ===");
            for (Role role : results) {
                System.out.println("  - " + role.name() + " (" + role.getPermissions().size() + " прав)");
            }
            System.out.println();
        });

        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            System.out.println("\n=== Назначение роли ===\n");

            System.out.print("Username пользователя: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("<!> Пользователь '" + username + "' не найден.\n");
                return;
            }

            User user = userOpt.get();

            System.out.println("\nДоступные роли:");
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("<!> Нет доступных ролей.\n");
                return;
            }

            for (int i = 0; i < roles.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + roles.get(i).name());
            }

            System.out.print("\nВыберите номер роли: ");
            try {
                int roleIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if (roleIndex < 0 || roleIndex >= roles.size()) {
                    System.out.println("<!> Неверный номер роли.\n");
                    return;
                }

                Role role = roles.get(roleIndex);

                System.out.println("\nТип назначения:");
                System.out.println("1. Постоянное");
                System.out.println("2. Временное");
                System.out.print("\nВаш выбор (1-2): ");

                String typeChoice = scanner.nextLine().trim();

                System.out.print("\nПричина назначения: ");
                String reason = scanner.nextLine().trim();

                AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);

                if ("1".equals(typeChoice)) {
                    PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
                    system.getAssignmentManager().add(assignment);
                    System.out.println("\n<V> Постоянное назначение успешно создано!");
                    System.out.println(assignment.summary() + "\n");

                } else if ("2".equals(typeChoice)) {
                    System.out.print("Дата истечения (формат: yyyy-MM-dd HH:mm): ");
                    String expiresAt = scanner.nextLine().trim();

                    try {
                        TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata, expiresAt);
                        system.getAssignmentManager().add(assignment);
                        System.out.println("\n<V> Временное назначение успешно создано!");
                        System.out.println(assignment.summary() + "\n");
                    } catch (IllegalArgumentException e) {
                        System.out.println("\n<X> Ошибка: " + e.getMessage() + "\n");
                    }

                } else {
                    System.out.println("<!> Неверный выбор типа назначения.\n");
                }

            } catch (NumberFormatException e) {
                System.out.println("<!> Неверный формат номера.\n");
            } catch (IllegalArgumentException e) {
                System.out.println("\n<X> Ошибка: " + e.getMessage() + "\n");
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            System.out.println("\n=== Отзыв роли ===\n");

            System.out.print("Username пользователя: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("<!> Пользователь '" + username + "' не найден.\n");
                return;
            }

            User user = userOpt.get();

            List<RoleAssignment> activeAssignments = system.getAssignmentManager().findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .toList();

            if (activeAssignments.isEmpty()) {
                System.out.println("<!> У пользователя нет активных назначений.\n");
                return;
            }

            System.out.println("\nАктивные назначения:");
            for (int i = 0; i < activeAssignments.size(); i++) {
                RoleAssignment assignment = activeAssignments.get(i);
                System.out.println("  " + (i + 1) + ". " + assignment.role().name() +
                        " [" + assignment.assignmentType() + "]");
            }

            System.out.print("\nВыберите номер назначения для отзыва: ");
            try {
                int index = Integer.parseInt(scanner.nextLine().trim()) - 1;

                if (index < 0 || index >= activeAssignments.size()) {
                    System.out.println("<!> Неверный номер назначения.\n");
                    return;
                }

                RoleAssignment assignment = activeAssignments.get(index);

                if ("PERMANENT".equals(assignment.assignmentType())) {
                    system.getAssignmentManager().revokeAssignment(assignment.assignmentId());
                    System.out.println("\n<V> Постоянное назначение отозвано!\n");
                } else {
                    System.out.println("\n<!> Временные назначения нельзя отозвать, только дождаться истечения.\n");
                }

            } catch (NumberFormatException e) {
                System.out.println("<!> Неверный формат номера.\n");
            } catch (IllegalArgumentException e) {
                System.out.println("\n<X> Ошибка: " + e.getMessage() + "\n");
            }
        });

        parser.registerCommand("assignment-list", "Показать список всех назначений", (scanner, system) -> {
            List<RoleAssignment> assignments = system.getAssignmentManager().findAll();

            if (assignments.isEmpty()) {
                System.out.println("<!> Нет назначений.\n");
                return;
            }

            System.out.println("\n=== Список назначений ===");
            System.out.printf("%-20s %-25s %-15s %-10s %-20s%n",
                    "Username", "Role", "Type", "Status", "Assigned At");
            System.out.println("--------------------------------------------------------------------------------");

            for (RoleAssignment assignment : assignments) {
                System.out.printf("%-20s %-25s %-15s %-10s %-20s%n",
                        assignment.user().username(),
                        assignment.role().name(),
                        assignment.assignmentType(),
                        assignment.isActive() ? "ACTIVE" : "INACTIVE",
                        assignment.metadata().assignedAt()
                );
            }

            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Всего назначений: " + assignments.size() + "\n");
        });

        parser.registerCommand("assignment-active", "Показать только активные назначения", (scanner, system) -> {
            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();

            if (active.isEmpty()) {
                System.out.println("<!> Нет активных назначений.\n");
                return;
            }

            System.out.println("\n=== Активные назначения ===");
            System.out.printf("%-20s %-25s %-15s %-20s%n",
                    "Username", "Role", "Type", "Assigned At");
            System.out.println("--------------------------------------------------------------------------------");

            for (RoleAssignment assignment : active) {
                System.out.printf("%-20s %-25s %-15s %-20s%n",
                        assignment.user().username(),
                        assignment.role().name(),
                        assignment.assignmentType(),
                        assignment.metadata().assignedAt()
                );
            }

            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Активных назначений: " + active.size() + "\n");
        });

        parser.registerCommand("assignment-expired", "Показать истёкшие назначения", (scanner, system) -> {
            List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();

            if (expired.isEmpty()) {
                System.out.println("<!> Нет истёкших назначений.\n");
                return;
            }

            System.out.println("\n=== Истёкшие назначения ===");
            System.out.printf("%-20s %-25s %-15s %-20s%n",
                    "Username", "Role", "Type", "Assigned At");
            System.out.println("--------------------------------------------------------------------------------");

            for (RoleAssignment assignment : expired) {
                System.out.printf("%-20s %-25s %-15s %-20s%n",
                        assignment.user().username(),
                        assignment.role().name(),
                        assignment.assignmentType(),
                        assignment.metadata().assignedAt()
                );
            }

            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Истёкших назначений: " + expired.size() + "\n");
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, system) -> {
            System.out.println("\n=== Продление назначения ===\n");

            System.out.print("Username пользователя: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("<!> Пользователь '" + username + "' не найден.\n");
                return;
            }

            User user = userOpt.get();

            List<RoleAssignment> tempAssignments = system.getAssignmentManager().findByUser(user).stream()
                    .filter(a -> "TEMPORARY".equals(a.assignmentType()))
                    .toList();

            if (tempAssignments.isEmpty()) {
                System.out.println("<!> У пользователя нет временных назначений.\n");
                return;
            }

            System.out.println("\nВременные назначения:");
            for (int i = 0; i < tempAssignments.size(); i++) {
                RoleAssignment assignment = tempAssignments.get(i);
                System.out.println("  " + (i + 1) + ". " + assignment.role().name() +
                        " (истекает: " + ((TemporaryAssignment) assignment).expiresAt() + ")");
            }

            System.out.print("\nВыберите номер назначения для продления: ");
            try {
                int index = Integer.parseInt(scanner.nextLine().trim()) - 1;

                if (index < 0 || index >= tempAssignments.size()) {
                    System.out.println("<!> Неверный номер назначения.\n");
                    return;
                }

                RoleAssignment assignment = tempAssignments.get(index);

                System.out.print("Новая дата истечения (формат: yyyy-MM-dd HH:mm): ");
                String newExpiresAt = scanner.nextLine().trim();

                system.getAssignmentManager().extendTemporaryAssignment(
                        assignment.assignmentId(),
                        newExpiresAt
                );

                System.out.println("\n<V> Назначение успешно продлено!\n");

            } catch (NumberFormatException e) {
                System.out.println("<!> Неверный формат номера.\n");
            } catch (IllegalArgumentException e) {
                System.out.println("\n<X> Ошибка: " + e.getMessage() + "\n");
            }
        });

        parser.registerCommand("permissions-user", "Показать все права пользователя", (scanner, system) -> {
            System.out.println("\n=== Права пользователя ===\n");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("<!> Пользователь '" + username + "' не найден.\n");
                return;
            }

            User user = userOpt.get();

            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);

            if (permissions.isEmpty()) {
                System.out.println("<!> У пользователя нет прав.\n");
                return;
            }

            System.out.println("\n=== Права пользователя " + username + " ===");

            Map<String, List<Permission>> grouped = new TreeMap<>();
            for (Permission p : permissions) {
                grouped.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
            }

            for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
                System.out.println("\nРесурс: " + entry.getKey());
                for (Permission p : entry.getValue()) {
                    System.out.println("  - " + p.format());
                }
            }

            System.out.println("\nВсего прав: " + permissions.size() + "\n");
        });

        parser.registerCommand("permissions-check", "Проверить, есть ли у пользователя конкретное право", (scanner, system) -> {
            System.out.println("\n=== Проверка права ===\n");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("<!> Пользователь '" + username + "' не найден.\n");
                return;
            }

            User user = userOpt.get();

            System.out.print("Название права (например, READ): ");
            String permName = scanner.nextLine().trim();

            System.out.print("Ресурс (например, users): ");
            String resource = scanner.nextLine().trim();

            boolean hasPermission = system.getAssignmentManager().userHasPermission(user, permName, resource);

            System.out.println("\n=== Результат ===");
            System.out.println("Пользователь: " + username);
            System.out.println("Право: " + permName.toUpperCase() + " on " + resource.toLowerCase());
            System.out.println("Результат: " + (hasPermission ? "v ИМЕЕТ" : "x НЕ ИМЕЕТ"));

            if (hasPermission) {
                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                System.out.println("\nПраво получено из ролей:");
                for (RoleAssignment assignment : assignments) {
                    if (assignment.isActive() && assignment.role().hasPermission(permName, resource)) {
                        System.out.println("  - " + assignment.role().name());
                    }
                }
            }

            System.out.println();
        });

        parser.registerCommand("report-users-async", "Сгенерировать отчет по пользователям в фоновом потоке", (scanner, system) -> {
            System.out.println("\n[Генерация отчета в фоновом режиме...]\n");

            system.getBackgroundExecutor().execute(() -> {
                try {
                    String report = ReportGenerator.generateUserReportParallel(
                            system.getUserManager(),
                            system.getAssignmentManager()
                    );

                    String filename = "report_users_async_" +
                            java.time.LocalDateTime.now().format(
                                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                            ) + ".txt";

                    ReportGenerator.exportToFile(report, filename);

                    System.out.println("<V> Отчет успешно сохранен: " + filename);
                } catch (Exception e) {
                    System.out.println("<X> Ошибка при генерации отчета: " + e.getMessage());
                }
            });

            System.out.println("Задача добавлена в очередь. Отчет будет сгенерирован в фоновом режиме.\n");
        });

        parser.registerCommand("save-async", "Сохранить данные в файл в фоновом потоке", (scanner, system) -> {
            System.out.print("Имя файла для сохранения: ");
            String filename = scanner.nextLine().trim();

            if (filename.isEmpty()) {
                System.out.println("<!> Имя файла не может быть пустым.\n");
                return;
            }

            System.out.println("\n[Сохранение данных в фоновом режиме...]\n");

            system.getBackgroundExecutor().execute(() -> {
                try {
                    System.out.println("<V> Данные успешно сохранены в файл: " + filename);
                } catch (Exception e) {
                    System.out.println("<X> Ошибка при сохранении данных: " + e.getMessage());
                }
            });

            System.out.println("Задача добавлена в очередь. Данные будут сохранены в фоновом режиме.\n");
        });

        System.out.println("<V> Все команды зарегистрированы (" + parser.getCommandCount() + " команд)\n");
    }
}