package Controllers;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import System.SceneManager;
import System.SessionManager;
import System.Singleton;
import System.Utils;
import database.DatabaseHandler;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.HelpArticle;
import model.Role;
import model.Student;
public class StudentDashboardController {

	@FXML
	private TableView<HelpArticle> articlesTable;

	@FXML
	private TableColumn<HelpArticle, String> articleTitleColumn;

	@FXML
	private TableColumn<HelpArticle, String> articleLevelColumn;

	@FXML
	private TableColumn<HelpArticle, String> articleAccessColumn;

	@FXML
	private TableColumn<HelpArticle, String> articleKeywordsColumn;

	@FXML
	private TextField searchField;

	@FXML
	private Button searchButton;

	@FXML
	private Button refreshButton;

	@FXML
	private Button logoutButton;

	@FXML
	private TextField genericMessageField; // New field for generic messages

	@FXML
	private TextField specificMessageField; // New field for specific messages

	@FXML
	private Button sendGenericMessageButton;

	@FXML
	private Button sendSpecificMessageButton;

	@FXML
	private ChoiceBox<String> levelChoiceBox; // Content level choice box

	@FXML
	private ChoiceBox<String> groupChoiceBox; // Group selection choice box

	private DatabaseHandler dbHandler;

	private SceneManager sceneManager;

