package Controllers;

import java.sql.SQLException;

import System.SceneManager;
import System.Singleton;
import System.Utils;
import database.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller class for setting up user account details.
 * This class manages the user input for setting up their profile information.
 */
public class setAccount {

	// FXML UI components    
	@FXML
	public TextField userName;  // TextField for username

	@FXML
	private TextField firstName;  // TextField for first name

	@FXML
	private TextField middleName;  // TextField for middle name (optional)

	@FXML
	private TextField lastName;  // TextField for last name

	@FXML
	private TextField prefName;  // TextField for preferred name (optional)

	// Labels used to display validation messages for each field
	@FXML
	private Label usernameLabel;

	@FXML
	private Label firstNameLabel;

	@FXML
	private Label middleNameLabel;

	@FXML
	private Label lastNameLabel;

	@FXML
	private Label prefNameLabel;

	// DatabaseHandler instance for interacting with the database
	private DatabaseHandler dbHandler;
	private SceneManager manager;
	private String email;

	@FXML
	public void initialize() {
		dbHandler = Singleton.getInstance().getDbHandler();
		manager = Singleton.getInstance().getManager();
		email = Singleton.getInstance().getEmail();

	}

	/**
	 * Handles the action when the user clicks "Next" to set up their account.
	 * Validates the input fields, sets up the user profile, and moves to the next scene.
	 * 
	 * @throws SQLException if a database error occurs during profile setup.
	 */
	@FXML
	public void handleNext() throws SQLException {

		// Validate required input fields (username, first name, last name)
		if (!Utils.validateInput(userName, usernameLabel, "Username is Required") || 
				!Utils.validateInput(firstName, firstNameLabel, "First Name Required") || 
				!Utils.validateInput(lastName, lastNameLabel, "Last Name is Required")) {
			return;  // Return if validation fails
		}

		// Set up the user's profile in the database
		dbHandler.setupUserProfile(userName.getText(), email, firstName.getText(), lastName.getText(), middleName.getText(), prefName.getText());

		// Switch to the reset password screen
		Singleton.getInstance().getLoggedInUser().setUserName(userName.getText());
		manager.switchScene("/Views/resetpassword.fxml");
	}

	/**
	 * Logs out the current user.
	 * Note: The actual implementation is currently a placeholder.
	 */
	public void logout() {
		System.out.print("logout");
	}

	/**
	 * Closes the application.
	 * Note: The actual implementation is currently a placeholder.
	 */
	public void closeApp() {
		try {
			manager.loadLoginScene();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
