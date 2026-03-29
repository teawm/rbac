package main.java.rbac;

public class UserFilters {

    public static UserFilter byUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username не может быть null");
        }
        String normalized = username.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }
        return user -> normalized.equals(user.username());
    }

    public static UserFilter byUsernameContains(String substring) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring не может быть null");
        }
        String normalized = substring.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Substring не может быть пустым");
        }
        return user -> user.username().toLowerCase().contains(normalized);
    }

    public static UserFilter byEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email не может быть null");
        }
        String normalized = email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        return user -> normalized.equals(user.email().toLowerCase());
    }

    public static UserFilter byEmailDomain(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain не может быть null");
        }
        String normalized = domain.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Domain не может быть пустым");
        }
        String domainWithAt = normalized.startsWith("@") ? normalized : "@" + normalized;
        return user -> user.email().toLowerCase().endsWith(domainWithAt);
    }

    public static UserFilter byFullNameContains(String substring) {
        if (substring == null) {
            throw new IllegalArgumentException("Substring не может быть null");
        }
        String normalized = substring.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Substring не может быть пустым");
        }
        return user -> user.fullName().toLowerCase().contains(normalized);
    }
}