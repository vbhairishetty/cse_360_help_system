package Testing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions; //JUnit 5
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.DatabaseHandler;
import database.EncryptionUtility;
import model.HelpArticle;
import model.Role;

public class ArticleEncryptionAndDecryptionTesting {
	private static final String ENCRYPTION_KEY = "testEncryptionKe"; // 16 bytes
    private Connection connection;

    @BeforeEach
    public void setUp() throws SQLException {
        // Assuming DatabaseHandler.getInstance() provides a singleton instance of the connection
        connection = DatabaseHandler.getInstance().getConnection();
//        clearHelpArticlesTable(); // Clear table before each test for isolation
    }

    @Test
    public void testEncryptionDecryptionOfArticleBody() throws SQLException {
        // Create a new HelpArticle
        String id = UUID.randomUUID().toString();
        String header = "Test Header 18";
        String level = "Beginner";
        String title = "Test Article";
        String shortDescription = "This is a test article for encryption testing.";
        String keywords = "test, encryption";
        String body = "This is the body content of the article to be encrypted.";
        String encryptedBody;
		try {
			encryptedBody = EncryptionUtility.Encrypt(body, ENCRYPTION_KEY);
			
			System.out.println(encryptedBody);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			encryptedBody ="";
			e.printStackTrace();
		}
        String links = "https://example.com";
        String sensitiveTitle = "Sensitive Info";
        String sensitiveDescription = "Sensitive Description";
        List<String> groups = List.of("Articles", "Account");
        String accessLevel = "PUBLIC";
        boolean isSpecialAccess = true;
        long specialAccessGroupId = 1L;
        String createdBy = "admin";
        Set<Role> allowedRoles = new HashSet<>();
        allowedRoles.add(Role.ADMIN);
        allowedRoles.add(Role.INSTRUCTOR);

        HelpArticle testArticle = new HelpArticle(
                id, header, level, title, shortDescription, keywords, body,
                encryptedBody, links, sensitiveTitle, sensitiveDescription,
                groups, accessLevel, isSpecialAccess, specialAccessGroupId,
                createdBy, allowedRoles
        );

        // Add the article to the database
        addHelpArticleToDatabase(testArticle);

        // Retrieve the article from the database
        HelpArticle retrievedArticle = getHelpArticleFromDatabase(id);

        // Decrypt the body
        String decryptedBody;
		try {
			decryptedBody = EncryptionUtility.Decrypt(retrievedArticle.getEncryptedBody(), ENCRYPTION_KEY);
			System.out.print(decryptedBody);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			decryptedBody = "";
			e.printStackTrace();
		}

        // Assert that the decrypted body matches the original body
        Assertions.assertEquals(body, decryptedBody, "Decrypted body does not match the original body");
    }

    private void clearHelpArticlesTable() throws SQLException {
        String deleteSQL = "DELETE FROM help_articles";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
            stmt.executeUpdate();
        }
    }

    private void addHelpArticleToDatabase(HelpArticle article) throws SQLException {
        String insertSQL = "INSERT INTO help_articles (id, header, article_level, title, short_description, keywords, body, " +
                "encrypted_body, links, sensitive_title, sensitive_description, access_level, is_special_access, special_access_group_id, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setString(1, article.getId());
            stmt.setString(2, article.getHeader());
            stmt.setString(3, article.getLevel());
            stmt.setString(4, article.getTitle());
            stmt.setString(5, article.getShortDescription());
            stmt.setString(6, article.getKeywords());
            stmt.setString(7, article.getBody());
            stmt.setString(8, article.getEncryptedBody());
            stmt.setString(9, article.getLinks());
            stmt.setString(10, article.getSensitiveTitle());
            stmt.setString(11, article.getSensitiveDescription());
            stmt.setString(12, article.getAccessLevel());
            stmt.setBoolean(13, article.isSpecialAccess());
            stmt.setLong(14, article.getSpecialAccessGroupId());
            stmt.setString(15, article.getCreatedBy());
            stmt.executeUpdate();
        }
    }

    private HelpArticle getHelpArticleFromDatabase(String articleId) throws SQLException {
        String selectSQL = "SELECT * FROM help_articles WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectSQL)) {
            stmt.setString(1, articleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String header = rs.getString("header");
                    String level = rs.getString("article_level");
                    String title = rs.getString("title");
                    String shortDescription = rs.getString("short_description");
                    String keywords = rs.getString("keywords");
                    String body = rs.getString("body");
                    String encryptedBody = rs.getString("encrypted_body");
                    String links = rs.getString("links");
                    String sensitiveTitle = rs.getString("sensitive_title");
                    String sensitiveDescription = rs.getString("sensitive_description");
                    String accessLevel = rs.getString("access_level");
                    boolean isSpecialAccess = rs.getBoolean("is_special_access");
                    long specialAccessGroupId = rs.getLong("special_access_group_id");
                    String createdBy = rs.getString("created_by");

                    return new HelpArticle(
                            id, header, level, title, shortDescription, keywords, body,
                            encryptedBody, links, sensitiveTitle, sensitiveDescription,
                            List.of(), accessLevel, isSpecialAccess, specialAccessGroupId,
                            createdBy, new HashSet<>()
                    );
                }
            }
        }
        throw new SQLException("Article not found with ID: " + articleId);
    }
}
