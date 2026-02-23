import java.util.Comparator;

public class RoleSorters {

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::name);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparing(role -> role.getPermissions().size());
    }

    public static Comparator<Role> byPermissionCountDesc() {
        return Comparator.comparing((Role role) -> role.getPermissions().size()).reversed();
    }

    public static Comparator<Role> byDescriptionLength() {
        return Comparator.comparing(role -> role.description().length());
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование сортировки ролей.]\n");

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

        Role managerRole = new Role("Manager", "Manage teams");
        managerRole.addPermission(new Permission("READ", "teams", "View teams"));
        managerRole.addPermission(new Permission("WRITE", "teams", "Edit teams"));
        managerRole.addPermission(new Permission("READ", "reports", "View reports"));

        Role[] roles = {adminRole, moderRole, viewerRole, managerRole};

        System.out.println("[Тест 1]: Сортировка по названию:");
        java.util.Arrays.sort(roles, RoleSorters.byName());
        for (Role r : roles) {
            System.out.println(r.name());
        }
        System.out.println();

        System.out.println("[Тест 2]: Сортировка по количеству прав (по возрастанию):");
        java.util.Arrays.sort(roles, RoleSorters.byPermissionCount());
        for (Role r : roles) {
            System.out.println(r.name() + " (" + r.getPermissions().size() + " прав)");
        }
        System.out.println();

        System.out.println("[Тест 3]: Сортировка по количеству прав (по убыванию):");
        java.util.Arrays.sort(roles, RoleSorters.byPermissionCountDesc());
        for (Role r : roles) {
            System.out.println(r.name() + " (" + r.getPermissions().size() + " прав)");
        }
        System.out.println();

        System.out.println("[Тест 4]: Сортировка по длине описания:");
        java.util.Arrays.sort(roles, RoleSorters.byDescriptionLength());
        for (Role r : roles) {
            System.out.println(r.name() + " (" + r.description().length() + " символов)");
        }
        System.out.println();

        System.out.println("[Тест 5]: Комбинированная сортировка: по количеству прав, затем по названию:");
        java.util.Arrays.sort(roles, RoleSorters.byPermissionCount().thenComparing(RoleSorters.byName()));
        for (Role r : roles) {
            System.out.println(r.name() + " (" + r.getPermissions().size() + " прав)");
        }
        System.out.println();

        System.out.println("[Все тесты завершены]");
    }
}