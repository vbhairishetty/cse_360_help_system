package Controllers;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.time.LocalDateTime;
import java.util.Random;

import System.SceneManager;
import System.Singleton;
import database.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import model.User;

/**
 * Controller class for sending OTP to users for password reset.
 * This class handles sending an OTP to a user based on their email address.
 */
public class sendOtp {

    // FXML UI components
    @FXML
    private TextField emailField;

    @FXML
    private Button sendOtpBtn;
    
    @FXML
    private Hyperlink cancelBtn;

    // Database handler for interacting with the database
    private DatabaseHandler dbHandler;

    // SceneManager for scene transitions
    public SceneManager manager;

    /**
     * Initializes the sendOtp controller.
     * Sets up necessary instances for database connection and scene management.
     */
    @FXML
    private void initialize() {
        manager = new SceneManager();
        dbHandler = Singleton.getInstance().getDbHandler();
    }

    /**
     * Handles the action of sending OTP to the user.
     * Validates the email address, retrieves the user, generates OTP, and updates the user account.
     */
    @FXML
    public void handleSendOtp() {
        String email = emailField.getText().trim();

        // Validate if the email field is empty
        if (email.isEmpty()) {
            showAlert("Please enter an email address.");
            return;
        }

        try {
            // Retrieve the user from the database using the provided email
            User user = dbHandler.getUserByEmail(email);
            if (user == null) {
                showAlert("No user found with the provided email address.");
                return;
            }

            // Set the username in Singleton for session management
            Singleton.getInstance().getLoggedInUser().setUserName(user.getUsername());

            // Generate OTP and set expiration time
            String otp = generateOtp();
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10); // OTP expires in 10 minutes

            // Update the user account with OTP and expiration time
            dbHandler.resetUserAccount(user.getUsername(), otp, expirationTime);
            showAlert("OTP sent successfully to: " + email);
            System.out.println("OTP for user " + user.getUsername() + ": " + otp);

            // Switch to OTP validation screen
            manager.switchScene("/Views/validateOtp.fxml");

        } catch (SQLNonTransientConnectionException e) {
            dbHandler.reconnect(); // Reconnect to the database if the connection was lost
        } catch (SQLException e) {
            showAlert("Error sending OTP: " + e.getMessage());
        }
    }

    /**
     * Handles the cancel action and redirects to the login scene.
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
     * Generates a 6-digit OTP.
     *
     * @return A string representing the generated OTP.
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate a 6-digit OTP
        return String.valueOf(otp);
    }

    /**
     * Utility method to show alerts.
     * This method is used to provide feedback to the user.
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
