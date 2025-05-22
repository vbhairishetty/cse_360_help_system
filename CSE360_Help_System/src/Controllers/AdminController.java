package Controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
import model.User;

/**
 * Controller class responsible for managing administrative functions within the application.
 * This includes user role assignment, user invitation, resetting user credentials, and more.
 */
public class AdminController {

	// FXML components
	@FXML
	private TextField emailField;

	@FXML
	private TableView<User> userTableView;

	@FXML
	private ComboBox<Role> assignRole;

	@FXML
	private ComboBox<Role> updateRole;

	@FXML
	private ComboBox<String> addRemoveChoice;

	@FXML
	private TableColumn<User, String> usernameColumn;

	@FXML
	private TableColumn<User, String> roleColumn;

	@FXML
	private TableColumn<User, String> emailColumn;

	@FXML
	private Button inviteButton;

	@FXML
	private Button resetButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button logoutButton;

	@FXML
	private Button refreshButton;

	@FXML
	private Button deleteArticleButton;

	@FXML
	private Button backupArticlesButton;

	@FXML
	private Button restoreArticlesButton;

	@FXML
	private Button createArticlePageButton;

	@FXML
	private Button editArticleButton;

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
	private TableView<String> groupTableView;

	@FXML
	private TableColumn<String, String> groupNameColumn;

	@FXML
	private ComboBox<String> userComboBox;

	@FXML
	private CheckBox adminRightsCheckBox;

	@FXML
	private CheckBox viewRightsCheckBox;
	
	@FXML
	private TableView<Message> messagesTableView; // New TableView for messages

	@FXML
	private TableColumn<Message, String> senderColumn; // Column for message sender

	@FXML
	private TableColumn<Message, String> contentColumn; // Column for message content

	@FXML
	private TableColumn<Message, String> timestampColumn; // Column for message timestamp

	@FXML
	private Button refreshMessagesButton;

	// Database handler to interact with the database
	private DatabaseHandler dbHandler;

