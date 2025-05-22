package Controllers;

import System.SceneManager;
import System.SessionManager;
import System.Singleton;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import model.Role;

public class DashboardController {

    @FXML
    public void initialize() {
        // Get the current user's role
        Role currentRole = SessionManager.getCurrentUserRole();

        // Check if the user role matches the expected role for this dashboard
        if (currentRole != Role.STUDENT) { // Example: Only STUDENT role should access this dashboard
            showAlert("Access Denied", "You do not have permission to access this dashboard.");
            // Redirect the user to their correct dashboard or log them out
            redirectUser(currentRole);
        }
    }

    private void redirectUser(Role role) {
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
