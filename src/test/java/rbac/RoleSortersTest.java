package test.java.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class RoleSortersTest {

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testSortByName() {
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
        Arrays.sort(roles, RoleSorters.byName());

        assertEquals("Administrator", roles[0].name());
        assertEquals("Manager", roles[1].name());
        assertEquals("Moderator", roles[2].name());
        assertEquals("Viewer", roles[3].name());
    }

    @Test
    void testSortByPermissionCountAscending() {
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
        Arrays.sort(roles, RoleSorters.byPermissionCount());

        assertEquals(1, roles[0].getPermissions().size());
        assertEquals(2, roles[1].getPermissions().size());
        assertEquals(3, roles[2].getPermissions().size());
        assertEquals(4, roles[3].getPermissions().size());
    }

    @Test
    void testSortByPermissionCountDescending() {
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
        Arrays.sort(roles, RoleSorters.byPermissionCountDesc());

        assertEquals(4, roles[0].getPermissions().size());
        assertEquals(3, roles[1].getPermissions().size());
        assertEquals(2, roles[2].getPermissions().size());
        assertEquals(1, roles[3].getPermissions().size());
    }

    @Test
    void testSortByDescriptionLength() {
        Role adminRole = new Role("Administrator", "Full system access");
        Role moderRole = new Role("Moderator", "Edit content");
        Role viewerRole = new Role("Viewer", "Read-only access");
        Role managerRole = new Role("Manager", "Manage teams");

        Role[] roles = {adminRole, moderRole, viewerRole, managerRole};
        Arrays.sort(roles, RoleSorters.byDescriptionLength());

        assertTrue(roles[0].description().length() <= roles[1].description().length());
        assertTrue(roles[1].description().length() <= roles[2].description().length());
        assertTrue(roles[2].description().length() <= roles[3].description().length());
    }

    @Test
    void testCombinedSortByPermissionCountThenName() {
        Role adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));

        Role moderRole = new Role("Moderator", "Edit content");
        moderRole.addPermission(new Permission("READ", "articles", "View articles"));
        moderRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));

        Role managerRole = new Role("Manager", "Manage teams");
        managerRole.addPermission(new Permission("READ", "teams", "View teams"));
        managerRole.addPermission(new Permission("WRITE", "teams", "Edit teams"));

        Role[] roles = {adminRole, moderRole, viewerRole, managerRole};
        Arrays.sort(roles, RoleSorters.byPermissionCount().thenComparing(RoleSorters.byName()));

        assertEquals("Viewer", roles[0].name());
        assertEquals("Administrator", roles[1].name());
        assertEquals("Manager", roles[2].name());
        assertEquals("Moderator", roles[3].name());
    }
}