	/**
	 * Initializes the controller after the FXML fields are loaded.
	 * Sets up role comboboxes, table columns, and loads the user data.
	 */
	@FXML
	public void initialize() {
		dbHandler = Singleton.getInstance().getDbHandler();

		// Verify the user's role
		Role currentRole = SessionManager.getCurrentUserRole();
		if (currentRole != Role.ADMIN) { // Only INSTRUCTOR role should access this dashboard
			Utils.showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have permission to access this dashboard.");
			// Redirect the user to their correct dashboard or log them out
			Utils.redirectUser(currentRole);
			return;
		}

		// Initialize the role ComboBox for updating and assigning roles
		updateRole.getItems().addAll(Role.values());
		assignRole.getItems().addAll(Role.values());

		// Initialize Add/Remove ChoiceBox
		addRemoveChoice.getItems().addAll("Add", "Remove");
		addRemoveChoice.setValue("Add"); // Set default valuea

		// Set up the user table columns
		usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
		roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRolesAsString()));
		emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

		// Set up the article table columns
		articleIdColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
		articleTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
		articleLevelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLevel()));
		articleAccessLevelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccessLevel()));
		articleKeywordsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKeywords()));

		// Initialize the messages TableView
		senderColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSender()));
		contentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));
		timestampColumn.setCellValueFactory(data -> new SimpleStringProperty(
		    data.getValue().getTimestamp() != null ? data.getValue().getTimestamp().toString() : "No Timestamp"));


		 // Load messages
		try {
		    loadGenericMessages();
		} catch (Exception e) {
		    System.err.println("Unexpected error in loading messages: " + e.getMessage());
		    e.printStackTrace();
		}

		
		// Load user and article data from the database
		loadUserData();
		loadArticleData();

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
	}

	private void openViewArticleForm(HelpArticle article) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ViewArticle.fxml"));
			Parent root = loader.load();

			ViewArticleController controller = loader.getController();

			// Get decrypted body if needed
			if (article.isSpecialAccess()) {
				String decryptedBody = dbHandler.getDecryptedBody(
						Long.parseLong(article.getId()),
						SessionManager.getCurrentUser().getUsername(),
						SessionManager.getCurrentUserRole()
						);
				article.setBody(decryptedBody);
			}

			controller.initializeArticle(article.getId(), SessionManager.getCurrentUser().getUsername(), SessionManager.getCurrentUserRole());

			Stage stage = new Stage();
			stage.setTitle("View Article - " + article.getTitle());
			stage.setScene(new Scene(root));

			// Add window close handler
			stage.setOnCloseRequest(event -> {
				Stage currentStage = (Stage) articleTableView.getScene().getWindow();
				currentStage.toFront();
			});

			stage.show();
		} catch (Exception e) {
			showAlert("Error loading article: " + e.getMessage());
		}
	}

	@FXML
	public void handleCreateSpecialAccessGroup() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Create Special Access Group");
		dialog.setHeaderText("Enter group name:");
		dialog.setContentText("Group Name:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(groupName -> {
			try {
				long groupId = dbHandler.createSpecialAccessGroup(
						groupName,
						SessionManager.getCurrentUser().getUsername()
						);
				showAlert("Success", "Special access group created successfully.", Alert.AlertType.INFORMATION);
			} catch (SQLException e) {
				showAlert("Error creating special access group: " + e.getMessage());
			}
		});
	}
	
	@FXML
	public void handleRefreshMessages() {
	    loadGenericMessages();
	}
	
	private void loadGenericMessages() {
	    try {
	        List<Message> messages = dbHandler.getMessagesForRole("ADMIN");
	        System.out.println("Messages received in loadGenericMessages: " + messages.size()); // Debugging

	        ObservableList<Message> messageList = FXCollections.observableArrayList(messages);
	        messagesTableView.getItems().clear();  // Clear any existing items
	        messagesTableView.setItems(messageList); // Set new items
	        messagesTableView.refresh(); // Refresh the view

	        System.out.println("Messages loaded into TableView: " + messageList.size()); // Debugging
	    } catch (SQLException e) {
	        showAlert("Error loading messages: " + e.getMessage());
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
	 * Loads article data from the database into the article TableView.
	 */
	private void loadArticleData() {
		try {
			// Get current user and role from SessionManager
			String currentUser = SessionManager.getCurrentUser().getUsername();
			Role userRole = SessionManager.getCurrentUserRole();

			// Get articles with proper access control
			ObservableList<HelpArticle> articles = FXCollections.observableArrayList(
					dbHandler.listAllArticles(currentUser, userRole)
					);

			articleTableView.getItems().clear();
			articleTableView.setItems(articles);
			articleTableView.refresh();
		} catch (SQLException e) {
			showAlert("Error loading article data: " + e.getMessage());
		}
	}

	// Rest of the methods like handleInviteUser, handleResetUser, handleAddRemoveRole, etc.
	// can remain the same as in the original AdminController.

	/**
	 * Handles the reset operation for a selected user's account.
	 * Generates an OTP and sets the expiration, then updates the user account.
	 */
	@FXML
	public void handleResetUser() {
		User selectedUser = userTableView.getSelectionModel().getSelectedItem();

		if (selectedUser == null) {
			showAlert("Please select a user to reset.");
			return;
		}

		String otp = Integer.toString(generateOneTimePassword());
		LocalDateTime expiration = LocalDateTime.now().plusMinutes(10); // Set OTP expiration to 10 minutes from now

		try {
			dbHandler.resetUserAccount(selectedUser.getUsername(), otp, expiration);
			System.out.println("Account reset for user: " + selectedUser.getUsername() + " with OTP: " + otp);
			loadUserData(); // Refresh data to reflect the changes
		} catch (SQLException e) {
			showAlert("Error resetting user account: " + e.getMessage());
		}
	}

	/**
	 * Generates a random 6-digit one-time password.
	 *
	 * @return a 6-digit OTP.
	 */
	private int generateOneTimePassword() {
		return (int) (Math.random() * 1000000); // Example: 6-digit OTP
	}

	/**
	 * Handles the deletion of a selected user's account after confirmation.
	 */
	@FXML
	public void handleDeleteUser() {
		User selectedUser = userTableView.getSelectionModel().getSelectedItem();

		if (selectedUser == null) {
			showAlert("Please select a user to delete.");
			return;
		}

		// Confirm the deletion
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete User");
		alert.setHeaderText("Are you sure you want to delete " + selectedUser.getUsername() + "?");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				dbHandler.deleteUserAccount(selectedUser.getUsername());
				System.out.println("User " + selectedUser.getUsername() + " deleted.");
				loadUserData(); // Refresh data to reflect the deletion
			} catch (SQLException e) {
				showAlert("Error deleting user: " + e.getMessage());
			}
		}
	}

	/**
	 * Handles adding or removing roles for a selected user.
	 * The action (Add/Remove) is determined by the choice in the ComboBox.
	 */
	@FXML
	public void handleAddRemoveRole() {
		User selectedUser = userTableView.getSelectionModel().getSelectedItem();
		Role selectedRole = updateRole.getValue();
		String action = addRemoveChoice.getValue(); // Get whether to "Add" or "Remove"

		// Validate input
		if (selectedUser == null || selectedRole == null || action == null) {
			showAlert("Please select a user, a role, and an action (Add/Remove).");
			return;
		}

		try {
			// Handle Add/Remove role based on the selected action
			if ("Add".equals(action)) {
				if (!selectedUser.getRoles().contains(selectedRole)) {
					dbHandler.modifyUserRole(selectedUser.getUsername(), selectedRole, true);
					System.out.println("Role " + selectedRole + " added to " + selectedUser.getUsername());
				} else {
					showAlert("The user already has the role " + selectedRole + ".");
				}
			} else if ("Remove".equals(action)) {
				if (selectedUser.getRoles().contains(selectedRole)) {
					dbHandler.modifyUserRole(selectedUser.getUsername(), selectedRole, false);
					System.out.println("Role " + selectedRole + " removed from " + selectedUser.getUsername());
				} else {
					showAlert("The user does not have the role " + selectedRole + ".");
				}
			}

			// Refresh the table to reflect changes
			loadUserData();

		} catch (SQLException e) {
			showAlert("Error modifying user role: " + e.getMessage());
		}
	}

	/**
	 * Logs out the current admin user and returns to the login screen.
	 *
	 * @throws SQLException if there is an issue during logout.
	 */
	@FXML
	public void handleLogout() throws SQLException {
		SessionManager.logout();
		SceneManager sceneManager = new SceneManager();
		sceneManager.loadLoginScene();
	}

	/**
	 * Refreshes the user data displayed in the user TableView.
	 */
	@FXML
	public void handleRefresh() {
		loadUserData();
	}

	/**
	 * Loads user data from the database into the user TableView.
	 */
	private void loadUserData() {
		try {
			ObservableList<User> users = FXCollections.observableArrayList(dbHandler.listAllUserAccounts());
			userTableView.getItems().clear();  // Clear existing items
			userTableView.setItems(users);     // Set the new list
			userTableView.refresh();           // Refresh the TableView to update the UI
		} catch (SQLException e) {
			showAlert("Error loading user data: " + e.getMessage());
		}
	}

	/**
	 * Utility method to display an alert to the user.
	 *
	 * @param message The message to be displayed in the alert.
	 */
	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
		System.out.println(message);
	}

	/**
	 * Handles inviting a new user by creating an account with an invitation code and assigning roles.
	 */
	@FXML
	public void handleInviteUser() {
		String email = emailField.getText();
		Role role = assignRole.getValue();

		if (email.isEmpty() || role == null) {
			showAlert("Please enter an email and select a role.");
			return;
		}

		String invitationCode = generateOneTimeCode();
		List<Role> roles = new ArrayList<>();
		roles.add(role);

		try {
			// Create user with email and invitation code
			dbHandler.createUser(email, invitationCode, roles);

			// Show success message with invitation code
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("User Invited");
			alert.setHeaderText(null);
			alert.setContentText("User invited successfully!\n\n" +
					"Email: " + email + "\n" +
					"Role: " + role + "\n" +
					"Invitation Code: " + invitationCode + "\n\n" +
					"Please share the invitation code with the user.");
			alert.showAndWait();

			emailField.clear();
			assignRole.getSelectionModel().clearSelection();
			loadUserData();
		} catch (SQLException e) {
			showAlert("Error inviting user: " + e.getMessage());
		}
	}

	public void handleDeleteArticle() {
		HelpArticle selectedArticle = articleTableView.getSelectionModel().getSelectedItem();

		if (selectedArticle == null) {
			showAlert("Please select an article to delete.");
			return;
		}

		// Check if user has permission to delete this article
		try {
			String currentUser = SessionManager.getCurrentUser().getUsername();
			if (!currentUser.equals(selectedArticle.getCreatedBy()) && 
					SessionManager.getCurrentUserRole() != Role.ADMIN) {
				showAlert("You don't have permission to delete this article.");
				return;
			}

			// Confirm deletion
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Delete Article");
			alert.setHeaderText("Are you sure you want to delete this article?");
			alert.setContentText("Title: " + selectedArticle.getTitle() + "\n" +
					"This action cannot be undone.");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				dbHandler.deleteHelpArticle(selectedArticle.getId(), SessionManager.getCurrentUserRole());
				loadArticleData();
				showAlert("Success", "Article deleted successfully.", Alert.AlertType.INFORMATION);
			}
		} catch (SQLException e) {
			showAlert("Error deleting article: " + e.getMessage());
		}
	}


	// Utility method to sanitize the filename
	private String sanitizeFilename(String filename) {
		return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
	}


	private void showAlert(String title, String message, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public List<String> getSelectedGroupsForBackup() {
		List<String> selectedGroups = new ArrayList<>();
		try {
			List<String> allGroups = dbHandler.getAllGroupNames();  // Call the method from DatabaseHandler

			// Create checkboxes for each group
			List<CheckBox> checkboxes = new ArrayList<>();
			for (String group : allGroups) {
				CheckBox checkBox = new CheckBox(group);
				checkboxes.add(checkBox);
			}

			// Create a custom dialog
			Dialog<List<String>> dialog = new Dialog<>();
			dialog.setTitle("Select Groups for Backup");
			dialog.setHeaderText("Select the groups you want to include in the backup:");

			// Set the button types
			ButtonType backupButtonType = new ButtonType("Backup", ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(backupButtonType, ButtonType.CANCEL);

			// Set the content of the dialog
			VBox content = new VBox(10);
			content.getChildren().addAll(checkboxes);
			dialog.getDialogPane().setContent(content);

			// Convert the result to a list of selected groups when the backup button is clicked
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == backupButtonType) {
					return checkboxes.stream()
							.filter(CheckBox::isSelected)
							.map(CheckBox::getText)
							.collect(Collectors.toList());
				}
				return null;
			});

			// Show the dialog and wait for the result
			Optional<List<String>> result = dialog.showAndWait();
			result.ifPresent(selectedGroups::addAll);

		} catch (SQLException e) {
			showAlert("Error", "Failed to retrieve groups: " + e.getMessage(), Alert.AlertType.ERROR);
		}

		return selectedGroups;
	}


	public void handleEditArticle() {
		HelpArticle selectedArticle = articleTableView.getSelectionModel().getSelectedItem();
		if (selectedArticle != null) {
			// Load the selected article's data into the edit form
			openEditArticleForm(selectedArticle);
		} else {
			showAlert("Please select an article to edit.");
		}
	}

	private void openEditArticleForm(HelpArticle article) {
		// This method would open the edit article form and populate the fields with the article's details.
		try {
			// Load the FXML for the edit article form
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EditArticles.fxml"));
			Parent root = loader.load();

			// Get the controller instance from the FXML loader
			EditArticleController controller = loader.getController();

			// Initialize the controller with the selected article's details
			controller.initializeArticle(article);

			Stage stage = new Stage();
			stage.setTitle("Edit Article");
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			showAlert("Error loading edit article form: " + e.getMessage());
		}
	}

	/**
	 * Manages special access rights for users in a group
	 */
	@FXML
	public void handleManageSpecialAccess() {
		String selectedGroup = groupTableView.getSelectionModel().getSelectedItem();
		String selectedUser = userComboBox.getValue();

		if (selectedGroup == null || selectedUser == null) {
			showAlert("Please select both a group and a user.");
			return;
		}

		try {
			long groupId = getGroupId(selectedGroup);
			boolean isAdmin = adminRightsCheckBox.isSelected();
			boolean canView = viewRightsCheckBox.isSelected();

			dbHandler.addUserToSpecialAccessGroup(groupId, selectedUser, isAdmin, canView);

			showAlert("Success", "Special access rights updated successfully.", Alert.AlertType.INFORMATION);
			dbHandler.loadGroupData(); // Refresh the group display
		} catch (SQLException e) {
			showAlert("Error updating special access rights: " + e.getMessage());
		}
	}

	/**
	 * Removes special access rights from a user
	 */
	@FXML
	public void handleRemoveSpecialAccess() {
		String selectedGroup = groupTableView.getSelectionModel().getSelectedItem();
		String selectedUser = userComboBox.getValue();

		if (selectedGroup == null || selectedUser == null) {
			showAlert("Please select both a group and a user.");
			return;
		}

		try {
			long groupId = getGroupId(selectedGroup);
			dbHandler.removeUserFromSpecialAccessGroup(groupId, selectedUser);

			showAlert("Success", "Special access rights removed successfully.", Alert.AlertType.INFORMATION);
			dbHandler.loadGroupData();
		} catch (SQLException e) {
			showAlert("Error removing special access rights: " + e.getMessage());
		}
	}

	private long getGroupId(String groupName) throws SQLException {
		// Add method to get group ID from name
		// This would need to be implemented in DatabaseHandler
		return dbHandler.getGroupIdByName(groupName);
	}


	/**
	 * Enhanced backup functionality with special access article handling
	 */
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
		ComboBox<String> groupFilter = new ComboBox<>();

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

				showAlert("Success", "Backup completed successfully!", Alert.AlertType.INFORMATION);
			} catch (IOException | SQLException e) {
				showAlert("Error during backup: " + e.getMessage());
			}
		});
	}

	/**
	 * Enhanced restore functionality with special access article handling
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

					showAlert("Success", "Restore completed successfully!", Alert.AlertType.INFORMATION);
					loadArticleData(); // Refresh the article display
				} catch (IOException | SQLException | ClassNotFoundException e) {
					showAlert("Error during restore: " + e.getMessage());
				}
			});
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

	/**
	 * Generates a one-time code used for user invitation.
	 *
	 * @return A 6-digit one-time code.
	 */
	private String generateOneTimeCode() {
		return String.valueOf((int) (Math.random() * 1000000)); // Example: 6-digit code
	}

	/**
	 * Handles group management operations
	 */
	@FXML
	private void handleCreateGroup() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Create New Group");
		dialog.setHeaderText("Enter group name:");
		dialog.setContentText("Group Name:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(groupName -> {
			try {
				boolean isSpecialAccess = false; // Default to regular group
				Alert specialAccessAlert = new Alert(Alert.AlertType.CONFIRMATION);
				specialAccessAlert.setTitle("Group Type");
				specialAccessAlert.setHeaderText("Should this be a special access group?");
				specialAccessAlert.setContentText("Choose group type:");

				Optional<ButtonType> specialAccessResult = specialAccessAlert.showAndWait();
				if (specialAccessResult.isPresent() && specialAccessResult.get() == ButtonType.OK) {
					isSpecialAccess = true;
				}

				if (isSpecialAccess) {
					dbHandler.createSpecialAccessGroup(groupName, SessionManager.getCurrentUser().getUsername());
				} else {
					dbHandler.addGroupIfNotExists(groupName);
				}

				showAlert("Success", "Group created successfully!", Alert.AlertType.INFORMATION);
				dbHandler.loadGroupData();
			} catch (SQLException e) {
				showAlert("Error creating group: " + e.getMessage());
			}
		});
	}

	@FXML
	private void handleManageGroupMembers() {
		String selectedGroup = groupTableView.getSelectionModel().getSelectedItem();
		if (selectedGroup == null) {
			showAlert("Please select a group to manage.");
			return;
		}

		try {
			// Create dialog for managing group members
			Dialog<Void> dialog = new Dialog<>();
			dialog.setTitle("Manage Group Members");
			dialog.setHeaderText("Manage members for group: " + selectedGroup);

			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));

			// Create lists for available and current members
			ListView<String> availableUsers = new ListView<>();
			ListView<String> currentMembers = new ListView<>();

			// Populate available users
			List<User> allUsers = dbHandler.listAllUserAccounts();
			availableUsers.getItems().addAll(
					allUsers.stream()
					.map(User::getUsername)
					.collect(Collectors.toList())
					);

			// Add controls
			Button addButton = new Button("Add >");
			Button removeButton = new Button("< Remove");

			addButton.setOnAction(e -> moveSelectedItems(availableUsers, currentMembers));
			removeButton.setOnAction(e -> moveSelectedItems(currentMembers, availableUsers));

			// Layout
			grid.add(new Label("Available Users:"), 0, 0);
			grid.add(new Label("Group Members:"), 2, 0);
			grid.add(availableUsers, 0, 1);
			grid.add(new VBox(10, addButton, removeButton), 1, 1);
			grid.add(currentMembers, 2, 1);

			dialog.getDialogPane().setContent(grid);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == ButtonType.OK) {
					updateGroupMembers(selectedGroup, currentMembers.getItems());
				}
				return null;
			});

			dialog.showAndWait();
		} catch (SQLException e) {
			showAlert("Error managing group members: " + e.getMessage());
		}
	}

	private void moveSelectedItems(ListView<String> source, ListView<String> target) {
		ObservableList<String> selectedItems = source.getSelectionModel().getSelectedItems();
		target.getItems().addAll(selectedItems);
		source.getItems().removeAll(selectedItems);
	}

	private void updateGroupMembers(String groupName, List<String> members) {
		try {
			long groupId = getGroupId(groupName);

			// Update group members in database
			for (String member : members) {
				dbHandler.addUserToSpecialAccessGroup(groupId, member, false, true);
			}

			showAlert("Success", "Group members updated successfully!", Alert.AlertType.INFORMATION);
		} catch (SQLException e) {
			showAlert("Error updating group members: " + e.getMessage());
		}
	}

	/**
	 * Enhanced role management functionality
	 */
	@FXML
	private void handleRoleManagement() {
		User selectedUser = userTableView.getSelectionModel().getSelectedItem();
		if (selectedUser == null) {
			showAlert("Please select a user to manage roles.");
			return;
		}

		try {
			// Create dialog for role management
			Dialog<List<Role>> dialog = new Dialog<>();
			dialog.setTitle("Manage User Roles");
			dialog.setHeaderText("Manage roles for user: " + selectedUser.getUsername());

			// Create grid for layout
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));

			// Create checkboxes for each role
			List<CheckBox> roleCheckboxes = new ArrayList<>();
			int row = 0;
			for (Role role : Role.values()) {
				CheckBox cb = new CheckBox(role.name());
				cb.setSelected(selectedUser.hasRole(role));
				roleCheckboxes.add(cb);
				grid.add(cb, 0, row++);
			}

			dialog.getDialogPane().setContent(grid);

			// Add buttons
			ButtonType updateButtonType = new ButtonType("Update Roles", ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

			// Convert the result
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == updateButtonType) {
					return roleCheckboxes.stream()
							.filter(CheckBox::isSelected)
							.map(cb -> Role.valueOf(cb.getText()))
							.collect(Collectors.toList());
				}
				return null;
			});

			Optional<List<Role>> result = dialog.showAndWait();
			result.ifPresent(newRoles -> {
				try {
					// Remove existing roles
					for (Role role : Role.values()) {
						if (selectedUser.hasRole(role) && !newRoles.contains(role)) {
							dbHandler.modifyUserRole(selectedUser.getUsername(), role, false);
						}
					}

					// Add new roles
					for (Role role : newRoles) {
						if (!selectedUser.hasRole(role)) {
							dbHandler.modifyUserRole(selectedUser.getUsername(), role, true);
						}
					}

					showAlert("Success", "User roles updated successfully!", Alert.AlertType.INFORMATION);
					loadUserData(); // Refresh the display
				} catch (SQLException e) {
					showAlert("Error updating user roles: " + e.getMessage());
				}
			});
		} catch (Exception e) {
			showAlert("Error managing roles: " + e.getMessage());
		}
	}

	

}
