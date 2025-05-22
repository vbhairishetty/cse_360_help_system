package Testing;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import database.DatabaseHandler;
import model.Role;
import model.User;

public class LoginTest {
    private DatabaseHandler db;

    @BeforeEach
    public void setup() throws Exception {
        db = DatabaseHandler.getInstance();
        db.createUsersTable();
     // Only create admin user if it doesn't exist
        if (db.getUser("admin1") == null) {
            User newUser = new User();
            newUser.setUserName("admin1");
            newUser.setEmail("admin1@example.com");
            newUser.setFirstName("Admin");
            newUser.setLastName("User");
            newUser.setPassword("admin".toCharArray());
            db.addAdminUser(newUser);
        }
        if (db.getUser("student1") == null) {
            db.createUserTesting("student1", "invitationCode", List.of(Role.STUDENT));		// Only create "student1" if it doesn't exist
        }
    }

    @Test
    public void testValidLogin() throws Exception {
        String result = db.loginUser("admin1", "admin");
        assertEquals("LOGIN_SUCCESS", result);
    }

    @Test
    public void testInvalidLogin() throws Exception {
        String result = db.loginUser("admin1", "wrongPassword");
        assertEquals("INCORRECT_PASSWORD", result);
    }

    @Test
    public void testNonExistentUser() throws Exception {
        String result = db.loginUser("nonexistent", "password");
        assertEquals("NOT_FOUND", result);
    }
}
