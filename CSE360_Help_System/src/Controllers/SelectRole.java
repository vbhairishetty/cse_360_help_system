package Controllers;

import System.SceneManager;
import System.SessionManager;
import System.Singleton;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.Role;
import model.User;

/**
 * Controller class for selecting a role for the current session.
 * This class allows users with multiple roles to select which role they want to use.
 */
public class SelectRole {

    // FXML UI components for role selection buttons and labels
    @FXML
    private Button studentButton;

    @FXML
    private Button instructorButton;

    @FXML
    private Button taGraderButton;

    @FXML
    private Button exStudentButton;

    @FXML
    private Label welcomeLabel;

    /**
     * Initializes the SelectRole controller.
     * Displays a welcome message and ensures a user is logged in before allowing role selection.
     */
    @FXML
    public void initialize() {
        // Show a welcome message for the current user
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + ". Please select a role:");
        } else {
            showAlert("No user is logged in. Please log in again.");
        }
    }

    /**
     * Handles the Student role selection.
     * Sets the role to STUDENT and proceeds to the appropriate homepage.
     */
    @FXML
    public void handleStudentSelection() {
        setRoleAndProceed(Role.STUDENT);
    }

    /**
     * Handles the Instructor role selection.
     * Sets the role to INSTRUCTOR and proceeds to the appropriate homepage.
     */
    @FXML
    public void handleInstructorSelection() {
        setRoleAndProceed(Role.INSTRUCTOR);
    }

    /**
     * Handles the TA/Grader role selection.
     * Sets the role to TA and proceeds to the appropriate homepage.
     */
    @FXML
    public void handleTAGraderSelection() {
        setRoleAndProceed(Role.TA);
    }

    /**
     * Handles the Ex-student role selection.
     * Sets the role to PAST_STUDENT and proceeds to the appropriate homepage.
     */
    @FXML
    public void handleExStudentSelection() {
        setRoleAndProceed(Role.PAST_STUDENT);
    }

    /**
     * Helper method to set the selected role for the current user and proceed to the appropriate homepage.
     *
     * @param role The role to be assigned to the current session.
     */
    private void setRoleAndProceed(Role role) {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser != null && currentUser.getRoles().contains(role)) {
            // Set the current role for the session
            SessionManager.setCurrentRole(role);
            
            // Redirect to the appropriate homepage based on the role
            SceneManager sceneManager = Singleton.getInstance().getManager();
            switch (role) {
                case STUDENT:
                    sceneManager.switchScene("/Views/studentDashboard.fxml");
                    break;
                case INSTRUCTOR:
                    sceneManager.switchScene("/Views/instructorDashboard.fxml");
                    break;
                case TA:
                    sceneManager.switchScene("/Views/taDashboard.fxml");
                    break;
                case PAST_STUDENT:
                    sceneManager.switchScene("/Views/exStudentDashboard.fxml");
                    break;
                default:
                    showAlert("Invalid role selected.");
                    break;
            }
        } else {
            // Show an alert if the user does not have access to the selected role
            showAlert("You do not have access to the selected role.");
        }
    }

    /**
     * Utility method to display alert messages.
     * This method is used to provide feedback to the user about errors during role selection.
     *
     * @param message The message to be displayed in the alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Role Selection Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
