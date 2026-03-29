package main.java.rbac;

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
}