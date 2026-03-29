package main.java.rbac;

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
}