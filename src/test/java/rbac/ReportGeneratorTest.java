package test.java.rbac;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.Optional;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ReportGeneratorTest {

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testGenerateUserReport() {
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        try {
            userManager.add(User.validate("admin", "System Admin", "admin@rbac.local"));
            userManager.add(User.validate("stas_", "Stas Makarov", "stas@example.com"));
            userManager.add(User.validate("andrey", "Andrey Lastnameov", "andrey@name.com"));
            userManager.add(User.validate("tea", "Чай Поставьте", "tea@black.drink"));
        } catch (Exception e) {
            fail("Ошибка при создании пользователей: " + e.getMessage());
        }

        try {
            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(new Permission("READ", "users", "View users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
            adminRole.addPermission(new Permission("DELETE", "users", "Delete users"));
            adminRole.addPermission(new Permission("READ", "roles", "View roles"));
            roleManager.add(adminRole);

            Role editorRole = new Role("ContentEditor", "Edit content");
            editorRole.addPermission(new Permission("READ", "articles", "View articles"));
            editorRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));
            roleManager.add(editorRole);

            Role viewerRole = new Role("Viewer", "Read-only access");
            viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
            viewerRole.addPermission(new Permission("READ", "articles", "View articles"));
            roleManager.add(viewerRole);
        } catch (Exception e) {
            fail("Ошибка при создании ролей: " + e.getMessage());
        }

        try {
            Optional<User> admin = userManager.findByUsername("admin");
            Optional<User> stas = userManager.findByUsername("stas_");
            Optional<User> andrey = userManager.findByUsername("andrey");
            Optional<User> tea = userManager.findByUsername("tea");

            Optional<Role> adminRole = roleManager.findByName("Administrator");
            Optional<Role> editorRole = roleManager.findByName("ContentEditor");
            Optional<Role> viewerRole = roleManager.findByName("Viewer");

            if (admin.isPresent() && adminRole.isPresent()) {
                AssignmentMetadata meta1 = AssignmentMetadata.now("system", "Initial setup");
                assignmentManager.add(new PermanentAssignment(admin.get(), adminRole.get(), meta1));
            }

            if (stas.isPresent() && editorRole.isPresent()) {
                AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");
                assignmentManager.add(new PermanentAssignment(stas.get(), editorRole.get(), meta2));
            }

            if (andrey.isPresent() && viewerRole.isPresent()) {
                AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Read access");
                assignmentManager.add(new PermanentAssignment(andrey.get(), viewerRole.get(), meta3));
            }

            if (tea.isPresent() && viewerRole.isPresent()) {
                AssignmentMetadata meta4 = AssignmentMetadata.now("admin", "Read access");
                assignmentManager.add(new PermanentAssignment(tea.get(), viewerRole.get(), meta4));
            }
        } catch (Exception e) {
            fail("Ошибка при создании назначений: " + e.getMessage());
        }

        String userReport = ReportGenerator.generateUserReport(userManager, assignmentManager);

        assertNotNull(userReport, "Отчёт не должен быть null");
        assertTrue(!userReport.isEmpty(), "Отчёт не должен быть пустым");
        assertTrue(userReport.contains("admin"), "Отчёт должен содержать 'admin'");
        assertTrue(userReport.contains("stas_"), "Отчёт должен содержать 'stas_'");
        assertTrue(userReport.contains("Administrator"), "Отчёт должен содержать 'Administrator'");
        assertTrue(userReport.contains("СТАТИСТИКА"), "Отчёт должен содержать раздел статистики");
    }

    @Test
    void testGenerateRoleReport() {
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        try {
            userManager.add(User.validate("admin", "System Admin", "admin@rbac.local"));
            userManager.add(User.validate("stas", "Stas Makarov", "stas@example.com"));
            userManager.add(User.validate("andrey", "Andrey Lastnameov", "andrey@mail.com"));
        } catch (Exception e) {
            fail("Ошибка при создании пользователей: " + e.getMessage());
        }

        try {
            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(new Permission("READ", "users", "View users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
            roleManager.add(adminRole);

            Role editorRole = new Role("ContentEditor", "Edit content");
            editorRole.addPermission(new Permission("READ", "articles", "View articles"));
            editorRole.addPermission(new Permission("WRITE", "articles", "Edit articles"));
            roleManager.add(editorRole);

            Role viewerRole = new Role("Viewer", "Read-only access");
            viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
            roleManager.add(viewerRole);
        } catch (Exception e) {
            fail("Ошибка при создании ролей: " + e.getMessage());
        }

        try {
            Optional<User> admin = userManager.findByUsername("admin");
            Optional<User> stas = userManager.findByUsername("stas_");
            Optional<User> andrey = userManager.findByUsername("andrey");

            Optional<Role> adminRole = roleManager.findByName("Administrator");
            Optional<Role> editorRole = roleManager.findByName("ContentEditor");
            Optional<Role> viewerRole = roleManager.findByName("Viewer");

            if (admin.isPresent() && adminRole.isPresent()) {
                AssignmentMetadata meta1 = AssignmentMetadata.now("system", "Initial setup");
                assignmentManager.add(new PermanentAssignment(admin.get(), adminRole.get(), meta1));
            }

            if (stas.isPresent() && editorRole.isPresent()) {
                AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");
                assignmentManager.add(new PermanentAssignment(stas.get(), editorRole.get(), meta2));
            }

            if (andrey.isPresent() && viewerRole.isPresent()) {
                AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Read access");
                assignmentManager.add(new PermanentAssignment(andrey.get(), viewerRole.get(), meta3));
            }
        } catch (Exception e) {
            fail("Ошибка при создании назначений: " + e.getMessage());
        }

        String roleReport = ReportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertNotNull(roleReport, "Отчёт не должен быть null");
        assertTrue(!roleReport.isEmpty(), "Отчёт не должен быть пустым");
        assertTrue(roleReport.contains("Administrator"), "Отчёт должен содержать 'Administrator'");
        assertTrue(roleReport.contains("ContentEditor"), "Отчёт должен содержать 'ContentEditor'");
        assertTrue(roleReport.contains("ТОП-3"), "Отчёт должен содержать раздел 'ТОП-3'");
    }

    @Test
    void testGeneratePermissionMatrix() {
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        try {
            userManager.add(User.validate("admin", "System Admin", "admin@rbac.local"));
            userManager.add(User.validate("stas", "Stas Makarov", "stas@example.com"));
        } catch (Exception e) {
            fail("Ошибка при создании пользователей: " + e.getMessage());
        }

        try {
            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(new Permission("READ", "users", "View users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
            roleManager.add(adminRole);

            Role editorRole = new Role("ContentEditor", "Edit content");
            editorRole.addPermission(new Permission("READ", "articles", "View articles"));
            roleManager.add(editorRole);
        } catch (Exception e) {
            fail("Ошибка при создании ролей: " + e.getMessage());
        }

        try {
            Optional<User> admin = userManager.findByUsername("admin");
            Optional<User> stas = userManager.findByUsername("stas_");

            Optional<Role> adminRole = roleManager.findByName("Administrator");
            Optional<Role> editorRole = roleManager.findByName("ContentEditor");

            if (admin.isPresent() && adminRole.isPresent()) {
                AssignmentMetadata meta1 = AssignmentMetadata.now("system", "Initial setup");
                assignmentManager.add(new PermanentAssignment(admin.get(), adminRole.get(), meta1));
            }

            if (stas.isPresent() && editorRole.isPresent()) {
                AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Content access");
                assignmentManager.add(new PermanentAssignment(stas.get(), editorRole.get(), meta2));
            }
        } catch (Exception e) {
            fail("Ошибка при создании назначений: " + e.getMessage());
        }

        String matrixReport = ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertNotNull(matrixReport, "Матрица не должна быть null");
        assertTrue(!matrixReport.isEmpty(), "Матрица не должна быть пустой");
        assertTrue(matrixReport.contains("admin"), "Матрица должна содержать 'admin'");
        assertTrue(matrixReport.contains("stas"), "Матрица должна содержать 'stas'");
        assertTrue(matrixReport.contains("users"), "Матрица должна содержать ресурс 'users'");
    }

    @Test
    void testExportToFile() throws IOException {
        String testReport = "Тестовый отчёт";
        String filename = "report_test_export.txt";

        ReportGenerator.exportToFile(testReport, filename);

        java.io.File file = new java.io.File(filename);
        assertTrue(file.exists(), "Файл должен существовать");
        assertTrue(file.length() > 0, "Файл не должен быть пустым");

        file.delete();
    }

    @Test
    void testExportToFileWithTimestamp() throws IOException {
        String testReport = "Тестовый отчёт с временной меткой";
        String baseFilename = "report_timestamp_test";

        ReportGenerator.exportToFileWithTimestamp(testReport, baseFilename);

        java.io.File dir = new java.io.File(".");
        java.io.File[] files = dir.listFiles((d, name) ->
                name.startsWith(baseFilename) && name.endsWith(".txt")
        );

        assertNotNull(files, "Должны существовать файлы");
        assertTrue(files.length > 0, "Должен быть создан хотя бы один файл");

        for (java.io.File file : files) {
            file.delete();
        }
    }

    @Test
    void testEmptyUserReport() {
        UserManager userManager = new UserManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        String userReport = ReportGenerator.generateUserReport(userManager, assignmentManager);

        assertNotNull(userReport, "Отчёт не должен быть null");
        assertTrue(userReport.contains("Нет зарегистрированных пользователей"),
                "Отчёт должен содержать сообщение об отсутствии пользователей");
    }

    @Test
    void testEmptyRoleReport() {
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        String roleReport = ReportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertNotNull(roleReport, "Отчёт не должен быть null");
        assertTrue(roleReport.contains("Нет зарегистрированных ролей"),
                "Отчёт должен содержать сообщение об отсутствии ролей");
    }
}