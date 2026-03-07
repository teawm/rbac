package rbac;

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
                        "  > Постоянных: %d\n" +
                        "  > Временных: %d",
                total, active, expired, permanent, temporary
        );
    }
}