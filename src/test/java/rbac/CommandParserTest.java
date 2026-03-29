package test.java.rbac;

import org.junit.jupiter.api.Test;
import java.util.Scanner;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTest {

    @Test
    void testRegisterCommand() {
        CommandParser parser = new CommandParser();

        parser.registerCommand("test-command", "Тестовая команда", (sc, sys) -> {
            System.out.println("Команда выполнена!");
        });

        assertEquals(1, parser.getCommandCount(), "Количество команд должно быть 1");
        assertTrue(parser.checkCommand("test-command"), "Команда должна существовать");
    }

    @Test
    void testExecuteRegisteredCommand() {
        CommandParser parser = new CommandParser();
        RBACSystem system = new RBACSystem();
        Scanner scanner = new Scanner(System.in);

        boolean[] executed = {false};
        parser.registerCommand("test", "Тест", (sc, sys) -> {
            executed[0] = true;
        });

        parser.executeCommand("test", scanner, system);

        assertTrue(executed[0], "Команда должна быть выполнена");
    }

    @Test
    void testExecuteNonExistentCommand() {
        CommandParser parser = new CommandParser();
        RBACSystem system = new RBACSystem();
        Scanner scanner = new Scanner(System.in);

        parser.executeCommand("non-existent", scanner, system);

        assertTrue(true);
    }

    @Test
    void testRegisterMultipleCommands() {
        CommandParser parser = new CommandParser();

        parser.registerCommand("user-list", "Показать список пользователей", (sc, sys) -> {});
        parser.registerCommand("role-list", "Показать список ролей", (sc, sys) -> {});
        parser.registerCommand("help", "Показать справку", (sc, sys) -> {});

        assertEquals(3, parser.getCommandCount(), "Должно быть зарегистрировано 3 команды");

        List<String> commands = parser.getCommandList();
        assertTrue(commands.contains("user-list"));
        assertTrue(commands.contains("role-list"));
        assertTrue(commands.contains("help"));
    }

    @Test
    void testCheckCommand() {
        CommandParser parser = new CommandParser();

        parser.registerCommand("user-list", "Показать список пользователей", (sc, sys) -> {});

        assertTrue(parser.checkCommand("user-list"), "Команда должна существовать");
        assertFalse(parser.checkCommand("non-existent"), "Команда не должна существовать");
    }

    @Test
    void testParseAndExecute() {
        CommandParser parser = new CommandParser();
        RBACSystem system = new RBACSystem();
        Scanner scanner = new Scanner(System.in);

        boolean[] executed = {false};
        parser.registerCommand("test", "Тест", (sc, sys) -> {
            executed[0] = true;
        });

        parser.parseAndExecute("test", scanner, system);

        assertTrue(executed[0], "Команда должна быть выполнена");
    }

    @Test
    void testParseAndExecuteWithArguments() {
        CommandParser parser = new CommandParser();
        RBACSystem system = new RBACSystem();
        Scanner scanner = new Scanner(System.in);

        boolean[] executed = {false};
        parser.registerCommand("user-list", "Показать список", (sc, sys) -> {
            executed[0] = true;
        });

        parser.parseAndExecute("user-list --filter=admin", scanner, system);

        assertTrue(executed[0], "Команда должна быть выполнена");
    }

    @Test
    void testClearAllCommands() {
        CommandParser parser = new CommandParser();

        parser.registerCommand("test1", "Тест 1", (sc, sys) -> {});
        parser.registerCommand("test2", "Тест 2", (sc, sys) -> {});

        assertEquals(2, parser.getCommandCount(), "До очистки должно быть 2 команды");

        parser.clear();

        assertEquals(0, parser.getCommandCount(), "После очистки не должно быть команд");
    }

    @Test
    void testRegisterCommandWithEmptyName() {
        CommandParser parser = new CommandParser();

        assertThrows(IllegalArgumentException.class, () -> {
            parser.registerCommand("", "Описание", (sc, sys) -> {});
        });
    }

    @Test
    void testRegisterCommandWithEmptyDescription() {
        CommandParser parser = new CommandParser();

        assertThrows(IllegalArgumentException.class, () -> {
            parser.registerCommand("test", "", (sc, sys) -> {});
        });
    }

    @Test
    void testRegisterCommandWithNullImplementation() {
        CommandParser parser = new CommandParser();

        assertThrows(IllegalArgumentException.class, () -> {
            parser.registerCommand("test", "Описание", null);
        });
    }

    @Test
    void testGetCommandList() {
        CommandParser parser = new CommandParser();

        parser.registerCommand("cmd1", "Команда 1", (sc, sys) -> {});
        parser.registerCommand("cmd2", "Команда 2", (sc, sys) -> {});
        parser.registerCommand("cmd3", "Команда 3", (sc, sys) -> {});

        List<String> commands = parser.getCommandList();

        assertEquals(3, commands.size(), "Должно быть 3 команды");
        assertTrue(commands.contains("cmd1"));
        assertTrue(commands.contains("cmd2"));
        assertTrue(commands.contains("cmd3"));
    }
}