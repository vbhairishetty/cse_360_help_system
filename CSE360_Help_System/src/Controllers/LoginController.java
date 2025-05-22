package Controllers;

import java.sql.SQLException;
import java.util.ArrayList;

import System.SceneManager;
import System.SessionManager;
import System.Singleton;
import System.Utils;
import database.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Role;
import model.User;

/**
 * Controller class for managing user login functionality.
 * This class handles user authentication, scene navigation, and managing the user session.
 */
public class LoginController {

    // FXML components for the login form
    @FXML
    private TextField usernameField;  // TextField to input the username

    @FXML
    private TextField passwordField;  // TextField to input the password

    @FXML
    private Hyperlink signupPage;  // Hyperlink to navigate to the sign-up page

    @FXML
    private Hyperlink resetPasswordPage;  // Hyperlink to navigate to the password reset page

    @FXML
    private Label usernameLabel;  // Label to show username validation errors

    @FXML
    private Label passwordLabel;  // Label to show password validation errors

    // SceneManager and DatabaseHandler references for scene transitions and database operations
    private SceneManager manager;
    private DatabaseHandler dbHandler;

    /**
     * Initializes the controller after the FXML fields are loaded.
     * Establishes a connection with the database and sets up the initial UI state.
     * 
     * @throws SQLException if there is an issue with the database connection.
     */
    @FXML
    private void initialize() throws SQLException {
        // Initialize DatabaseHandler and SceneManager instances
        dbHandler = Singleton.getInstance().getDbHandler();
        manager = Singleton.getInstance().getManager();

        // If database connection fails, show error alert and stop further execution
        if (dbHandler == null) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to initialize database connection.");
            return;
        }

        // Hide error labels initially for a clean UI
        usernameLabel.setVisible(false);
        passwordLabel.setVisible(false);
    }

    /**
     * Handles the "Login" button click event.
     * Validates the user's credentials and manages the login flow based on different login statuses.
     * 
     * @throws SQLException if there is an issue with the database operation.
     */
    @FXML
    public void handleLogin() throws SQLException {
        // Validate the input fields for non-empty username and password
        if (!Utils.validateInput(usernameField, usernameLabel, "UserName required") || 
            !Utils.validateInput(passwordField, passwordLabel, "Password Required")) {
            return;
        }

        // Perform user login using the provided credentials
        String loginAction = dbHandler.loginUser(usernameField.getText(), passwordField.getText());
        System.out.println(loginAction); // Debugging output for login action status

        switch (loginAction) {
            case "LOGIN_SUCCESS": 
                // If login is successful, retrieve user details from the database
                User user = dbHandler.getUser(usernameField.getText());

                if (user != null) {
                    // Set up a session for the logged-in user
                    SessionManager.login(user);

                    // Ensure user roles are initialized
                    if (user.getRoles() == null) {
                        user.setRoles(new ArrayList<>());
                    }

                    // Handle role selection or redirect to the appropriate page based on roles
                    if (SessionManager.hasMultipleRoles()) {
                        showRoleSelectionScreen(user);
                    } else {
                        Role role = user.getRoles().get(0);
                        SessionManager.setCurrentRole(role);
                        manager.switchSceneBasedOnRole(role);
                    }
                } else {
                    // Show error alert if user details cannot be retrieved from the database
                    Utils.showAlert(Alert.AlertType.ERROR, "Login Error", "Failed to retrieve user details.");
                }
                break;

            case "INCORRECT_PASSWORD": 
                // Show error label if the password is incorrect
                Utils.validateInput(passwordField, passwordLabel, "Please check your password");
                break;

            case "OTP_REQUIRED": 
                // Redirect user to set up a new password via OTP verification
                manager.switchScene("/Views/resetPassword.fxml");
                break;

            case "INVITATION_PENDING": 
                // Redirect user to complete account setup using the invitation code
                manager.switchScene("/Views/invitation.fxml");
                break;

            case "SETUP_REQUIRED": 
                // Redirect user to complete their profile setup
                manager.switchScene("/Views/setAcc.fxml");
                break;

            default:
                // Show error if the username doesn't exist in the database
                Utils.validateInput(usernameField, usernameLabel, "Username doesn't exist. Try again");
        }
    }

    /**
     * Handles the "Sign Up" hyperlink click event.
     * Switches the scene to the invitation page for new user registration.
     */
    public void handleSignup() {
        manager.switchScene("/Views/invitation.fxml");
    }

    /**
     * Handles the "Forgot Password" hyperlink click event.
     * Switches the scene to the OTP page for resetting the user's password.
     */
    public void handleForgotPassword() {
        manager.switchScene("/Views/otp.fxml");
    }

    /**
     * Displays the role selection screen if the user has multiple roles.
     * 
     * @param user The user object for whom the roles need to be selected.
     */
    private void showRoleSelectionScreen(User user) {
        manager.switchScene("/Views/selectrole.fxml");
        System.out.println("Multiple roles detected. Prompting user for role selection...");
    }
}
