package Testing;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Controllers.ViewArticleController;
import database.DatabaseHandler;
import model.HelpArticle;
import model.Role;

public class ViewArticleTest {
	private DatabaseHandler dbHandler;
	private ViewArticleController controller;

	@BeforeEach
	public void setup() throws SQLException {
		// Setting up a database connection and initializing the controller
		dbHandler = new DatabaseHandler(); // Assuming DatabaseHandler sets up the connection
		controller = new ViewArticleController();
		controller.setDatabaseHandler(dbHandler);
		//        clearHelpArticlesTable(); // Clear the table to ensure test isolation
	}

	@Test
	public void testViewPublicArticle_Success() throws SQLException {
		// Create a public article
		String articleId = UUID.randomUUID().toString();
		HelpArticle article = new HelpArticle(
				articleId,
				"Test Header - View Article 12",
				"Beginner",
				"Test Article",
				"This is a test article for viewing.",
				"test, view",
				"This is the body content of the article.",
				null,
				"https://example.com",
				"Sensitive Info",
				"Sensitive Description",
				List.of("group1"),
				"PUBLIC",
				false,
				1L,
				"testUser",
				new HashSet<>(Set.of(Role.ADMIN, Role.INSTRUCTOR))
				);

		// Add the article to the database
		dbHandler.addHelpArticle(article, "admin");

		// View the article
		HelpArticle viewedArticle = controller.viewArticle(articleId, "admin", Role.ADMIN);

		// Assertions
		assert viewedArticle != null;
		assert viewedArticle.getId().equals(articleId);
		assert viewedArticle.getTitle().equals("Test Article");
		assert viewedArticle.getBody().equals("This is the body content of the article.");
	}

	@Test
	public void testViewRestrictedArticle_NoAccess() throws SQLException {
	    // Create a restricted article
	    String articleId = UUID.randomUUID().toString();
	    HelpArticle article = new HelpArticle(
	            articleId,
	            "Restricted Header 12",
	            "Intermediate",
	            "Restricted Article",
	            "This is a restricted article for viewing.",
	            "restricted, view",
	            "This is the body content of the restricted article.",
	            null,
	            "https://example.com",
	            "Sensitive Info",
	            "Sensitive Description",
	            List.of("group1"),
	            "RESTRICTED",
	            false,
	            1L,
	            "testUser",
	            new HashSet<>(Set.of(Role.ADMIN))
	            );

	    // Add the article to the database
	    dbHandler.addHelpArticle(article, "admin");

	    // Attempt to view the article with no access
	    HelpArticle viewedArticle = null;
	    try {
	        viewedArticle = controller.viewArticle(articleId, "admin", Role.STUDENT);
	        assert false : "Access should have been denied but wasn't.";
	    } catch (SQLException e) {
	        // Access should be denied, thus an exception should be thrown
	        assert e.getMessage().contains("Access denied");
	    }

	    // Assertions
	    assert viewedArticle == null;
	}


	@Test
	public void testViewRestrictedArticle_WithAccess() throws SQLException {
		// Create a restricted article
		String articleId = UUID.randomUUID().toString();
		HelpArticle article = new HelpArticle(
				articleId,
				"Restricted Header 13",
				"Intermediate",
				"Restricted Article",
				"This is a restricted article for viewing.",
				"restricted, view",
				"This is the body content of the restricted article.",
				null,
				"https://example.com",
				"Sensitive Info",
				"Sensitive Description",
				List.of("group1"),
				"RESTRICTED",
				false,
				1L,
				"testUser",
				new HashSet<>(Set.of(Role.ADMIN, Role.INSTRUCTOR))
				);

		// Add the article to the database
		dbHandler.addHelpArticle(article, "admin");

		// View the article with access
		HelpArticle viewedArticle = controller.viewArticle(articleId, "admin", Role.ADMIN);

		// Assertions
		assert viewedArticle != null;
		assert viewedArticle.getId().equals(articleId);
		assert viewedArticle.getTitle().equals("Restricted Article");
		assert viewedArticle.getBody().equals("This is the body content of the restricted article.");
	}

	private void clearHelpArticlesTable() throws SQLException {
		String deleteSQL = "DELETE FROM help_articles";
		try (var stmt = dbHandler.getConnection().prepareStatement(deleteSQL)) {
			stmt.executeUpdate();
		}
	}
}
