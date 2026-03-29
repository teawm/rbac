package rbac;

import java.util.*;
import java.util.stream.Collectors;

public class Role {
    private final String id;
    private String name;
    private String description;
    private final Set<Permission> permissions;

    private static final Set<String> existingNames = new HashSet<>();

    public Role(String name, String description) {
        validateName(name);
        validateDescription(description);
        this.id = UUID.randomUUID().toString();
        this.name = name.trim();
        this.description = description.trim();
        this.permissions = new HashSet<>();
        existingNames.add(this.name.toLowerCase());
    }

    Role(String id, String name, String description) {
        validateName(name);
        validateDescription(description);
        this.id = id;
        this.name = name.trim();
        this.description = description.trim();
        this.permissions = new HashSet<>();
        existingNames.add(this.name.toLowerCase());
    }

    static void clearExistingNamesForTesting() {
        existingNames.clear();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название роли не может быть пустым!");
        }
        String normalizedName = name.trim().toLowerCase();
        if (existingNames.contains(normalizedName)) {
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

        existingNames.remove(name.toLowerCase());

        try {
            validateName(newName);
            this.name = newName.trim();
            existingNames.add(this.name.toLowerCase());
        } catch (IllegalArgumentException e) {
            existingNames.add(name.toLowerCase());
            throw e;
        }
    }

    public void setDescription(String newDescription) {
        validateDescription(newDescription);
        this.description = newDescription.trim();
    }

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
}