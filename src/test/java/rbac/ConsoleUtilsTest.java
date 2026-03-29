package test.java.rbac;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleUtilsTest {

    @Test
    void testPromptStringRequired() {
        String input = "Test Input\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptString(scanner, "Enter: ", true);

        assertEquals("Test Input", result);
        scanner.close();
    }

    @Test
    void testPromptStringOptionalEmpty() {
        String input = "\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptString(scanner, "Enter: ", false);

        assertEquals("", result);
        scanner.close();
    }

    @Test
    void testPromptIntInRange() {
        String input = "5\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        int result = ConsoleUtils.promptInt(scanner, "Enter (1-10): ", 1, 10);

        assertEquals(5, result);
        scanner.close();
    }

    @Test
    void testPromptIntOutOfRange() {
        String input = "15\n5\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        int result = ConsoleUtils.promptInt(scanner, "Enter (1-10): ", 1, 10);

        assertEquals(5, result);
        scanner.close();
    }

    @Test
    void testPromptYesNo() {
        String input = "yes\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");

        assertTrue(result);
        scanner.close();
    }

    @Test
    void testPromptYesNoNo() {
        String input = "no\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");

        assertFalse(result);
        scanner.close();
    }

    @Test
    void testPromptChoice() {
        String input = "2\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3");
        String result = ConsoleUtils.promptChoice(scanner, "Choose:", options);

        assertEquals("Option 2", result);
        scanner.close();
    }

    @Test
    void testPromptChoiceWithCancel() {
        String input = "0\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3");
        String result = ConsoleUtils.promptChoiceWithCancel(scanner, "Choose:", options, true);

        assertNull(result);
        scanner.close();
    }

    @Test
    void testPromptEmailValid() {
        String input = "test@example.com\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptEmail(scanner, "Email:");

        assertEquals("test@example.com", result);
        scanner.close();
    }

    @Test
    void testPromptDateValid() {
        String input = "2026-02-15\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptDate(scanner, "Date:", false);

        assertEquals("2026-02-15", result);
        scanner.close();
    }

    @Test
    void testPromptDateWithTimeValid() {
        String input = "2026-02-15 19:45\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptDate(scanner, "Date:", false);

        assertEquals("2026-02-15 19:45", result);
        scanner.close();
    }

    @Test
    void testPromptDouble() {
        String input = "3.14\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        double result = ConsoleUtils.promptDouble(scanner, "Enter number:");

        assertEquals(3.14, result, 0.001);
        scanner.close();
    }

    @Test
    void testPromptYesNoCustom() {
        String input = "yes\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        java.util.Set<String> trueValues = new HashSet<>(Arrays.asList("yes", "y"));
        java.util.Set<String> falseValues = new HashSet<>(Arrays.asList("no", "n"));

        boolean result = ConsoleUtils.promptYesNoCustom(scanner, "Подтвердите", trueValues, falseValues);

        assertTrue(result);
        scanner.close();
    }

    @Test
    void testClearScreen() {
        assertDoesNotThrow(() -> {
            ConsoleUtils.clearScreen();
        });
    }

    @Test
    void testPrintSeparator() {
        assertDoesNotThrow(() -> {
            ConsoleUtils.printSeparator(10);
        });
    }

    @Test
    void testPrintSectionHeader() {
        assertDoesNotThrow(() -> {
            ConsoleUtils.printSectionHeader("Test Header");
        });
    }

    @Test
    void testPrintMessages() {
        assertDoesNotThrow(() -> {
            ConsoleUtils.printSuccess("Success");
            ConsoleUtils.printError("Error");
            ConsoleUtils.printWarning("Warning");
            ConsoleUtils.printInfo("Info");
        });
    }
}