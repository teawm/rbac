package main.java.rbac;

import java.util.regex.Pattern;

public record Permission(String name, String resource, String description) {
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    public Permission {
        if (name == null || resource == null || description == null) {
            throw new IllegalArgumentException("Все поля обязательны!");
        }

        name = name.trim().replaceAll("\\s+", "").toUpperCase();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Название права не может быть пустым!");
        }
        if (!PERMISSION_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Название права должно содержать только буквы, цифры и подчеркивание!"
            );
        }

        resource = resource.trim().toLowerCase();
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым!");
        }

        description = description.trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Описание права не может быть пустым!");
        }
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = namePattern == null ||
                name.contains(namePattern.toUpperCase().trim());
        boolean resourceMatches = resourcePattern == null ||
                resource.contains(resourcePattern.toLowerCase().trim());
        return nameMatches && resourceMatches;
    }
}