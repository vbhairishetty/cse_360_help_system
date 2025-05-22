package Controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import System.SceneManager;
import System.SessionManager;
import System.Singleton;
import System.Utils;
import database.DatabaseHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.HelpArticle;
import model.Message;
import model.Role;

/**
 * Controller class responsible for managing instructor functions within the application.
 * This includes creating, editing, deleting, backing up, and restoring help articles.
 */
public class InstructorController {

    // FXML components
    @FXML
    private TableView<HelpArticle> articleTableView;

    @FXML
    private TableColumn<HelpArticle, String> articleIdColumn;

    @FXML
    private TableColumn<HelpArticle, String> articleTitleColumn;

    @FXML
    private TableColumn<HelpArticle, String> articleLevelColumn;

    @FXML
    private TableColumn<HelpArticle, String> articleAccessLevelColumn;

    @FXML
    private TableColumn<HelpArticle, String> articleKeywordsColumn;

    @FXML
    private Button createArticlePageButton;

    @FXML
    private Button editArticleButton;

    @FXML
    private Button deleteArticleButton;

    @FXML
    private Button backupArticlesButton;

    @FXML
    private Button restoreArticlesButton;

    @FXML
    private Button logoutButton;

    @FXML
    private ChoiceBox<String> restoreTypeChoiceBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ChoiceBox<String> levelChoiceBox;

    @FXML
    private ChoiceBox<String> groupChoiceBox;
    
    @FXML
    private TableView<Message> messagesTableView;

    @FXML
    private TableColumn<Message, String> senderColumn;

    @FXML
    private TableColumn<Message, String> contentColumn;

    @FXML
    private TableColumn<Message, String> timestampColumn;

    @FXML
    private Button refreshMessagesButton;


    // Database handler to interact with the database
    private DatabaseHandler dbHandler;

