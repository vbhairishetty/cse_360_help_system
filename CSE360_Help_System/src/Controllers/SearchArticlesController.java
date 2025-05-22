package Controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import System.SessionManager;
import System.Singleton;
import database.DatabaseHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.HelpArticle;
import model.Role;

/**
 * Controller class responsible for managing the search functionality for help articles.
 * Users can search by keywords, level, or a combination of both.
 */
public class SearchArticlesController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> levelComboBox;

    @FXML
    private TableView<HelpArticle> resultsTableView;

    @FXML
    private TableColumn<HelpArticle, String> titleColumn;

    @FXML
    private TableColumn<HelpArticle, String> levelColumn;

    @FXML
    private TableColumn<HelpArticle, String> descriptionColumn;

    @FXML
    private Button searchButton;

    @FXML
    private Button clearButton;

    // Database handler to interact with the database
    private DatabaseHandler dbHandler;

    // The current user's role
    private Role userRole;

    /**
     * Initializes the controller after the FXML fields are loaded.
     * Sets up the ComboBox and table columns, and ensures data retrieval is ready.
     */
    @FXML
    public void initialize() {
        dbHandler = Singleton.getInstance().getDbHandler();
        userRole = SessionManager.getCurrentUserRole();

        // Initialize the ComboBox with levels
        levelComboBox.getItems().addAll("beginner", "intermediate", "advanced", "expert");

        // Set up the table columns
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        levelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLevel()));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShortDescription()));
    }

    /**
     * Handles the search operation for help articles.
     * Searches articles by keyword and/or level.
     */
    @FXML
    public void handleSearch() {
        String keyword = searchField.getText();
        String level = levelComboBox.getValue();

        try {
            // Retrieve the search results based on the input criteria
            List<HelpArticle> articles = dbHandler.searchHelpArticles(keyword, level, new ArrayList<>(), SessionManager.getCurrentUser().getUsername(), userRole);
            ObservableList<HelpArticle> results = FXCollections.observableArrayList(articles);
            resultsTableView.setItems(results);
        } catch (SQLException e) {
            showAlert("Error retrieving articles: " + e.getMessage());
        }
    }

    /**
     * Clears the search inputs and results table.
     */
    @FXML
    public void handleClear() {
        searchField.clear();
        levelComboBox.getSelectionModel().clearSelection();
        resultsTableView.getItems().clear();
    }

    /**
     * Utility method to display an alert to the user.
     *
     * @param message The message to be displayed in the alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}