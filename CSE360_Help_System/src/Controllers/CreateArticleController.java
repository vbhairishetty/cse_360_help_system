package Controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import System.SceneManager;
import System.SessionManager;
import System.Singleton;
import database.DatabaseHandler;
import database.EncryptionUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.HelpArticle;
import model.Role;

public class CreateArticleController {

    @FXML
    private TextField header;

    @FXML
    private TextField title;

    @FXML
    private TextArea shortDesc;

    @FXML
    private TextField keywords;

    @FXML
    private TextArea body;

    @FXML
    private TextField links;

    @FXML
    private TextField sensitiveTitle;

    @FXML
    private TextField sensitiveDesc;
    
    @FXML
    private TextField newGroup;
    
    @FXML
    private ChoiceBox<String> existingGroups; 

    @FXML
    private ChoiceBox<String> articleLevelChoiceBox;
    
    @FXML
    private ChoiceBox<String> accessLevelChoiceBox;

    @FXML
    private CheckBox adminAccessCheckBox;

    @FXML
    private CheckBox instructorAccessCheckBox;

    @FXML
    private CheckBox studentAccessCheckBox;

    @FXML
    private CheckBox specialAccessCheckBox;
    
    @FXML
    private Button backBtn;
    
    @FXML
    private Button createBtn;    

    private DatabaseHandler databaseHandler;

    private SceneManager manager;
    
    private static final String ENCRYPTION_KEY = "testEncryptionKe";

    @FXML
    public void initialize() {
        try {
            databaseHandler = new DatabaseHandler();
            manager = Singleton.getInstance().getManager();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        articleLevelChoiceBox.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
        accessLevelChoiceBox.getItems().addAll("PUBLIC", "RESTRICTED", "CONFIDENTIAL");
        try {
            existingGroups.getItems().addAll(databaseHandler.getAllGroupNames()); // Method to get group names from database
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading groups: " + e.getMessage());
        }
    }

    @FXML
    public void handleCreateArticle() {
        String articleHeader = header.getText();
        String articleTitle = title.getText();
        String articleShortDesc = shortDesc.getText();
        String articleKeywords = keywords.getText();
        String articleBody = body.getText();
        String articleLinks = links.getText();
        String articleSensitiveTitle = sensitiveTitle.getText();
        String articleSensitiveDesc = sensitiveDesc.getText();
        String articleLevel = articleLevelChoiceBox.getValue();
        String accessLevel = accessLevelChoiceBox.getValue();

        if (articleTitle.isEmpty() || articleBody.isEmpty() || articleLevel == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }
        
        // Create or assign groups
        List<String> groups = new ArrayList<>();
        String newGroupName = newGroup.getText().trim();
        if (!newGroupName.isEmpty()) {
            groups.add(newGroupName);
        }

        String selectedGroup = existingGroups.getValue();
        if (selectedGroup != null && !selectedGroup.isEmpty()) {
            groups.add(selectedGroup);
        }

        boolean isSpecialAccess = specialAccessCheckBox.isSelected();
        String encryptedBody = null;

        if (isSpecialAccess) {
        	try {
        	    if (articleBody == null || articleBody.isEmpty()) {
        	        throw new IllegalArgumentException("Article body cannot be null or empty for encryption.");
        	    }

        	    String encryptedContent = EncryptionUtility.Encrypt(articleBody, ENCRYPTION_KEY);
        	    System.out.println("Encrypted Content: " + encryptedContent);
        	    
        	    String decryptedContent = EncryptionUtility.Decrypt(encryptedContent, ENCRYPTION_KEY);
        	    System.out.println("Decrypted Content: " + decryptedContent);
        	    encryptedBody = encryptedContent;
        	} catch (IllegalArgumentException e) {
        	    showAlert(Alert.AlertType.ERROR, "Encryption Error", "Invalid input: " + e.getMessage());
        	    return;
        	} catch (Exception e) {
        	    e.printStackTrace(); // Add stack trace for more detailed debugging information.
        	    showAlert(Alert.AlertType.ERROR, "Encryption Error", "Failed to encrypt the article body: " + e.getMessage());
        	    return;
        	}
        }

        // Determine allowed roles
        Set<Role> allowedRoles = new HashSet<>();
        if (adminAccessCheckBox.isSelected()) {
            allowedRoles.add(Role.ADMIN);
        }
        if (instructorAccessCheckBox.isSelected()) {
            allowedRoles.add(Role.INSTRUCTOR);
        }
        if (studentAccessCheckBox.isSelected()) {
            allowedRoles.add(Role.STUDENT);
            allowedRoles.add(Role.TA);
            allowedRoles.add(Role.PAST_STUDENT);
        }

        HelpArticle article = new HelpArticle();
        article.setHeader(articleHeader);
        article.setTitle(articleTitle);
        article.setShortDescription(articleShortDesc);
        article.setKeywords(articleKeywords);
        article.setBody(isSpecialAccess ? "" : articleBody);
        article.setEncryptedBody(encryptedBody);
        article.setLinks(articleLinks);
        article.setSensitiveTitle(articleSensitiveTitle);
        article.setSensitiveDescription(articleSensitiveDesc);
        article.setLevel(articleLevel);
        article.setSpecialAccess(isSpecialAccess);
        article.setAccessLevel(accessLevel);
        article.setAllowedRoles(allowedRoles);
        article.setGroups(groups);

        try {
            // Save the article to the database
            String createdBy = SessionManager.getCurrentUser().getUsername();
            databaseHandler.addHelpArticle(article, createdBy);

            if (isSpecialAccess) {
                // Handle special access rights
                long groupId = databaseHandler.createSpecialAccessGroup(articleTitle + " Special Access Group", createdBy);

                // Add the creator as the admin of this special access group
                databaseHandler.addSpecialAccessAdmin(groupId, createdBy);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Article created successfully.");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create the article.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackToDashboard() {
        Role currentRole = SessionManager.getCurrentUserRole();
        if (currentRole != null) {
            switch (currentRole) {
                case ADMIN:
                    manager.switchScene("/Views/adminDash.fxml");
                    break;
                case STUDENT:
                    manager.switchScene("/Views/studentDashboard.fxml");
                    break;
                case INSTRUCTOR:
                    manager.switchScene("/Views/instructorDashboard.fxml");
                    break;
                case TA:
                    manager.switchScene("/Views/taDashboard.fxml");
                    break;
                case PAST_STUDENT:
                    manager.switchScene("/Views/exStudentDashboard.fxml");
                    break;
                default:
                    showAlert(Alert.AlertType.INFORMATION, "Try again", "Invalid role. Unable to redirect to dashboard.");
                    break;
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Try again", "No role assigned. Please log in again.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
