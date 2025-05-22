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

public class EditArticleController {

    @FXML
    private TextField headerField;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea shortDescField;

    @FXML
    private TextField keywordsField;

    @FXML
    private TextArea bodyField;

    @FXML
    private TextField linksField;

    @FXML
    private TextField sensitiveTitleField;

    @FXML
    private TextField sensitiveDescField;

    @FXML
    private ChoiceBox<String> levelChoiceBox;

    @FXML
    private ChoiceBox<String> accessLevelChoiceBox;

    @FXML
    private CheckBox specialAccessCheckBox;

    @FXML
    private CheckBox adminCheckBox;

    @FXML
    private CheckBox instructorCheckBox;

    @FXML
    private CheckBox studentCheckBox;

    @FXML
    private Button backBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private ChoiceBox<String> groupChoiceBox;

    private DatabaseHandler dbHandler;

    private SceneManager manager;

    private HelpArticle selectedArticle;

    private static final String ENCRYPTION_KEY = "testEncryptionKe";

    @FXML
    public void initialize() {
        dbHandler = Singleton.getInstance().getDbHandler();
        manager = Singleton.getInstance().getManager();

        levelChoiceBox.getItems().addAll("beginner", "intermediate", "advanced", "expert");
        accessLevelChoiceBox.getItems().addAll("PUBLIC", "RESTRICTED", "PRIVATE");

        try {
            groupChoiceBox.getItems().addAll(dbHandler.getAllGroupNames());
        } catch (SQLException e) {
            showAlert("Error loading groups: " + e.getMessage());
        }
    }

