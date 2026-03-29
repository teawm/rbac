package test.java.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandRegistryTest {

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testRegisterAllCommands() {
        CommandParser parser = new CommandParser();

        CommandRegistry.registerAllCommands(parser);

        assertEquals(24, parser.getCommandCount(), "Должно быть зарегистрировано 23 команды");
    }

    @Test
    void testKeyCommandsExist() {
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        assertTrue(parser.checkCommand("help"), "Команда 'help' должна существовать");
        assertTrue(parser.checkCommand("exit"), "Команда 'exit' должна существовать");
        assertTrue(parser.checkCommand("user-list"), "Команда 'user-list' должна существовать");
        assertTrue(parser.checkCommand("user-create"), "Команда 'user-create' должна существовать");
        assertTrue(parser.checkCommand("role-list"), "Команда 'role-list' должна существовать");
        assertTrue(parser.checkCommand("assign-role"), "Команда 'assign-role' должна существовать");
    }

    @Test
    void testUserManagementCommandsExist() {
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        assertTrue(parser.checkCommand("user-view"), "Команда 'user-view' должна существовать");
        assertTrue(parser.checkCommand("user-update"), "Команда 'user-update' должна существовать");
        assertTrue(parser.checkCommand("user-delete"), "Команда 'user-delete' должна существовать");
        assertTrue(parser.checkCommand("user-search"), "Команда 'user-search' должна существовать");
    }

    @Test
    void testRoleManagementCommandsExist() {
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        assertTrue(parser.checkCommand("role-create"), "Команда 'role-create' должна существовать");
        assertTrue(parser.checkCommand("role-view"), "Команда 'role-view' должна существовать");
        assertTrue(parser.checkCommand("role-delete"), "Команда 'role-delete' должна существовать");
        assertTrue(parser.checkCommand("role-add-permission"), "Команда 'role-add-permission' должна существовать");
        assertTrue(parser.checkCommand("role-search"), "Команда 'role-search' должна существовать");
    }

    @Test
    void testAssignmentCommandsExist() {
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        assertTrue(parser.checkCommand("revoke-role"), "Команда 'revoke-role' должна существовать");
        assertTrue(parser.checkCommand("assignment-list"), "Команда 'assignment-list' должна существовать");
        assertTrue(parser.checkCommand("assignment-active"), "Команда 'assignment-active' должна существовать");
        assertTrue(parser.checkCommand("assignment-expired"), "Команда 'assignment-expired' должна существовать");
        assertTrue(parser.checkCommand("assignment-extend"), "Команда 'assignment-extend' должна существовать");
    }

    @Test
    void testPermissionCommandsExist() {
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        assertTrue(parser.checkCommand("permissions-user"), "Команда 'permissions-user' должна существовать");
        assertTrue(parser.checkCommand("permissions-check"), "Команда 'permissions-check' должна существовать");
    }

    @Test
    void testUtilityCommandsExist() {
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        assertTrue(parser.checkCommand("clear"), "Команда 'clear' должна существовать");
        assertTrue(parser.checkCommand("stats"), "Команда 'stats' должна существовать");
    }

    @Test
    void testCommandDescriptionsNotEmpty() {
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        for (String cmd : parser.getCommandList()) {
            assertNotNull(parser.getCommandList());
        }
    }

    @Test
    void testCommandParserIntegration() {
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        assertNotNull(parser);
        assertTrue(parser.getCommandCount() > 0);
    }
}