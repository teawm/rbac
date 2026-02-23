import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new HashMap<>();

    @Override
    public void add(RoleAssignment item) {
        if (item == null) {
            throw new IllegalArgumentException("Assignment не может быть null");
        }

        boolean duplicateExists = assignments.values().stream()
                .anyMatch(existing ->
                        existing.user().equals(item.user()) &&
                                existing.role().equals(item.role()) &&
                                existing.isActive()
                );

        if (duplicateExists) {
            throw new IllegalArgumentException(
                    "Роль '" + item.role().name() + "' уже назначена пользователю '" +
                            item.user().username() + "'"
            );
        }

        assignments.put(item.assignmentId(), item);
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) {
            return false;
        }
        return assignments.remove(item.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(assignments.get(id.trim()));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return assignments.values().stream()
                .filter(assignment -> assignment.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return assignments.values().stream()
                .filter(assignment -> assignment.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = (filter != null)
                ? findByFilter(filter)
                : findAll();

        if (sorter != null) {
            result = result.stream()
                    .sorted(sorter)
                    .collect(Collectors.toList());
        }

        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(assignment -> !assignment.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        if (user == null || role == null) {
            return false;
        }
        return assignments.values().stream()
                .anyMatch(assignment ->
                        assignment.user().equals(user) &&
                                assignment.role().equals(role) &&
                                assignment.isActive()
                );
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        if (user == null || permissionName == null || resource == null) {
            return false;
        }

        Set<Permission> userPermissions = getUserPermissions(user);
        return userPermissions.stream()
                .anyMatch(p ->
                        p.name().equals(permissionName.trim().toUpperCase()) &&
                                p.resource().equals(resource.trim().toLowerCase())
                );
    }

    public Set<Permission> getUserPermissions(User user) {
        if (user == null) {
            return Collections.emptySet();
        }

        return assignments.values().stream()
                .filter(assignment -> assignment.user().equals(user) && assignment.isActive())
                .flatMap(assignment -> assignment.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignmentId не может быть пустым");
        }

        RoleAssignment assignment = assignments.get(assignmentId.trim());
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение с ID '" + assignmentId + "' не найдено");
        }

        if (assignment instanceof PermanentAssignment permanent) {
            permanent.revoke();
        } else {
            throw new IllegalArgumentException(
                    "Назначение типа '" + assignment.assignmentType() + "' нельзя отозвать"
            );
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignmentId не может быть пустым");
        }
        if (newExpirationDate == null || newExpirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("NewExpirationDate не может быть пустым");
        }

        RoleAssignment assignment = assignments.get(assignmentId.trim());
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение с ID '" + assignmentId + "' не найдено");
        }

        if (assignment instanceof TemporaryAssignment temporary) {
            temporary.extend(newExpirationDate);
        } else {
            throw new IllegalArgumentException(
                    "Назначение типа '" + assignment.assignmentType() + "' нельзя продлить"
            );
        }
    }

    public String getStatistics() {
        int total = count();
        int active = getActiveAssignments().size();
        int expired = getExpiredAssignments().size();
        long permanent = assignments.values().stream()
                .filter(a -> "PERMANENT".equals(a.assignmentType()))
                .count();
        long temporary = assignments.values().stream()
                .filter(a -> "TEMPORARY".equals(a.assignmentType()))
                .count();

        return String.format(
                "Всего назначений: %d\n" +
                        "  > Активных: %d\n" +
                        "  > Истёкших: %d\n" +
                        "  > остоянных: %d\n" +
                        "  > Временных: %d",
                total, active, expired, permanent, temporary
        );
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование AssignmentManager]\n");

        AssignmentManager manager = new AssignmentManager();

        User user1 = User.validate("stas_", "Stas Makarov", "stas@example.com");
        User user2 = User.validate("anton", "Anton Shtirlitz", "anton@mail.ru");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");

        System.out.println("[Тест 1]: Добавление постоянного назначения:");
        try {
            PermanentAssignment pa1 = new PermanentAssignment(user1, adminRole, meta1);
            manager.add(pa1);
            System.out.println("[v] Назначение добавлено");
            System.out.println("  > Количество назначений: " + manager.count());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 2]: Дублирование назначения:");
        try {
            PermanentAssignment pa2 = new PermanentAssignment(user1, adminRole, meta2);
            manager.add(pa2);
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Исключение получено: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3]: Поиск назначений по пользователю:");
        try {
            TemporaryAssignment ta1 = new TemporaryAssignment(
                    user2,
                    moderRole,
                    meta2,
                    java.time.LocalDateTime.now().plusDays(7).format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    )
            );
            manager.add(ta1);

            List<RoleAssignment> userAssignments = manager.findByUser(user2);
            System.out.println("[v] Найдено назначений: " + userAssignments.size());
            for (RoleAssignment a : userAssignments) {
                System.out.println("  > " + a.summary());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4]: Активные и истекшие назначения:");
        try {
            List<RoleAssignment> active = manager.getActiveAssignments();
            List<RoleAssignment> expired = manager.getExpiredAssignments();
            System.out.println("  > Активных: " + active.size());
            System.out.println("  > Истёкших: " + expired.size());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5]: Проверка наличия роли у пользователя:");
        try {
            boolean hasAdmin = manager.userHasRole(user1, adminRole);
            boolean hasModer = manager.userHasRole(user1, moderRole);
            System.out.println("[v] stas_ имеет роль Administrator: " + hasAdmin);
            System.out.println("[v] stas_ имеет роль Moderator: " + hasModer);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 6]: Получение всех прав пользователя:");
        try {
            Set<Permission> permissions = manager.getUserPermissions(user1);
            System.out.println("[v] Найдено прав: " + permissions.size());
            for (Permission p : permissions) {
                System.out.println("  > " + p.format());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 7]: Проверка наличия конкретного права:");
        try {
            boolean canReadUsers = manager.userHasPermission(user1, "READ", "users");
            boolean canWriteArticles = manager.userHasPermission(user1, "WRITE", "articles");
            System.out.println("[v] stas_ может READ users: " + canReadUsers);
            System.out.println("[v] stas_ может WRITE articles: " + canWriteArticles);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 8]: Фильтрация по типу назначения:");
        try {
            List<RoleAssignment> temporary = manager.findByFilter(
                    AssignmentFilters.byType("TEMPORARY")
            );
            System.out.println("[v] Найдено временных назначений: " + temporary.size());
            for (RoleAssignment a : temporary) {
                System.out.println(" > " + a.summary());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 9]: Отзыв постоянного назначения:");
        try {
            Optional<RoleAssignment> toRevoke = manager.findByUser(user1).stream().findFirst();
            if (toRevoke.isPresent()) {
                manager.revokeAssignment(toRevoke.get().assignmentId());
                System.out.println("[v] Назначение отозвано");
                System.out.println("  > Активных назначений: " + manager.getActiveAssignments().size());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 10]: Продление временного назначения:");
        try {
            Optional<RoleAssignment> toExtend = manager.findByUser(user2).stream().findFirst();
            if (toExtend.isPresent()) {
                manager.extendTemporaryAssignment(
                        toExtend.get().assignmentId(),
                        java.time.LocalDateTime.now().plusDays(30).format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        )
                );
                System.out.println("[v] Назначение продлено");
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 11]: Статистика по назначениям:");
        try {
            System.out.println(manager.getStatistics());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 12]: Комбинированный фильтр и сортировка:");
        try {
            AssignmentFilter filter = AssignmentFilters.byType("TEMPORARY")
                    .and(AssignmentFilters.activeOnly());
            List<RoleAssignment> result = manager.findAll(
                    filter,
                    AssignmentSorters.byUsername()
            );
            System.out.println("[v] Найдено: " + result.size() + " назначений");
            for (RoleAssignment a : result) {
                System.out.println("  > " + a.summary());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}