package Controllers;

import java.sql.SQLException;

import System.SceneManager;
import System.Singleton;
import System.Utils;
import database.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;

/**
 * Controller class responsible for managing the admin creation process.
 * Allows an admin to be created with basic account details like username, password, and name.
 */
public class AdminCreationController {
    
    // Database handler to interact with the database
    DatabaseHandler dbHandler = null;

    // FXML components for admin creation form
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField password1;

    @FXML
    private PasswordField password2;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField middleNameField;

    @FXML
    private TextField prefNameField;

    @FXML
    private TextField emailField;

    @FXML
    private Button createAdmin;

    @FXML 
    private Label errorLabel1;

    @FXML 
    private Label errorLabel2;
    
    private SceneManager manager; 

    /**
     * Initializes the controller after the FXML fields are loaded.
     * Sets up button listeners and gets the database handler instance.
     */
    @FXML
    private void initialize() {
        // Set initial visibility of the error label
        errorLabel1.setVisible(false);
        manager = Singleton.getInstance().getManager();
        
        // Set up button event listener to handle admin creation
        createAdmin.setOnAction(event -> handleCreateAdmin());
        
        // Get the database handler instance from the Singleton
        dbHandler = Singleton.getInstance().getDbHandler();
    }

    /**
     * Handles the creation of an admin account.
     * Collects form inputs, creates a new admin user, and adds the user to the database.
     */
    public void handleCreateAdmin() {
        try {
            // Create a new User object for the admin
            User admin = new User(usernameField.getText(), password2.getText().toCharArray());
            admin.setEmail(emailField.getText());
            admin.setFirstName(firstNameField.getText());
            admin.setLastName(lastNameField.getText());
            admin.setMidName(middleNameField.getText());
            admin.setPrefName(prefNameField.getText());
            
            // Insert the initial admin user into the database
            dbHandler.insertInitialAdmin(admin);
            System.out.println("Admin created successfully.\nUserName: " + admin.getUsername());
            manager.loadLoginScene();
        } catch (SQLException e) {
            e.printStackTrace();
            // Show alert message if there is a database error
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to connect to the database.");
        }
    }
}
