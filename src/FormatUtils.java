import java.util.List;

public class FormatUtils {

    public static String truncate(String text, int maxLength) {
        if (text == null || maxLength <= 0) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        if (maxLength <= 3) {
            return text.substring(0, maxLength);
        }

        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return " ".repeat(length - text.length()) + text;
    }

    public static String formatTable(String[] headers, List<String[]> rows, int[] columnWidths) {
        if (columnWidths == null) {
            columnWidths = new int[headers.length];

            for (int i = 0; i < headers.length; i++) {
                columnWidths[i] = headers[i].length();
            }

            for (String[] row : rows) {
                for (int i = 0; i < row.length && i < columnWidths.length; i++) {
                    if (row[i] != null && row[i].length() > columnWidths[i]) {
                        columnWidths[i] = row[i].length();
                    }
                }
            }
        } else {
            if (columnWidths.length != headers.length) {
                throw new IllegalArgumentException("Длина массива columnWidths должна соответствовать количеству заголовков");
            }

            for (int i = 0; i < headers.length; i++) {
                if (columnWidths[i] < headers[i].length()) {
                    columnWidths[i] = headers[i].length();
                }
            }
        }

        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] += 2;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("+");
        for (int i = 0; i < columnWidths.length; i++) {
            sb.append("-".repeat(columnWidths[i]));
            if (i < columnWidths.length - 1) {
                sb.append("+");
            }
        }
        sb.append("+\n");

        sb.append("|");
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (columnWidths[i] - 2 < header.length()) {
                header = truncate(header, columnWidths[i] - 2);
            }
            sb.append(" ").append(padRight(header, columnWidths[i] - 2)).append(" |");
        }
        sb.append("\n");

        sb.append("+");
        for (int i = 0; i < columnWidths.length; i++) {
            sb.append("-".repeat(columnWidths[i]));
            if (i < columnWidths.length - 1) {
                sb.append("+");
            }
        }
        sb.append("+\n");

        for (String[] row : rows) {
            sb.append("|");
            for (int i = 0; i < columnWidths.length; i++) {
                String cell = (i < row.length) ? row[i] : "";

                if (columnWidths[i] - 2 < cell.length()) {
                    cell = truncate(cell, columnWidths[i] - 2);
                }

                sb.append(" ").append(padRight(cell, columnWidths[i] - 2)).append(" |");
            }
            sb.append("\n");
        }

        sb.append("+");
        for (int i = 0; i < columnWidths.length; i++) {
            sb.append("-".repeat(columnWidths[i]));
            if (i < columnWidths.length - 1) {
                sb.append("+");
            }
        }
        sb.append("+");

        return sb.toString();
    }

    public static String formatBox(String text) {
        String[] lines = text.split("\n");
        int maxLength = 0;
        for (String line : lines) {
            maxLength = Math.max(maxLength, line.length());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("+").append("-".repeat(maxLength + 2)).append("+\n");
        for (String line : lines) {
            sb.append("| ").append(padRight(line, maxLength)).append(" |\n");
        }
        sb.append("+").append("-".repeat(maxLength + 2)).append("+");

        return sb.toString();
    }

    public static String formatHeader(String text) {
        return "\n\n" + formatBox(text) + "\n";
    }

    public static void main(String[] args) {
        System.out.println("[Тестирование FormatUtils.]\n");

        System.out.println("[Тест 1] Таблица:");
        try {
            String[] headers = {"Username", "Full Name", "Email"};
            List<String[]> rows = List.of(
                    new String[]{"admin", "System Administrator", "admin@rbac.local"},
                    new String[]{"stas", "Stas Makarov", "stas@mail.com"},
                    new String[]{"andrey", "Andrey Withlonglastname", "user@longname.lol"}
            );
            int[] columnWidths = {10, 20, 20};
            String table = formatTable(headers, rows, columnWidths);
            System.out.println(table);
        } catch (Exception e) {
            System.out.println("[x] Ошибка: " + e.getMessage());
        }
        System.out.println();

        System.out.println("[Тест завершен.]");
    }
}