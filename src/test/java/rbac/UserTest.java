package rbac;
    
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testValidUser() {
        User user = User.validate(" stas_ ", " Stas Makarov ", "stas_email@exam.ple");
        assertEquals("stas_", user.username());
        assertEquals("Stas Makarov", user.fullName());
        assertEquals("stas_email@exam.ple", user.email());
    }

    @Test
    void testInvalidUsernameTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            User.validate("ab", "Incorrect User", "test@example.com");
        });
    }
}