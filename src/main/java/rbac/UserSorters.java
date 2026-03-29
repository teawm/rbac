package main.java.rbac;

import java.util.Comparator;

public class UserSorters {

    public static Comparator<User> byUsername() {
        return Comparator.comparing(User::username);
    }

    public static Comparator<User> byFullName() {
        return Comparator.comparing(User::fullName);
    }

    public static Comparator<User> byEmail() {
        return Comparator.comparing(User::email);
    }

    public static Comparator<User> byUsernameLength() {
        return Comparator.comparing(user -> user.username().length());
    }
}