    public void initializeArticle(HelpArticle article) {
        this.selectedArticle = article;

        // Populate fields with the selected article's data
        headerField.setText(selectedArticle.getHeader());
        titleField.setText(selectedArticle.getTitle());
        shortDescField.setText(selectedArticle.getShortDescription());
        keywordsField.setText(selectedArticle.getKeywords());
        try {
            if (selectedArticle.getEncryptedBody() != null) {
                String decryptedBody = EncryptionUtility.Decrypt(selectedArticle.getEncryptedBody(), ENCRYPTION_KEY);
                bodyField.setText(decryptedBody);
                System.out.println("Decrypted body: " + decryptedBody);
            } else {
                bodyField.setText(selectedArticle.getBody());
                System.out.println("Body: " + selectedArticle.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        linksField.setText(selectedArticle.getLinks());
        sensitiveTitleField.setText(selectedArticle.getSensitiveTitle());
        sensitiveDescField.setText(selectedArticle.getSensitiveDescription());
        levelChoiceBox.setValue(selectedArticle.getLevel());
        accessLevelChoiceBox.setValue(selectedArticle.getAccessLevel());
        
        if (selectedArticle.getGroups() == null) {
            selectedArticle.setGroups(new ArrayList<>()); // Initialize with a new modifiable list
        }

        // Populate role-based visibility
        if (selectedArticle.getAllowedRoles() != null) {
            adminCheckBox.setSelected(selectedArticle.getAllowedRoles().contains(Role.ADMIN));
            instructorCheckBox.setSelected(selectedArticle.getAllowedRoles().contains(Role.INSTRUCTOR));
            studentCheckBox.setSelected(selectedArticle.getAllowedRoles().contains(Role.STUDENT));
        }

        specialAccessCheckBox.setSelected(selectedArticle.isSpecialAccess());
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
                    showAlert("Invalid role. Unable to redirect to dashboard.");
                    break;
            }
        } else {
            showAlert("No role assigned. Please log in again.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleSaveArticle() {
        System.out.println("Saving Article...");

        boolean isNewArticle = (selectedArticle == null);
        if (isNewArticle) {
            selectedArticle = new HelpArticle();
            System.out.println("Creating a new article...");
        } else {
            System.out.println("Editing Existing Article ID: " + selectedArticle.getId());
        }

        // Get the updated data from the fields
        String articleHeader = headerField.getText();
        String articleTitle = titleField.getText();
        String articleShortDesc = shortDescField.getText();
        String articleKeywords = keywordsField.getText();
        String articleBody = bodyField.getText();
        String articleLinks = linksField.getText();
        String articleSensitiveTitle = sensitiveTitleField.getText();
        String articleSensitiveDesc = sensitiveDescField.getText();
        String articleLevel = levelChoiceBox.getValue();
        String accessLevel = accessLevelChoiceBox.getValue();
        boolean isSpecialAccess = specialAccessCheckBox.isSelected();

        // Validate mandatory fields
        if (articleHeader.isEmpty() || articleTitle.isEmpty() || articleShortDesc.isEmpty() || articleLevel == null || accessLevel == null) {
            showAlert("Please fill in all mandatory fields: Header, Title, Short Description, Level, and Access Level.");
            return;
        }

        // Update the selected article object with new values
        selectedArticle.setHeader(articleHeader);
        selectedArticle.setTitle(articleTitle);
        selectedArticle.setShortDescription(articleShortDesc);
        selectedArticle.setKeywords(articleKeywords);
        selectedArticle.setBody(articleBody);
        selectedArticle.setLinks(articleLinks);
        selectedArticle.setSensitiveTitle(articleSensitiveTitle);
        selectedArticle.setSensitiveDescription(articleSensitiveDesc);
        selectedArticle.setLevel(articleLevel);
        selectedArticle.setAccessLevel(accessLevel);
        selectedArticle.setSpecialAccess(isSpecialAccess);

        // Do not touch the groups_col field
        // No updates to groups_col or groups list here

        // Role-based visibility handling
        Set<Role> allowedRoles = new HashSet<>();
        if (adminCheckBox.isSelected()) allowedRoles.add(Role.ADMIN);
        if (instructorCheckBox.isSelected()) allowedRoles.add(Role.INSTRUCTOR);
        if (studentCheckBox.isSelected()) {
            allowedRoles.add(Role.STUDENT);
            allowedRoles.add(Role.PAST_STUDENT);
            allowedRoles.add(Role.TA);
        }
        selectedArticle.setAllowedRoles(allowedRoles);

        Role currentUserRole = SessionManager.getCurrentUserRole();

        try {
            if (isNewArticle) {
                dbHandler.addHelpArticle(selectedArticle, SessionManager.getCurrentUser().getUsername());
                showAlert("New article created successfully!");
            } else {
                dbHandler.updateHelpArticle(selectedArticle, SessionManager.getCurrentUser().getUsername(), currentUserRole);
                showAlert("Article updated successfully!");
            }
            handleBackToDashboard();
        } catch (SQLException e) {
            showAlert("Error saving article: " + e.getMessage());
        }
    }



    @FXML
    public void handleAssignToGroup() {
        String selectedGroup = groupChoiceBox.getValue();
        if (selectedGroup != null && !selectedGroup.isEmpty()) {
            try {
                dbHandler.assignArticleToGroup(selectedArticle.getId(), selectedGroup, SessionManager.getCurrentUser().getUsername());
                showAlert("Group successfully assigned to the article.");
            } catch (SQLException e) {
                showAlert("Error assigning group to the article: " + e.getMessage());
            }
        } else {
            showAlert("Please select a group to assign to the article.");
        }
    }

    @FXML
    public void handleRemoveFromGroup() {
        String selectedGroup = groupChoiceBox.getValue();
        if (selectedGroup != null && !selectedGroup.isEmpty()) {
            try {
                dbHandler.removeArticleFromGroup(selectedArticle.getId(), selectedGroup, SessionManager.getCurrentUser().getUsername());
                showAlert("Group successfully removed from the article.");
            } catch (SQLException e) {
                showAlert("Error removing group from the article: " + e.getMessage());
            }
        } else {
            showAlert("Please select a group to remove from the article.");
        }
    }
}
