package test.java.rbac;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FormatUtilsTest {

    @Test
    void testFormatTableWithAutomaticWidth() {
        String[] headers = {"Username", "Full Name", "Email"};
        List<String[]> rows = List.of(
                new String[]{"admin", "System Administrator", "admin@rbac.local"},
                new String[]{"stas", "Stas Makarov", "stas@mail.com"},
                new String[]{"andrey", "Andrey Withlonglastname", "user@longname.lol"}
        );

        String table = FormatUtils.formatTable(headers, rows);

        assertNotNull(table);
        assertTrue(table.contains("Username"));
        assertTrue(table.contains("admin"));
        assertTrue(table.contains("stas"));
        assertTrue(table.contains("+"));
        assertTrue(table.contains("|"));
    }

    @Test
    void testFormatTableWithFixedWidth() {
        String[] headers = {"Username", "Full Name", "Email"};
        List<String[]> rows = List.of(
                new String[]{"admin", "System Administrator", "admin@rbac.local"},
                new String[]{"stas", "Stas Makarov", "stas@mail.com"}
        );
        int[] columnWidths = {10, 20, 20};

        String table = FormatUtils.formatTable(headers, rows, columnWidths);

        assertNotNull(table);
        assertTrue(table.contains("admin"));
        assertTrue(table.contains("stas"));
        assertTrue(table.contains("+"));
        assertTrue(table.contains("|"));
    }
}