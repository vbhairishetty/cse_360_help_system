package Testing;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import System.SessionManager;
import database.DatabaseHandler;
import model.HelpArticle;
import model.Role;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ArticleManagementTest {
    private DatabaseHandler dbHandler;

    @BeforeEach
    public void setup() throws Exception {
        dbHandler = DatabaseHandler.getInstance();
        dbHandler.createUsersTable();
        dbHandler.createHelpArticlesTable();

        // Create admin user if not already present
        if (dbHandler.getUser("admin1") == null) {
            dbHandler.createUserTesting("admin1", "adminPassword", List.of(Role.ADMIN));
        }

        // Create instructor user if not already present
        if (dbHandler.getUser("instructor1") == null) {
            dbHandler.createUserTesting("instructor1", "instructorPassword", List.of(Role.INSTRUCTOR));
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        clearHelpArticlesTable();
    }
    
    public void clearHelpArticlesTable() throws SQLException {
    	Connection connection = DatabaseHandler.getInstance().getConnection();
        connection.setAutoCommit(false); // Begin transaction
        try {
            // Delete from dependent tables first
            String deleteRolePermissionsQuery = "DELETE FROM article_role_permissions";
            try (PreparedStatement stmt = connection.prepareStatement(deleteRolePermissionsQuery)) {
                stmt.executeUpdate();
            }

            String deleteGroupLinksQuery = "DELETE FROM help_article_groups";
            try (PreparedStatement stmt = connection.prepareStatement(deleteGroupLinksQuery)) {
                stmt.executeUpdate();
            }

            // Delete from main help_articles table
            String deleteHelpArticlesQuery = "DELETE FROM help_articles";
            try (PreparedStatement stmt = connection.prepareStatement(deleteHelpArticlesQuery)) {
                stmt.executeUpdate();
            }

            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            connection.rollback(); // Rollback transaction on error
            throw e;
        } finally {
            connection.setAutoCommit(true); // Reset auto-commit
        }
    }



    @Test
    public void testCreateAndAccessArticle() throws Exception {
        // Log in as admin
        String adminLoginResult = dbHandler.loginUser("admin1", "admin");
        assertEquals("LOGIN_SUCCESS", adminLoginResult, "Admin login should be successful.");

        // Create a new article
        HelpArticle article = new HelpArticle();
        article.setId(UUID.randomUUID().toString()); // Generate a unique ID for the article
        article.setHeader("Admin Header");
        article.setTitle("Admin Created Article - " + UUID.randomUUID()); // Ensure unique title
        article.setShortDescription("Article created by admin.");
        article.setKeywords("admin, test, article");
        article.setBody("This is a test article created by admin.");
        article.setLevel("Beginner");
        article.setAccessLevel("PUBLIC");
        article.setAllowedRoles(Set.of(Role.ADMIN, Role.INSTRUCTOR, Role.STUDENT));

        // Add the article to the database
        dbHandler.addHelpArticle(article, "admin1");

        // Verify that the article was added
        HelpArticle retrievedArticleAsAdmin = dbHandler.getHelpArticle(article.getId(), "admin1", Role.ADMIN);
        assertNotNull(retrievedArticleAsAdmin, "Retrieved article as admin should not be null.");
        assertEquals("Admin Header", retrievedArticleAsAdmin.getHeader(), "Article header should match.");
        assertTrue(retrievedArticleAsAdmin.getTitle().startsWith("Admin Created Article"), "Article title should match.");

        // Log in as instructor
        String instructorLoginResult = dbHandler.loginUser("instructor1", "instructorPassword");
        assertEquals("LOGIN_SUCCESS", instructorLoginResult, "Instructor login should be successful.");
        User instructorUser = new User("instructor1", "instructorPassword".toCharArray());
        instructorUser.addRole(Role.INSTRUCTOR);
        SessionManager.login(instructorUser);

        // Verify that the instructor can access the article
        HelpArticle retrievedArticleAsInstructor = dbHandler.getHelpArticle(article.getId(), "instructor1", Role.INSTRUCTOR);
        assertNotNull(retrievedArticleAsInstructor, "Retrieved article as instructor should not be null.");
        assertEquals("Admin Header", retrievedArticleAsInstructor.getHeader(), "Article header should match.");
        assertTrue(retrievedArticleAsInstructor.getTitle().startsWith("Admin Created Article"), "Article title should match.");
    }
}
