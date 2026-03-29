package rbac;

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
}