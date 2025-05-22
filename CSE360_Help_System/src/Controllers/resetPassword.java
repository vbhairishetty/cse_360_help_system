package Controllers;

import java.sql.SQLException;

import System.SceneManager;
import System.Singleton;
import database.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

/**
 * Controller class for resetting the user's password.
 * This class allows users to set a new password after a reset request.
 */
public class resetPassword {

	// UI elements for password fields, labels, and buttons
	@FXML
	private PasswordField passwordField1;

	@FXML
	private PasswordField passwordField2;

	@FXML
	private Button changePasswordBtn;

	@FXML
	private Label passwordLabel;

	// Instance variables for managing database and scene transitions
	private DatabaseHandler dbHandler;
	private String username; // Username of the user resetting the password
	private SceneManager manager;

	/**
	 * Initializes the reset password controller.
	 * This method sets up initial configurations, including retrieving user information and configuring the UI.
	 */
	@FXML
	private void initialize() {
		// Hide the error/success label initially
		passwordLabel.setVisible(false);

		// Retrieve the logged-in username from the Singleton
		username = Singleton.getInstance().getLoggedInUser().getUsername();

		// Get the database handler and scene manager instances
		dbHandler = Singleton.getInstance().getDbHandler();
		manager = new SceneManager();
	}

	/**
	 * Handles the action to change the user's password.
	 * This method checks the validity of input fields, ensures both passwords match,
	 * and then updates the password in the database.
	 */
	@FXML
	public void changePassword() {
		// Get the passwords entered by the user
		String password1 = passwordField1.getText();
		String password2 = passwordField2.getText();

		// Validate if both fields are filled
		if (password1.isEmpty() || password2.isEmpty()) {
			showAlert("Please fill in both password fields.");
			return;
		}

		// Check if the passwords match
		if (!password1.equals(password2)) {
			showAlert("Passwords do not match. Please try again.");
			return;
		}

		try {
			// Update the user's password in the database
			dbHandler.updateUserPassword(username, password2);
			showAlert("Password updated successfully.");

			// Redirect the user back to the login screen
			manager.loadLoginScene();
		} catch (SQLException e) {
			// Handle database errors during the password update process
			showAlert("Error updating password: " + e.getMessage());
		}
	}

	/**
	 * Handles the cancel action, which navigates the user back to the login screen.
	 * This is useful when the user decides not to proceed with resetting their password.
	 */
	@FXML
	public void handleCancel() {
		try {
			manager.loadLoginScene();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Utility method to show alerts on the passwordLabel.
	 * This is used to provide feedback to the user about validation errors or success messages.
	 *
	 * @param message The message to be displayed.
	 */
	private void showAlert(String message) {
		passwordLabel.setText(message);
		passwordLabel.setVisible(true);
	}
}
