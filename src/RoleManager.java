import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new HashMap<>();

    private final Map<String, Role> rolesByName = new HashMap<>();

    @Override
    public void add(Role item) {
        if (item == null) {
            throw new IllegalArgumentException("Role не может быть null");
        }

        if (rolesByName.containsKey(item.name().toLowerCase())) {
            throw new IllegalArgumentException("Роль с названием '" + item.name() + "' уже существует");
        }

        rolesById.put(item.id(), item);
        rolesByName.put(item.name().toLowerCase(), item);
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) {
            return false;
        }

        Role removed = rolesById.remove(item.id());
        if (removed != null) {
            rolesByName.remove(removed.name().toLowerCase());
            return true;
        }

        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(rolesById.get(id.trim()));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(rolesByName.get(name.trim().toLowerCase()));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = (filter != null)
                ? findByFilter(filter)
                : findAll();

        if (sorter != null) {
            result = result.stream()
                    .sorted(sorter)
                    .collect(Collectors.toList());
        }

        return result;
    }

    public boolean exists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return rolesByName.containsKey(name.trim().toLowerCase());
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("RoleName не может быть пустым");
        }
        if (permission == null) {
            throw new IllegalArgumentException("Permission не может быть null");
        }

        Role role = rolesByName.get(roleName.trim().toLowerCase());
        if (role == null) {
            throw new IllegalArgumentException("Роль '" + roleName + "' не найдена");
        }

        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("RoleName не может быть пустым");
        }
        if (permission == null) {
            throw new IllegalArgumentException("Permission не может быть null");
        }

        Role role = rolesByName.get(roleName.trim().toLowerCase());
        if (role == null) {
            throw new IllegalArgumentException("Роль '" + roleName + "' не найдена");
        }

        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new IllegalArgumentException("PermissionName не может быть пустым");
        }
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource не может быть пустым");
        }

        String normName = permissionName.trim().toUpperCase();
        String normResource = resource.trim().toLowerCase();

        return rolesById.values().stream()
                .filter(role -> role.hasPermission(normName, normResource))
                .collect(Collectors.toList());
    }

    public void updateDescription(String roleName, String newDescription) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("RoleName не может быть пустым");
        }
        if (newDescription == null || newDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Description не может быть пустым");
        }

        Role role = rolesByName.get(roleName.trim().toLowerCase());
        if (role == null) {
            throw new IllegalArgumentException("Роль '" + roleName + "' не найдена");
        }

        role.setDescription(newDescription);
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование RoleManager]\n");

        RoleManager manager = new RoleManager();

        System.out.println("[Тест 1]: Добавление роли:");
        try {
            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(new Permission("READ", "users", "View users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

            manager.add(adminRole);
            System.out.println("[v] Роль добавлена");
            System.out.println("  > Количество ролей: " + manager.count());
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 2]: Поиск по имени:");
        try {
            Optional<Role> found = manager.findByName("Administrator");
            if (found.isPresent()) {
                System.out.println("[v] Найдена: " + found.get().name());
                System.out.println("  > Прав: " + found.get().getPermissions().size());
            } else {
                System.out.println("[x] Роль не найдена");
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 3]: Дублирование имени роли:");
        try {
            Role duplicate = new Role("Administrator", "Another admin");
            manager.add(duplicate);
            System.out.println("[x] Ошибка: ожидалось исключение");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Исключение получено: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 4]: Фильтрация по наличию права:");
        try {
            Role moderRole = new Role("Moderator", "Edit content");
            moderRole.addPermission(new Permission("READ", "articles", "View articles"));
            moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));
            manager.add(moderRole);

            Role viewerRole = new Role("Viewer", "Read-only access");
            viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
            manager.add(viewerRole);

            List<Role> filtered = manager.findByFilter(
                    RoleFilters.hasPermission("WRITE", "articles")
            );
            System.out.println("[v] Найдено ролей: " + filtered.size());
            for (Role r : filtered) {
                System.out.println("  > " + r.name());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 5]: Сортировка по количеству прав:");
        try {
            List<Role> sorted = manager.findAll(null, RoleSorters.byPermissionCountDesc());
            for (Role r : sorted) {
                System.out.println("  > " + r.name() + " (" + r.getPermissions().size() + " прав)");
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 6]: Добавление права к существующей роли:");
        try {
            manager.addPermissionToRole("Viewer", new Permission("READ", "logs", "View logs"));
            Optional<Role> updated = manager.findByName("Viewer");
            if (updated.isPresent()) {
                System.out.println("[v] Прав добавлено: " + updated.get().getPermissions().size());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 7]: Поиск ролей с правом READ on users:");
        try {
            List<Role> rolesWithRead = manager.findRolesWithPermission("READ", "users");
            System.out.println("[v] Найдено ролей: " + rolesWithRead.size());
            for (Role r : rolesWithRead) {
                System.out.println("  > " + r.name());
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 8]: Удаление роли:");
        try {
            Optional<Role> toRemove = manager.findByName("Viewer");
            if (toRemove.isPresent()) {
                boolean removed = manager.remove(toRemove.get());
                System.out.println("[v] Удалена: " + removed);
                System.out.println("  > Количество ролей: " + manager.count());
            }
        } catch (Exception e) {
            System.out.println("  ✗ Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 9]: Комбинированный фильтр и сортировка:");
        try {
            RoleFilter filter = RoleFilters.byNameContains("admin")
                    .or(RoleFilters.hasAtLeastNPermissions(2));
            List<Role> result = manager.findAll(filter, RoleSorters.byName());
            System.out.println("[v] Найдено: " + result.size() + " ролей");
            for (Role r : result) {
                System.out.println("  > " + r.name() + " (" + r.getPermissions().size() + " прав)");
            }
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест 10]: Проверка существования:");
        System.out.println("[v] Administrator существует: " + manager.exists("Administrator"));
        System.out.println("[v] idk не существует: " + manager.exists("idk"));
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}