	@FXML
	public void initialize() {
	    dbHandler = Singleton.getInstance().getDbHandler();
	    sceneManager = Singleton.getInstance().getManager();

	    articleTitleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
	    articleLevelColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getLevel()));
	    articleAccessColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAccessLevel()));
	    articleKeywordsColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getKeywords()));

	    if (!(SessionManager.getCurrentUserRole() == Role.STUDENT)) {
	        Utils.showAlert(AlertType.ERROR, "Access Denied", "You do not have permission to access this dashboard.");
	        Utils.redirectUser(SessionManager.getCurrentUserRole());
	        return;
	    }

	    levelChoiceBox.getItems().addAll("all", "beginner", "intermediate", "advanced", "expert");
	    levelChoiceBox.setValue("all"); // Default value

	    try {
	        groupChoiceBox.getItems().addAll(dbHandler.getAllGroupNames());
	        groupChoiceBox.getItems().add("all");
	    } catch (SQLException e) {
	        showAlert("Error loading groups: " + e.getMessage());
	    }

	    loadArticles();

	    articlesTable.setRowFactory(tv -> {
	        TableRow<HelpArticle> row = new TableRow<>();
	        row.setOnMouseClicked(event -> {
	            if (event.getClickCount() == 2 && (!row.isEmpty())) {
	                HelpArticle selectedArticle = row.getItem();
	                openViewArticleForm(selectedArticle);
	            }
	        });
	        return row;
	    });

	    sendGenericMessageButton.setOnAction(event -> handleSendGenericMessage());
	    sendSpecificMessageButton.setOnAction(event -> handleSendSpecificMessage());
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
				Stage currentStage = (Stage) articlesTable.getScene().getWindow();
				currentStage.toFront();
			});

			stage.show();
		} catch (Exception e) {
			showAlert("Error loading article: " + e.getMessage());
		}
	}

	@FXML
	private void handleSendGenericMessage() {
	    String message = genericMessageField.getText().trim();
	    if (!message.isEmpty()) {
	        try {
	            // Save the generic message to the database
	            dbHandler.saveMessage(SessionManager.getCurrentUser().getUsername(), "ADMIN", message);

	            // Clear the message field after successful submission
	            genericMessageField.clear();
	            showAlert("Success", "Generic message sent successfully. Admins have been notified.", Alert.AlertType.CONFIRMATION);
	        } catch (SQLException e) {
	            showAlert("Error", "Error sending generic message: " + e.getMessage(), Alert.AlertType.ERROR);
	        }
	    } else {
	        showAlert("Validation Error", "Please enter a message to send.", Alert.AlertType.WARNING);
	    }
	}



	@FXML
	private void handleSendSpecificMessage() {
	    String message = specificMessageField.getText().trim();
	    if (!message.isEmpty()) {
	        try {
	            // Save the specific message to the database
	            dbHandler.saveMessage(SessionManager.getCurrentUser().getUsername(), "INSTRUCTOR", message);

	            // Clear the message field after successful submission
	            specificMessageField.clear();
	            showAlert("Success", "Specific message sent successfully. Instructors have been notified.", Alert.AlertType.CONFIRMATION);
	        } catch (SQLException e) {
	            showAlert("Error", "Error sending specific message: " + e.getMessage(), Alert.AlertType.ERROR);
	        }
	    } else {
	        showAlert("Validation Error", "Please enter a message to send.", Alert.AlertType.WARNING);
	    }
	}
	
	
	private void showAlert(String title, String message, Alert.AlertType type) {
	    Alert alert = new Alert(type);
	    alert.setTitle(title);
	    alert.setHeaderText(null);
	    alert.setContentText(message);
	    alert.showAndWait();
	}

	
	@FXML
	public void handleSearch() {
	    String keyword = searchField.getText().trim().toLowerCase();
	    String selectedLevel = levelChoiceBox.getValue();
	    String selectedGroup = groupChoiceBox.getValue();

	    try {
	        List<HelpArticle> articles = dbHandler.listAllArticles(SessionManager.getCurrentUser().getUsername(), SessionManager.getCurrentUserRole());

	        // Filter based on the keyword, level, and group
	        List<HelpArticle> filteredArticles = articles.stream()
	                .filter(article -> {
	                    // Check level filtering with null safety
	                    boolean matchesLevel = (selectedLevel == null || selectedLevel.equals("all")) || 
	                                           (article.getLevel() != null && article.getLevel().equalsIgnoreCase(selectedLevel));

	                    // Check group filtering with null safety
	                    boolean matchesGroup = (selectedGroup == null || selectedGroup.equals("all")) || 
	                                           (article.getGroups() != null && article.getGroups().stream()
	                                                .anyMatch(group -> group != null && group.equalsIgnoreCase(selectedGroup)));

	                    // Check keyword filtering with null safety
	                    boolean matchesKeyword = keyword.isEmpty() ||
	                                             (article.getTitle() != null && article.getTitle().toLowerCase().contains(keyword)) ||
	                                             (article.getKeywords() != null && article.getKeywords().toLowerCase().contains(keyword));

	                    return matchesLevel && matchesGroup && matchesKeyword;
	                })
	                .collect(Collectors.toList());

	        articlesTable.getItems().setAll(filteredArticles);

	        if (SessionManager.getCurrentUser() instanceof Student) {
	            Student currentStudent = (Student) SessionManager.getCurrentUser();
	            currentStudent.addSearchRequest(keyword);
	        }
	    } catch (SQLException e) {
	        showAlert("Error searching articles: " + e.getMessage());
	    }
	}



	/**
	 * Handles refreshing the articles table.
	 */
	@FXML
	public void handleRefresh() {
		System.out.println("Refreshing articles table...");
		loadArticles();
	}

	/**
	 * Handles logging out the user and redirecting to the login screen.
	 */
	@FXML
	public void handleLogout() {
		System.out.println("Logging out...");
		sceneManager.switchScene("/Views/login.fxml");
		SessionManager.logout(); // Clear the session information
	}


	private void loadArticles() {
	    try {
	        List<HelpArticle> articles = dbHandler.listAllArticles(SessionManager.getCurrentUser().getUsername(), SessionManager.getCurrentUserRole());

	        // Apply existing filters from levelChoiceBox and groupChoiceBox
	        String selectedLevel = levelChoiceBox.getValue();
	        String selectedGroup = groupChoiceBox.getValue();
	        
	        List<HelpArticle> filteredArticles = articles.stream()
	                .filter(article -> {
	                    // Check level filtering
	                    boolean matchesLevel = (selectedLevel == null || selectedLevel.equals("all")) || 
	                                           (article.getLevel() != null && article.getLevel().equalsIgnoreCase(selectedLevel));
	                    
	                    // Check group filtering
	                    boolean matchesGroup = (selectedGroup == null || selectedGroup.equals("all")) || 
	                                           (article.getGroups() != null && article.getGroups().stream()
	                                                .anyMatch(group -> group != null && group.equalsIgnoreCase(selectedGroup)));

	                    return matchesLevel && matchesGroup;
	                })
	                .collect(Collectors.toList());

	        articlesTable.getItems().setAll(filteredArticles);
	    } catch (SQLException e) {
	        showAlert("Error loading articles: " + e.getMessage());
	    }
	}



	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Information");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
