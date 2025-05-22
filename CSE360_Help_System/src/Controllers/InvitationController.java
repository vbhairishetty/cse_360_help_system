package Controllers;

import java.sql.SQLException;

import System.SceneManager;
import System.Singleton;
import database.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

/**
 * Controller class responsible for handling invitation-based user setup.
 * This class allows users to enter their invitation code and email to validate their account setup.
 */
public class InvitationController {

    // FXML components for invitation form
    @FXML
    private TextField invitationCode;

    @FXML
    private TextField emailId;

    @FXML
    private Button nextButton;

    @FXML
    private Hyperlink cancelBtn;

    // References to database handler and scene manager
    private DatabaseHandler dbHandler;
    private SceneManager sceneManager;

    /**
     * Initializes the controller after the FXML fields are loaded.
     * Sets up the database handler and scene manager.
     */
    @FXML
    public void initialize() {
        // Get database handler and scene manager instances from the Singleton
        dbHandler = Singleton.getInstance().getDbHandler();
        sceneManager = Singleton.getInstance().getManager();
    }

    /**
     * Handles the "Next" button click event.
     * Validates the invitation code and email ID entered by the user and switches to the account setup scene if valid.
     * 
     * @throws SQLException if a database error occurs during validation.
     */
    @FXML
    public void handleNext() throws SQLException {
        String code = invitationCode.getText();
        String email = emailId.getText();

        // Validate the input fields
        if (code.isEmpty() || email.isEmpty()) {
            showAlert("Please fill in both invitation code and email ID.");
            return;
        }

        // Validate the invitation using the database
        String userEmail = dbHandler.loginWithInvitation(email, code);

        if (userEmail != null) {
            // If the invitation is valid, proceed to the account setup
            System.out.println("Valid invitation. Proceeding to account setup...");
            Singleton.getInstance().setEmail(userEmail);
            sceneManager.switchScene("/Views/setAcc.fxml");
        } else {
            // Display an alert if the invitation is invalid
            showAlert("Invalid invitation code or email ID.");
        }
    }

    /**
     * Handles the "Cancel" button click event.
     * Switches the scene back to the login screen.
     */
    public void handleCancel() {
        try {
            // Load the login scene
            sceneManager.loadLoginScene();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method to show alert dialogs for user validation errors.
     * 
     * @param message The message to display in the alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
