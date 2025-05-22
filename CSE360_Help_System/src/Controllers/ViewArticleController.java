package Controllers;

import java.sql.SQLException;

import database.DatabaseHandler;
import database.EncryptionUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.HelpArticle;
import model.Role;

public class ViewArticleController {

    private DatabaseHandler dbHandler;

    @FXML
    private Label articleTitle;

    @FXML
    private Label shortDescription;

    @FXML
    private Label articleBody;

    @FXML
    private Label keywords;

    public void setDatabaseHandler(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void initializeArticle(String articleId, String currentUserName, Role currentUserRole) {
    	
    	if(dbHandler == null) {try {
			dbHandler = new DatabaseHandler();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Cannot initialize DatabaseHandler");
			e.printStackTrace();
		} }
    	
        try {
            // Step 1: Retrieve the article from the database
            HelpArticle article = dbHandler.getHelpArticle(articleId, currentUserName, currentUserRole);

            // Step 2: Verify if the user has the appropriate access level
            if (article == null) {
                showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have permission to view this article.");
                return;
            }

            // Step 3: Populate the article details in the UI
            articleTitle.setText(article.getTitle());
            shortDescription.setText(article.getShortDescription());

            // Decrypt the article body if it is encrypted
            String bodyContent;
            if (article.isSpecialAccess() && article.getEncryptedBody() != null) {
                try {
                    bodyContent = EncryptionUtility.Decrypt(article.getEncryptedBody(), "testEncryptionKe"); // Replace with your key securely
                } catch (Exception e) {
                    bodyContent = "Error decrypting article content.";
                    e.printStackTrace();
                }
            } else {
                bodyContent = article.getBody();
            }
            articleBody.setText(bodyContent);

            // Set the keywords
            keywords.setText(article.getKeywords());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading Article", "Error loading article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClose() {
        // Close the current window
        Stage stage = (Stage) articleTitle.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public HelpArticle viewArticle(String articleId, String currentUserName, Role currentUserRole) throws SQLException {
        // Step 1: Retrieve the article from the database
        HelpArticle article = dbHandler.getHelpArticle(articleId, currentUserName, currentUserRole);

        // Step 2: Verify if the user has the appropriate access level
        if (article == null) {
            return null; // Article not found in the database
        }

        // Access verification is already handled by `getHelpArticle` in DatabaseHandler
        return article;
    }

    private boolean checkUserAccess(String currentUserName, Role userRole, HelpArticle article) throws SQLException {
        if (article.getAccessLevel().equalsIgnoreCase("PUBLIC")) {
            return true; // Public articles are accessible by everyone
        }

        // Check allowed roles for restricted/confidential access
        if (article.getAllowedRoles() != null && article.getAllowedRoles().contains(userRole)) {
            return true; // The user's role is allowed to access this article
        }

        // If special access is required, check if the user has permission
        if (article.isSpecialAccess()) {
            return dbHandler.hasSpecialAccessPermission(currentUserName, article.getSpecialAccessGroupId());
        }

        return false; // Default case: no access
    }

}
