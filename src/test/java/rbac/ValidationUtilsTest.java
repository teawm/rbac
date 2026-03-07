package rbac;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

    @Test
    void testValidUsername() {
        assertTrue(ValidationUtils.isValidUsername("stas"), "Валидное имя должно проходить проверку");
        assertTrue(ValidationUtils.isValidUsername("john_doe"), "Имя с подчёркиванием должно проходить");
        assertTrue(ValidationUtils.isValidUsername("user123"), "Имя с цифрами должно проходить");
    }

    @Test
    void testInvalidUsernameTooShort() {
        assertFalse(ValidationUtils.isValidUsername("an"), "Слишком короткое имя не должно проходить");
    }

    @Test
    void testInvalidUsernameTooLong() {
        assertFalse(ValidationUtils.isValidUsername("verylongusername12345"), "Слишком длинное имя не должно проходить");
    }

    @Test
    void testInvalidUsernameSpecialChars() {
        assertFalse(ValidationUtils.isValidUsername("stas@name"), "Имя с недопустимыми символами не должно проходить");
        assertFalse(ValidationUtils.isValidUsername("user-name"), "Имя с дефисом не должно проходить");
        assertFalse(ValidationUtils.isValidUsername("user name"), "Имя с пробелом не должно проходить");
    }

    @Test
    void testValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("stas@example.com"), "Валидный email должен проходить проверку");
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.co.uk"), "Email с точками должен проходить");
    }

    @Test
    void testInvalidEmail() {
        assertFalse(ValidationUtils.isValidEmail("name@com"), "Невалидный email не должен проходить");
        assertFalse(ValidationUtils.isValidEmail("user@domain"), "Email без точки не должен проходить");
        assertFalse(ValidationUtils.isValidEmail("@domain.com"), "Email без имени не должен проходить");
    }

    @Test
    void testValidDate() {
        assertTrue(ValidationUtils.isValidDate("2026-02-15"), "Валидная дата должна проходить проверку");
        assertTrue(ValidationUtils.isValidDate("2026-02-15 19:45"), "Валидная дата с временем должна проходить");
    }

    @Test
    void testInvalidDate() {
        assertFalse(ValidationUtils.isValidDate("15-02-2026"), "Неверный формат даты не должен проходить");
        assertFalse(ValidationUtils.isValidDate("2026/02/15"), "Неверный формат даты не должен проходить");
    }

    @Test
    void testNormalizeString() {
        String input = "  stas   makarov  ";
        String normalized = ValidationUtils.normalizeString(input);
        assertEquals("stas makarov", normalized, "Строка должна быть нормализована");
        assertEquals(12, normalized.length(), "Длина нормализованной строки должна быть 12");
    }

    @Test
    void testRequireNonEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("", "username");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("   ", "username");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty(null, "username");
        });
    }

    @Test
    void testValidateUsername() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validateUsername("stas", "username");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.validateUsername("an", "username");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.validateUsername("stas@name", "username");
        });
    }

    @Test
    void testValidateEmail() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validateEmail("stas@example.com", "email");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.validateEmail("name@com", "email");
        });
    }

    @Test
    void testValidateDate() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validateDate("2026-02-15", "date");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.validateDate("15-02-2026", "date");
        });
    }

    @Test
    void testNullUsername() {
        assertFalse(ValidationUtils.isValidUsername(null), "Null не должен проходить проверку");
    }

    @Test
    void testNullEmail() {
        assertFalse(ValidationUtils.isValidEmail(null), "Null не должен проходить проверку");
    }

    @Test
    void testNullDate() {
        assertFalse(ValidationUtils.isValidDate(null), "Null не должен проходить проверку");
    }
}