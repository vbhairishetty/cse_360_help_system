/**
 * The SceneManager class is responsible for managing scenes in the JavaFX application.
 * It includes methods to load the initial login scene, switch between different scenes,
 * and navigate based on the user's role.
 * 
 * <p>This class extends the Application class from JavaFX, and it serves as the entry point
 * for the GUI application "Compass 360".</p>
 * 
 * @author YourName
 */
package System;

import java.sql.SQLException;

import database.DatabaseHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Role;

/**
 * SceneManager is a class that handles loading and switching between different scenes in the JavaFX application.
 * It manages the primary stage and provides utility methods for navigating the user interface.
 */
public class SceneManager extends Application {

    /** The primary stage used to display scenes. */
    private static Stage primaryStage;

    /**
     * The start method is the entry point for the JavaFX application.
     * It sets up the primary stage and loads the initial login scene.
     * 
     * @param stage The primary stage for this application.
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Compass 360");

        // Load the initial scene
        try {
            loadLoginScene();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The main method launches the JavaFX application.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Loads the login scene based on whether the user table is empty.
     * If the user table is empty, the admin creation scene is loaded, otherwise the login scene is loaded.
     * 
     * @throws SQLException If a database access error occurs.
     */
    public void loadLoginScene() throws SQLException {
        DatabaseHandler dbHandler = null;
        try {
            dbHandler = Singleton.getInstance().getDbHandler();
            String filename;

            // Load the FXML for the login page
            if (dbHandler.isUserTableEmpty()) {
                filename = "/Views/adminCreation.fxml";
            } else {
                filename = "/Views/Login.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(filename));
            Parent root = loader.load();

            // Create a new scene
            Scene scene = new Scene(root);

            // Set the scene on the primary stage
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Switches to another scene based on the provided FXML file path.
     * 
     * @param fxmlFilePath The path to the FXML file to load.
     */
    public void switchScene(String fxmlFilePath) {
        try {
            // Load the new FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
            Parent root = loader.load();

            // Create a new scene
            Scene scene = new Scene(root);

            // Set the scene on the primary stage
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Switches the scene based on the user's role. This method directs users to their respective homepages.
     * 
     * @param role The role of the user (ADMIN, INSTRUCTOR, TA, STUDENT).
     */
    public void switchSceneBasedOnRole(Role role) {
        // Navigate to the homepage based on the role
        switch (role) {
            case ADMIN:
                System.out.println("Redirecting to Admin dashboard...");
                this.switchScene("/Views/adminDash.fxml"); // Load Admin homepage
                break;
            case INSTRUCTOR:
                System.out.println("Redirecting to Instructor homepage...");
                this.switchScene("/Views/instructorDashboard.fxml"); // Load Instructor homepage
                break;
            case TA:
                System.out.println("Redirecting to TA homepage...");
                this.switchScene("/Views/taDashboard.fxml"); // Load TA homepage
                break;
            case STUDENT:
                System.out.println("Redirecting to Student homepage...");
                this.switchScene("/Views/studentDashboard.fxml"); // Load Student homepage
                break;
            case PAST_STUDENT:
            	System.out.println("Redirecting to Past Student Homepage.");
            	this.switchScene("/Views/exStudentDashboard.fxml");
            	break;
            default:
            	System.out.println("Something went wrong, try again..");
            	this.switchScene("/Views/Login.fxml");
            	break;
        }
    }
}
