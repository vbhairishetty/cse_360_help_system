package Testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.DatabaseHandler;
import model.HelpArticle;
import model.Role;

import java.util.List;
import java.util.Set;

public class SearchArticlesTest {
    private DatabaseHandler dbHandler;

    @BeforeEach
    public void setup() throws Exception {
        dbHandler = DatabaseHandler.getInstance();
        dbHandler.createUsersTable();
        dbHandler.createHelpArticlesTable();

        // Set up test users and articles
        if (dbHandler.getUser("User1") == null) {
            dbHandler.createUserTesting("User1", "password", List.of(Role.STUDENT));
        }

        if (dbHandler.getUser("admin1") == null) {
            dbHandler.createUserTesting("admin1", "adminPassword", List.of(Role.ADMIN));
        }

        // Add test articles
        if (dbHandler.listAllArticles("admin1", Role.ADMIN).isEmpty()) {
            HelpArticle article1 = new HelpArticle();
            article1.setId("article1");
            article1.setHeader("Introduction to Java");
            article1.setTitle("Learn Java Basics");
            article1.setShortDescription("A beginner's guide to Java.");
            article1.setKeywords("java, programming, beginner");
            article1.setBody("Java is a versatile programming language...");
            article1.setAccessLevel("PUBLIC");
            article1.setAllowedRoles(Set.of(Role.ADMIN, Role.INSTRUCTOR, Role.STUDENT));
            dbHandler.addHelpArticle(article1, "admin1");

            HelpArticle article2 = new HelpArticle();
            article2.setId("article2");
            article2.setHeader("Advanced Java");
            article2.setTitle("Java for Professionals");
            article2.setShortDescription("An advanced guide to Java programming.");
            article2.setKeywords("java, programming, advanced");
            article2.setBody("Advanced Java topics include...");
            article2.setAccessLevel("RESTRICTED");
            article2.setAllowedRoles(Set.of(Role.ADMIN, Role.INSTRUCTOR));
            dbHandler.addHelpArticle(article2, "admin1");
        }
    }

    
    @Test
    public void testSearchArticlesAsStudent() throws Exception {
        // Log in as a student
        String loginResult = dbHandler.loginUser("User1", "password");
        assertEquals("LOGIN_SUCCESS", loginResult);

        // Perform the search
        List<HelpArticle> searchResults = dbHandler.searchHelpArticles("test", null, null, "User1", Role.STUDENT);

        // Assertions
        assertNotNull(searchResults, "Search results should not be null.");
        assertFalse(searchResults.isEmpty(), "Search results should not be empty.");
        assertTrue(searchResults.stream().allMatch(article -> article.getAccessLevel().equals("PUBLIC")),
                   "Search results should only contain PUBLIC articles.");
        
        // Verify restricted articles are not visible
        assertFalse(searchResults.stream().anyMatch(article -> article.getAccessLevel().equals("RESTRICTED")),
                    "Restricted articles should not be visible to students.");
    }

}
