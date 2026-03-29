package test.java.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RBACSystemTest {

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testCreateSystem() {
        RBACSystem system = new RBACSystem();

        assertNotNull(system.getUserManager());
        assertNotNull(system.getRoleManager());
        assertNotNull(system.getAssignmentManager());
        assertEquals("system", system.getCurrentUser());
    }

    @Test
    void testInitializeSystem() {
        RBACSystem system = new RBACSystem();

        system.initialize();

        assertEquals(1, system.getUserManager().count(), "Должен быть создан 1 пользователь (администратор)");
        assertEquals(3, system.getRoleManager().count(), "Должны быть созданы 3 роли");
        assertEquals(1, system.getAssignmentManager().count(), "Должно быть создано 1 назначение");
        assertEquals("admin", system.getCurrentUser(), "Текущий пользователь должен быть 'admin'");
    }

    @Test
    void testCheckAdminUserExists() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        Optional<User> admin = system.getUserManager().findByUsername("admin");

        assertTrue(admin.isPresent(), "Администратор должен быть найден");
        assertEquals("System Administrator", admin.get().fullName());
    }

    @Test
    void testCheckRolesExist() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        assertTrue(system.getRoleManager().exists("Administrator"), "Роль Administrator должна существовать");
        assertTrue(system.getRoleManager().exists("Manager"), "Роль Manager должна существовать");
        assertTrue(system.getRoleManager().exists("Viewer"), "Роль Viewer должна существовать");
    }

    @Test
    void testAdminHasRole() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        Optional<User> admin = system.getUserManager().findByUsername("admin");
        Optional<Role> adminRole = system.getRoleManager().findByName("Administrator");

        assertTrue(admin.isPresent());
        assertTrue(adminRole.isPresent());

        boolean hasRole = system.getAssignmentManager().userHasRole(admin.get(), adminRole.get());
        assertTrue(hasRole, "Администратор должен иметь роль Administrator");

        if (hasRole) {
            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(admin.get());
            assertTrue(permissions.size() > 0, "Администратор должен иметь права");
        }
    }

    @Test
    void testGenerateStatistics() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        String stats = system.generateStatistics();

        assertTrue(stats.contains("Статистика системы"), "Статистика должна содержать заголовок");
        assertTrue(stats.contains("Пользователи:"), "Статистика должна содержать раздел пользователей");
        assertTrue(stats.contains("Роли:"), "Статистика должна содержать раздел ролей");
        assertTrue(stats.contains("Назначения:"), "Статистика должна содержать раздел назначений");
    }

    @Test
    void testChangeCurrentUser() {
        RBACSystem system = new RBACSystem();

        system.setCurrentUser("admin");

        assertEquals("admin", system.getCurrentUser(), "Текущий пользователь должен быть изменён на 'admin'");
    }

    @Test
    void testCreateUserAndAssignRole() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        system.getUserManager().add(testUser);

        Optional<Role> viewerRole = system.getRoleManager().findByName("Viewer");
        assertTrue(viewerRole.isPresent(), "Роль Viewer должна существовать");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(testUser, viewerRole.get(), meta);
        system.getAssignmentManager().add(assignment);

        boolean hasRole = system.getAssignmentManager().userHasRole(testUser, viewerRole.get());
        assertTrue(hasRole, "Пользователь должен иметь роль Viewer");

        boolean canViewReports = system.getAssignmentManager().userHasPermission(
                testUser, "READ", "reports"
        );
        assertTrue(canViewReports, "Пользователь должен иметь право просматривать отчёты");
    }

    @Test
    void testUpdatedStatistics() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        User testUser = User.validate("stas_", "Stas Makarov", "stas@mail.com");
        system.getUserManager().add(testUser);

        String stats = system.generateStatistics();

        assertTrue(stats.contains("Всего: 2"), "Статистика должна показывать 2 пользователя");
    }

    @Test
    void testNullUsernameThrowsException() {
        RBACSystem system = new RBACSystem();

        assertThrows(IllegalArgumentException.class, () -> {
            system.setCurrentUser("");
        });
    }

    @Test
    void testEmptyUsernameThrowsException() {
        RBACSystem system = new RBACSystem();

        assertThrows(IllegalArgumentException.class, () -> {
            system.setCurrentUser("   ");
        });
    }
}