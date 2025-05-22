package Controllers;

import java.sql.SQLException;

import System.SceneManager;
import System.SessionManager;
import System.Singleton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller class for managing user logout functionality.
 * This class handles logging out the user and navigating back to the login scene.
 */
public class LogoutController {
	
	@FXML
	private Button addArticleBtn;
    
    // Instance of SceneManager to handle scene navigation
    private SceneManager manager = Singleton.getInstance().getManager();

    /**
     * Logs out the current user and loads the login scene.
     * This method clears the user's session data and navigates to the login screen.
     * 
     * @throws SQLException if there is an issue during the logout process.
     */
    @FXML
    public void logout() throws SQLException {
        // Perform the logout by clearing session details
        SessionManager.logout();
        
        // Load the login scene after successful logout
        manager.loadLoginScene();
    }
    
    @FXML
    public void showArticlePage() {
    	manager.switchScene("/Views/createArticle.fxml");
    }
}
