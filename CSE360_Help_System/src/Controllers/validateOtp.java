package Controllers;

import java.sql.SQLException;
import java.time.LocalDateTime;

import System.SceneManager;
import System.Singleton;
import database.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.User;

/**
 * Controller class for OTP validation.
 * This class manages the OTP verification process for users who need to reset their password.
 */
public class validateOtp {
    
    // FXML UI components
    @FXML
    private TextField usernameField; // TextField for entering the username
    
    @FXML
    private TextField otpField; // TextField for entering the OTP
    
    @FXML
    private Button submitBtn; // Button for submitting the OTP
    
    @FXML
    private Label otpLabel; // Label to display error messages or validation prompts
    
    @FXML
    private Hyperlink cancelLink; // Hyperlink to cancel OTP validation and return to login

    // DatabaseHandler and SceneManager instances for database operations and scene transitions
    private DatabaseHandler dbHandler;
    private SceneManager sceneManager;

    /**
     * Initializes the controller. Sets up the scene manager and database handler,
     * and hides the OTP label initially.
     */
    @FXML
    public void initialize() {
        otpLabel.setVisible(false); // Hide the OTP label initially
        sceneManager = new SceneManager(); // Instantiate SceneManager
        
        dbHandler = Singleton.getInstance().getDbHandler(); // Get DatabaseHandler from Singleton
    }

    /**
     * Handles the OTP submission event.
     * If the OTP is verified successfully, it navigates to the reset password screen.
     */
    public void submitOtp() {
        if (verifyOtp(usernameField.getText(), otpField.getText())) {
            navigateToResetPassword(); // Navigate to reset password page if OTP verification is successful
        }
    }

    /**
     * Handles the cancel event, which navigates back to the login screen.
     * 
     * @throws SQLException if an error occurs during the navigation to the login screen.
     */
    public void handleCancel() throws SQLException {
        sceneManager.loadLoginScene(); // Load the login scene
    }

    /**
     * Verifies if the provided OTP is valid for the given username.
     * 
     * @param username the username for which the OTP is to be verified.
     * @param otp the one-time password provided by the user.
     * @return true if the OTP is valid, false otherwise.
     */
    public boolean verifyOtp(String username, String otp) {
        try {
            // Get user details from the database
            User user = dbHandler.getUser(username);
            Singleton.getInstance().getLoggedInUser().setUserName(username); // Store the username in Singleton for further use
            
            if (user == null) {
                showAlert("No user found with the provided username.");
                return false;
            }

            String storedOtp = user.getOtp();
            LocalDateTime otpExpiration = user.getOtpExpiration();

            // Check if OTP and expiration time are assigned
            if (storedOtp == null || otpExpiration == null) {
                showAlert("OTP is not assigned or has expired.");
                return false;
            }

            // Check if OTP has expired
            if (LocalDateTime.now().isAfter(otpExpiration)) {
                showAlert("OTP has expired.");
                return false;
            }

            // Check if OTP matches
            if (!storedOtp.equals(otp)) {
                showAlert("Invalid OTP.");
                return false;
            }

            return true; // OTP is valid
        } catch (SQLException e) {
            showAlert("Error verifying OTP: " + e.getMessage());
            return false; // Return false in case of SQL error
        }
    }

    /**
     * Utility method to show alert messages using the OTP label.
     * 
     * @param message the message to be displayed.
     */
    private void showAlert(String message) {
        otpLabel.setText(message); // Set the message text
        otpLabel.setVisible(true); // Make the OTP label visible
    }

    /**
     * Navigates to the reset password page if OTP verification is successful.
     */
    private void navigateToResetPassword() {
        sceneManager.switchScene("/Views/resetPassword.fxml"); // Switch to reset password scene
    }
}
