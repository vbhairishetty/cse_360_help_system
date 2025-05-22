package Controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import System.SessionManager;
import System.Singleton;
import database.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.HelpArticle;
import model.Role;

public class RestoreBackupController {

    @FXML
    private Button chooseFileBtn;

    @FXML
    private Button executeBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private RadioButton restoreRadio;

    @FXML
    private RadioButton backupRadio;

    @FXML
    private Label fileLabel;

    @FXML
    private ToggleGroup toggleGroup;

    private File selectedFile;
    private DatabaseHandler dbHandler;

    @FXML
    public void initialize() {
        dbHandler = Singleton.getInstance().getDbHandler();
    }

    @FXML
    public void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Data Files", "*.dat"));
        selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            fileLabel.setText(selectedFile.getName());
        } else {
            fileLabel.setText("No file chosen");
        }
    }

    @FXML
    public void handleExecuteAction() {
        if (selectedFile == null) {
            showAlert("Please choose a file before executing an action.");
            return;
        }

        Role currentUserRole = SessionManager.getCurrentUserRole();

        if (restoreRadio.isSelected()) {
            handleRestore(currentUserRole);
        } else if (backupRadio.isSelected()) {
            handleBackup(currentUserRole);
        } else {
            showAlert("Please select either Restore or Backup.");
        }
    }

    private void handleRestore(Role userRole) {
        if (!isAuthorized(userRole)) {
            showAlert("You do not have permission to perform a restore.");
            return;
        }

        try {
            dbHandler.restoreHelpSystemData(Collections.singletonList(selectedFile.getAbsolutePath()), false, userRole, Singleton.getInstance().getLoggedInUser().getUsername());
            showAlert("Restore completed successfully!");
        } catch (SQLException | IOException | ClassNotFoundException e) {
            showAlert("Error during restore: " + e.getMessage());
        }
    }

    private void handleBackup(Role userRole) {
    	List<String> nullList = null;
        if (!isAuthorized(userRole)) {
            showAlert("You do not have permission to perform a backup.");
            return;
        }

        try {
            // Assuming user-authored articles are tagged in some way, retrieve only those
            List<HelpArticle> articlesToBackup = dbHandler.searchHelpArticles("", "", null, SessionManager.getCurrentUser().getUsername(), userRole);
            dbHandler.backupHelpSystemData(selectedFile.getAbsolutePath(), nullList, userRole, null);
            showAlert("Backup completed successfully!");
        } catch (SQLException | IOException e) {
            showAlert("Error during backup: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        //navigate to a previous screen
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private boolean isAuthorized(Role userRole) {
        return userRole == Role.ADMIN || userRole == Role.INSTRUCTOR;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
