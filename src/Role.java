import java.util.*;
import java.util.stream.Collectors;

// Класс Role с String id, name, description, Set<Permission> permissions
public class Role {
    private final String id;
    private String name;
    private String description;
    private final Set<Permission> permissions;

    // Счетчик для генерации идентификаторов
    private static final List<String> names = new ArrayList<>();

    public Role(String name, String description) {
        validateName(name);
        validateDescription(description);

        this.id = UUID.randomUUID().toString();
        this.name = name.trim();
        this.description = description.trim();
        this.permissions = new HashSet<>();

        // Условие уникальности имени (name), сохранение в список
        names.add(this.name.toLowerCase());
    }

    Role(String id, String name, String description) {
        validateName(name);
        validateDescription(description);
        this.id = id;
        this.name = name.trim();
        this.description = description.trim();
        this.permissions = new HashSet<>();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название роли не может быть пустым!");
        }
        if (names.contains(name.trim().toLowerCase())) {
            throw new IllegalArgumentException("Роль '" + name.trim() + "' уже существует!");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Описание роли не может быть пустым!");
        }
    }

    public void addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Право не может быть null!");
        }
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Право не может быть null!");
        }
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        if (permissionName == null || resource == null) {
            return false;
        }
        String normName = permissionName.trim().toUpperCase();
        String normResource = resource.trim().toLowerCase();
        return permissions.stream()
                .anyMatch(p -> p.name().equals(normName) && p.resource().equals(normResource));
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public void setName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Новое название не может быть пустым!");
        }
        // Удаляем из списка
        names.remove(name.toLowerCase());
        try {
            validateName(newName);
            this.name = newName.trim();
            names.add(this.name.toLowerCase());
        } catch (IllegalArgumentException error) {
            // Но восстанавливаем если ошибка
            names.add(name.toLowerCase());
            throw error;
        }
    }

    public void setDescription(String newDescription) {
        validateDescription(newDescription);
        this.description = newDescription.trim();
    }

    // Переопределение equals и hashCode по id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(id, role.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    // Переопределение toString для вывода
    @Override
    public String toString() {
        return "Role{id='" + id + "', name='" + name + "', permissions=" + permissions.size() + "}";
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");

        if (permissions.isEmpty()) {
            sb.append("  - <no permissions>\n");
        } else {
            permissions.stream()
                    .sorted(Comparator.comparing(Permission::name))
                    .forEach(p -> sb.append("  - ").append(p.format()).append("\n"));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("\n[Тестирование класса Role].\n");

        // Тест 1
        try {
            Role admin = new Role("Administrator", "Full system access");
            admin.addPermission(new Permission("READ", "users", "View user list"));
            admin.addPermission(new Permission("WRITE", "users", "Create/edit users"));
            admin.addPermission(new Permission("DELETE", "users", "Delete users"));

            System.out.println("[v] Тест 1: Создана роль с 3 правами");
            System.out.println(admin.format());
        } catch (Exception e) {
            System.out.println("[x] Тест 1: " + e.getMessage());
        }

        // Тест 2
        try {
            new Role("Administrator", "Another admin");
            System.out.println("[x] Тест 2: ожидалось исключение из-за дублирования имени");
        } catch (IllegalArgumentException e) {
            System.out.println("[v] Тест 2: " + e.getMessage());
        }

        // Тест 3
        try {
            Role viewer = new Role("Viewer", "Read-only access");
            Permission readUsers = new Permission("READ", "users", "View users");
            viewer.addPermission(readUsers);

            boolean hasRead = viewer.hasPermission(readUsers);
            boolean hasWrite = viewer.hasPermission(new Permission("WRITE", "users", "Edit users"));

            System.out.println("\n[v] Тест 3.1 (есть право READ): " + hasRead);
            System.out.println("[v] Тест 3.2 (нет права WRITE): " + !hasWrite);
            System.out.println("\n[v] Тест 3");
        } catch (Exception e) {
            System.out.println("\n[x] Тест 3: " + e.getMessage());
        }

        // Тест 4
        try {
            Role editor = new Role("Editor", "Content editor");
            editor.addPermission(new Permission("WRITE", "articles", "Edit articles"));

            boolean hasWriteArticles = editor.hasPermission("WRITE", "articles");
            boolean hasDeleteArticles = editor.hasPermission("DELETE", "articles");

            System.out.println("\n[v] Тест 4.1 (WRITE on articles): " + hasWriteArticles);
            System.out.println("[v] Тест 4.2 (нет DELETE): " + !hasDeleteArticles);
            System.out.println("\n[v] Тест 4");
        } catch (Exception e) {
            System.out.println("\n[x] Тест 4: " + e.getMessage());
        }

        // Тест 5
        try {
            new Role("TestRole", "   ");
            System.out.println("\n[x] Тест 5: ожидалось исключение для пустого описания");
        } catch (IllegalArgumentException e) {
            System.out.println("\n[v] Тест 5: " + e.getMessage());
        }

        // Тест 6
        try {
            Role role = new Role("Tester", "Test role");
            role.addPermission(new Permission("TEST", "system", "Run tests"));

            Set<Permission> perms = role.getPermissions();
            try {
                perms.add(new Permission("HACK", "system", "fail"));
                System.out.println("[x] Тест 6: ожидалось исключение при модификации");
            } catch (UnsupportedOperationException e) {
                System.out.println("[v] Тест 6: возвращенная коллекция неизменяема");
            }
        } catch (Exception e) {
            System.out.println("[x] Тест 6: " + e.getMessage());
        }

        // Тест 7
        try {
            Role oldRole = new Role("OldName", "Description");
            oldRole.setName("NewName");
            System.out.println("[v] Тест 7: название изменено на '" + oldRole.name() + "'");
        } catch (Exception e) {
            System.out.println("[x] Тест 7: " + e.getMessage());
        }

        System.out.println("\n[Все тесты завершены].\n");
    }
}