    /**
     * Initializes the controller after the FXML fields are loaded.
     * Sets up table columns, verifies user role, and loads the article data.
     */
    @FXML
    public void initialize() {
        dbHandler = Singleton.getInstance().getDbHandler();

        // Verify the user's role
        Role currentRole = SessionManager.getCurrentUserRole();
        if (currentRole != Role.INSTRUCTOR) { // Only INSTRUCTOR role should access this dashboard
            Utils.showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have permission to access this dashboard.");
            // Redirect the user to their correct dashboard or log them out
            Utils.redirectUser(currentRole);
            return;
        }

        restoreTypeChoiceBox.getItems().addAll("Merge", "Overwrite");
        restoreTypeChoiceBox.setValue("Merge");

        levelChoiceBox.getItems().addAll("all", "beginner", "intermediate", "advanced", "expert");
        levelChoiceBox.setValue("all"); // Default value

        try {
            groupChoiceBox.getItems().addAll(dbHandler.getAllGroupNames());
            groupChoiceBox.getItems().add("all");
        } catch (SQLException e) {
            showAlert("Error loading groups: " + e.getMessage());
        }

        // Set up the article table columns
        articleIdColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        articleTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        articleLevelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLevel()));
        articleAccessLevelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccessLevel()));
        articleKeywordsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKeywords()));

        // Load article data from the database
        loadArticleData();
        setupMessagesTable();
        loadMessages();


        // Add a double-click listener to view article details
        articleTableView.setRowFactory(tv -> {
            TableRow<HelpArticle> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    HelpArticle selectedArticle = row.getItem();
                    openViewArticleForm(selectedArticle);
                }
            });
            return row;
        });

        searchButton.setOnAction(event -> handleSearch());
    }

    private void openViewArticleForm(HelpArticle article) {
        try {
            // Load the FXML for the view article form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ViewArticle.fxml"));
            Parent root = loader.load();

            // Get the controller instance from the FXML loader
            ViewArticleController controller = loader.getController();

            // Initialize the controller with the selected article's details
            controller.initializeArticle(article.getId(), SessionManager.getCurrentUser().getUsername(), SessionManager.getCurrentUserRole());

            // Create a new stage (window) to show the article
            Stage stage = new Stage();
            stage.setTitle("View Article");
            stage.setScene(new Scene(root));
            stage.show();

            // Set an action for when the window is closed to return focus to the dashboard
            stage.setOnCloseRequest(event -> {
                Stage currentStage = (Stage) articleTableView.getScene().getWindow();
                currentStage.toFront(); // Bring the dashboard window back to the front
            });
        } catch (IOException e) {
            showAlert("Error loading view article form: " + e.getMessage());
        }
    }
    
    private void setupMessagesTable() {
        senderColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSender()));
        contentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));
        timestampColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp().toString()));

        refreshMessagesButton.setOnAction(event -> loadMessages());
    }

    private void loadMessages() {
        try {
            List<Message> messages = dbHandler.getMessagesForRole("INSTRUCTOR");
            ObservableList<Message> messageList = FXCollections.observableArrayList(messages);
            messagesTableView.setItems(messageList);
            messagesTableView.refresh();
        } catch (SQLException e) {
            showAlert("Error loading messages: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefreshMessages() {
        loadMessages();
    }


   
    /**
     * Handles searching for articles based on keywords, level, and group.
     */
    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedLevel = levelChoiceBox.getValue();
        String selectedGroup = groupChoiceBox.getValue();

        try {
            List<HelpArticle> articles = dbHandler.listAllArticles(SessionManager.getCurrentUser().getUsername(), SessionManager.getCurrentUserRole());

            // Filter based on the keyword, level, and group
            List<HelpArticle> filteredArticles = articles.stream()
                    .filter(article -> (selectedLevel == null || selectedLevel.equals("all") || article.getLevel().equalsIgnoreCase(selectedLevel)) &&
                            (selectedGroup == null || selectedGroup.equals("all") || (article.getGroups() != null && article.getGroups().contains(selectedGroup))) &&
                            (keyword.isEmpty() || (article.getTitle().toLowerCase().contains(keyword) ||
                                    (article.getKeywords() != null && article.getKeywords().toLowerCase().contains(keyword)))))
                    .collect(Collectors.toList());
                    
            articleTableView.getItems().setAll(filteredArticles);

        } catch (SQLException e) {
            showAlert("Error searching articles: " + e.getMessage());
        }
    }

    
 // Helper classes for backup/restore options
 	private static class BackupOptions {
 		String filename;
 		boolean includeSpecialAccess;
 		String groupFilter;

 		BackupOptions(String filename, boolean includeSpecialAccess, String groupFilter) {
 			this.filename = filename;
 			this.includeSpecialAccess = includeSpecialAccess;
 			this.groupFilter = groupFilter;
 		}
 	}

 	private static class RestoreOptions {
 		boolean overwrite;
 		boolean includeSpecialAccess;

 		RestoreOptions(boolean overwrite, boolean includeSpecialAccess) {
 			this.overwrite = overwrite;
 			this.includeSpecialAccess = includeSpecialAccess;
 		}
 	}

    
    
    @FXML
    public void handleBackupArticles() {
        // Get current user information
        String currentUser = SessionManager.getCurrentUser().getUsername();
        Role currentRole = SessionManager.getCurrentUserRole();

        // Create backup options dialog
        Dialog<BackupOptions> dialog = new Dialog<>();
        dialog.setTitle("Backup Articles");
        dialog.setHeaderText("Configure Backup Options");

        // Create dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField filenameField = new TextField("backup");
        CheckBox includeSpecialAccess = new CheckBox("Include Special Access Articles");
        ChoiceBox<String> groupFilter = new ChoiceBox<>();

        // Populate groups
        try {
            groupFilter.getItems().addAll(dbHandler.getAllGroupNames());
        } catch (SQLException e) {
            showAlert("Error loading groups: " + e.getMessage());
        }

        grid.add(new Label("Filename:"), 0, 0);
        grid.add(filenameField, 1, 0);
        grid.add(includeSpecialAccess, 0, 1, 2, 1);
        grid.add(new Label("Filter by Group:"), 0, 2);
        grid.add(groupFilter, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType backupButtonType = new ButtonType("Backup", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(backupButtonType, ButtonType.CANCEL);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == backupButtonType) {
                return new BackupOptions(
                        filenameField.getText(),
                        includeSpecialAccess.isSelected(),
                        groupFilter.getValue()
                );
            }
            return null;
        });

        Optional<BackupOptions> result = dialog.showAndWait();
        result.ifPresent(options -> {
            try {
                List<String> groups = options.groupFilter != null ?
                        Collections.singletonList(options.groupFilter) :
                        Collections.emptyList();

                dbHandler.backupHelpSystemData(
                        options.filename,
                        groups,
                        currentRole,
                        currentUser
                );

                showAlert("Backup completed successfully!");
            } catch (IOException | SQLException e) {
                showAlert("Error during backup: " + e.getMessage());
            }
        });
    }

    
    @FXML
    /**
     * Enhanced restore functionality with special access article handling.
     */
    public void handleRestoreArticles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File(s)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Backup files", "*.dat")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if (files != null && !files.isEmpty()) {
            Dialog<RestoreOptions> dialog = new Dialog<>();
            dialog.setTitle("Restore Options");
            dialog.setHeaderText("Configure Restore Options");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            CheckBox overwriteExisting = new CheckBox("Overwrite Existing Articles");
            CheckBox restoreSpecialAccess = new CheckBox("Restore Special Access Articles");

            grid.add(overwriteExisting, 0, 0);
            grid.add(restoreSpecialAccess, 0, 1);

            dialog.getDialogPane().setContent(grid);

            ButtonType restoreButtonType = new ButtonType("Restore", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(restoreButtonType, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == restoreButtonType) {
                    return new RestoreOptions(
                            overwriteExisting.isSelected(),
                            restoreSpecialAccess.isSelected()
                    );
                }
                return null;
            });

            Optional<RestoreOptions> result = dialog.showAndWait();
            result.ifPresent(options -> {
                try {
                    List<String> filePaths = files.stream()
                            .map(File::getAbsolutePath)
                            .collect(Collectors.toList());

                    dbHandler.restoreHelpSystemData(
                            filePaths,
                            options.overwrite,
                            SessionManager.getCurrentUserRole(),
                            SessionManager.getCurrentUser().getUsername()
                    );

                    showAlert("Restore completed successfully!");
                    loadArticleData(); // Refresh the article display
                } catch (IOException | SQLException | ClassNotFoundException e) {
                    showAlert("Error during restore: " + e.getMessage());
                }
            });
        }
    }


    /**
     * Handles navigation to the Create Article page.
     */
    @FXML
    public void handleCreateArticlePage() {
        SceneManager sceneManager = new SceneManager();
        sceneManager.switchScene("/Views/createArticle.fxml"); // Ensure the path to createArticle.fxml is correct
    }

    /**
     * Handles editing the selected article.
     */
    @FXML
    public void handleEditArticle() {
        HelpArticle selectedArticle = articleTableView.getSelectionModel().getSelectedItem();

        if (selectedArticle == null) {
            showAlert("Please select an article to edit.");
            return;
        }

        // TODO: Implement the logic to navigate to an Edit Article page and pre-fill the article information
        System.out.println("Editing article: " + selectedArticle.getTitle());
    }

    /**
     * Handles deleting the selected article after confirmation.
     */
    @FXML
    public void handleDeleteArticle() {
        HelpArticle selectedArticle = articleTableView.getSelectionModel().getSelectedItem();

        if (selectedArticle == null) {
            showAlert("Please select an article to delete.");
            return;
        }

        // Confirm the deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Article");
        alert.setHeaderText("Are you sure you want to delete the article titled: " + selectedArticle.getTitle() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dbHandler.deleteHelpArticle(selectedArticle.getId(), Role.INSTRUCTOR);
                System.out.println("Article deleted: " + selectedArticle.getTitle());
                loadArticleData(); // Refresh data to reflect the deletion
            } catch (SQLException e) {
                showAlert("Error deleting article: " + e.getMessage());
            }
        }
    }

    /**
     * Logs out the current instructor user and returns to the login screen.
     */
    @FXML
    public void handleLogout() {
        SessionManager.logout();
        SceneManager sceneManager = new SceneManager();
        sceneManager.switchScene("/Views/login.fxml");
    }

    /**
     * Loads article data from the database into the article TableView.
     */
    private void loadArticleData() {
        try {
            ObservableList<HelpArticle> articles = FXCollections.observableArrayList(dbHandler.listAllArticles(SessionManager.getCurrentUser().getUsername(), SessionManager.getCurrentUserRole()));
            articleTableView.getItems().clear();  // Clear existing items
            articleTableView.setItems(articles);  // Set the new list
            articleTableView.refresh();           // Refresh the TableView to update the UI
        } catch (SQLException e) {
            showAlert("Error loading article data: " + e.getMessage());
        }
    }

    /**
     * Utility method to display an alert to the user.
     *
     * @param message The message to be displayed in the alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
