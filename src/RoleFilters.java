public class RoleFilters {

    public static RoleFilter byName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name не может быть null");
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Name не может быть пустым");
        }
        return role -> normalized.equals(role.name());
    }

    public static RoleFilter byNameContains(String substring) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring не может быть null");
        }
        String normalized = substring.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Substring не может быть пустым");
        }
        return role -> role.name().toLowerCase().contains(normalized);
    }

    public static RoleFilter hasPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission не может быть null");
        }
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        if (permissionName == null) {
            throw new IllegalArgumentException("PermissionName не может быть null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Resource не может быть null");
        }
        String normName = permissionName.trim().toUpperCase();
        String normResource = resource.trim().toLowerCase();
        return role -> role.hasPermission(normName, normResource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("N не может быть отрицательным");
        }
        return role -> role.getPermissions().size() >= n;
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование фильтров ролей.]\n");

        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        adminRole.addPermission(new Permission("DELETE", "users", "Delete users"));
        adminRole.addPermission(new Permission("READ", "logs", "View logs"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));

        Role[] roles = {adminRole, moderRole, viewerRole};

        System.out.println("[Тест 1]: Фильтр byName(\"Administrator\"):");
        RoleFilter filter1 = RoleFilters.byName("Administrator");
        for (Role r : roles) {
            if (filter1.test(r)) {
                System.out.println("[v] - " + r.name() + " (" + r.getPermissions().size() + " прав)");
            }
        }
        System.out.println();

        System.out.println("[Тест 2]: Фильтр byNameContains(\"Moder\"):");
        RoleFilter filter2 = RoleFilters.byNameContains("Moder");
        for (Role r : roles) {
            if (filter2.test(r)) {
                System.out.println("[v] - " + r.name());
            }
        }
        System.out.println();

        System.out.println("[Тест 3]: Фильтр hasPermission(READ on users):");
        Permission readUsers = new Permission("READ", "users", "View users");
        RoleFilter filter3 = RoleFilters.hasPermission(readUsers);
        for (Role r : roles) {
            if (filter3.test(r)) {
                System.out.println("[v] - " + r.name());
            }
        }
        System.out.println();

        System.out.println("[Тест 4]: Фильтр hasPermission(\"WRITE\", \"articles\"):");
        RoleFilter filter4 = RoleFilters.hasPermission("WRITE", "articles");
        for (Role r : roles) {
            if (filter4.test(r)) {
                System.out.println("[v] - " + r.name());
            }
        }
        System.out.println();

        System.out.println("[Тест 5]: Фильтр hasAtLeastNPermissions(3):");
        RoleFilter filter5 = RoleFilters.hasAtLeastNPermissions(3);
        for (Role r : roles) {
            if (filter5.test(r)) {
                System.out.println("[v] - " + r.name() + " (" + r.getPermissions().size() + " прав)");
            }
        }
        System.out.println();

        System.out.println("[Тест 6]: Комбинированный фильтр: название содержит \"Admin\" AND имеет право READ on users:");
        RoleFilter filter6 = RoleFilters.byNameContains("Admin")
                .and(RoleFilters.hasPermission("READ", "users"));
        for (Role r : roles) {
            if (filter6.test(r)) {
                System.out.println("[v] - " + r.name());
            }
        }
        System.out.println();

        System.out.println("[Тест 7]: Комбинированный фильтр: имеет право WRITE on users OR WRITE on articles:");
        RoleFilter filter7 = RoleFilters.hasPermission("WRITE", "users")
                .or(RoleFilters.hasPermission("WRITE", "articles"));
        for (Role r : roles) {
            if (filter7.test(r)) {
                System.out.println("[v] - " + r.name());
            }
        }
        System.out.println();

        System.out.println("[Тест 8]: Инвертированный фильтр: НЕ имеет минимум 2 права:");
        RoleFilter filter8 = RoleFilters.hasAtLeastNPermissions(2).negate();
        for (Role r : roles) {
            if (filter8.test(r)) {
                System.out.println("[v] - " + r.name() + " (" + r.getPermissions().size() + " прав)");
            }
        }
        System.out.println();

        System.out.println("[Все тесты завершены.]");
    }
}