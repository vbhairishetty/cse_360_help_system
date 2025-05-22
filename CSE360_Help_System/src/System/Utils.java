/**
 * The Utils class provides utility methods for input validation and displaying alerts in the JavaFX application.
 * This class includes static methods for validating user input fields, validating password requirements,
 * and showing alert dialogs to users.
 * 
 * <p>These utility methods are intended to be reusable across different parts of the application,
 * making validation and alert management easier to handle.</p>
 * 
 * @author YourName
 */
package System;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Role;

/**
 * Utils is a utility class that contains methods for validating user input and showing alert dialogs in JavaFX.
 */
public class Utils {
    
    /**
     * Validates the input in a TextField and displays an error message in a Label if the input is empty.
     * 
     * @param input        The TextField to validate.
     * @param label        The Label to display the error message.
     * @param labelMessage The message to display if the input is invalid.
     * @return True if the input is valid (i.e., not empty), false otherwise.
     */
    public static boolean validateInput(TextField input, Label label, String labelMessage) {
        if (input.getText().isEmpty()) {
            label.setText(labelMessage);
            label.setVisible(true);
            return false;
        }
        return true;
    }

    /**
     * Validates the passwords to ensure they meet the specified requirements:
     * at least 8 characters, including at least 1 numeric character, 1 lowercase character,
     * 1 special character, and 1 uppercase character.
     * 
     * @param password1    The TextField for the first password input.
     * @param password2    The TextField for the repeated password input.
     * @param errorLabel1  The Label to display the error message for the first password.
     * @param errorLabel2  The Label to display the error message for the repeated password.
     */
    private void validatePassword(TextField password1, TextField password2, Label errorLabel1, Label errorLabel2) {
        String password = password1.getText();
        String re_password = password2.getText();

        // Define password requirements
        boolean isValid1 = password.length() >= 8 && password.matches(".*\\d.*") && password.matches(".*[!@#$%^&*()].*");
        boolean isValid2 = re_password.length() >= 8 && re_password.matches(".*\\d.*") && re_password.matches(".*[!@#$%^&*()].*");

        if (isValid1) {
            errorLabel1.setVisible(false); // Hide error message if valid
        } else {
            errorLabel1.setText("Password does not meet requirements, please re-enter");
            errorLabel1.setVisible(true); // Show error message if invalid
        }
        
        if (isValid2) {
            errorLabel2.setVisible(false); // Hide error message if valid
        } else {
            errorLabel2.setText("Password does not meet requirements, please re-enter");
            errorLabel2.setVisible(true); // Show error message if invalid
        }
    }

    /**
     * Displays an alert dialog with a specified type, title, and message.
     * 
     * @param alertType The type of alert to display (e.g., ERROR, INFORMATION, WARNING).
     * @param title     The title of the alert dialog.
     * @param message   The message to be displayed in the alert dialog.
     */
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    public static void redirectUser(Role role) {
        SceneManager sceneManager = Singleton.getInstance().getManager();
        if (role != null) {
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
                case ADMIN:
                    sceneManager.switchScene("/Views/adminDash.fxml");
                    break;
                default:
                    sceneManager.switchScene("/Views/login.fxml");
                    break;
            }
        } else {
            sceneManager.switchScene("/Views/login.fxml");
        }
    }
}
