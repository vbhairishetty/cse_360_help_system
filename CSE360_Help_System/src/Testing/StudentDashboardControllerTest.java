package Testing;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;

import Controllers.StudentDashboardController;
import System.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.HelpArticle;
import model.User;
import database.DatabaseHandler;

public class StudentDashboardControllerTest extends ApplicationTest {

    private StudentDashboardController controller;
    private FxRobot robot;
 
    @Override
    public void start(Stage stage) throws Exception {
    	User user = new DatabaseHandler().getUser("User1");
    	SessionManager.login(user);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/studentDashboard.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        robot = new FxRobot();
    }

    @Test
    public void testHandleRefresh() {
        // Given
        TableView<HelpArticle> articlesTable = robot.lookup("#articlesTable").queryAs(TableView.class);
        int initialArticleCount = articlesTable.getItems().size();

        // When
        Button refreshButton = robot.lookup("#refreshButton").queryAs(Button.class);
        robot.clickOn(refreshButton);

        // Then
        int refreshedArticleCount = articlesTable.getItems().size();
        assertNotEquals(initialArticleCount, refreshedArticleCount, "The article count should change after refreshing.");
    }

    @Test
    public void testHandleLogout() {
        // Given
        Button logoutButton = robot.lookup("#logoutButton").queryAs(Button.class);

        // When
        robot.clickOn(logoutButton);

        // Then
        assertTrue(robot.lookup("#loginPane").tryQuery().isPresent(), "Logout should redirect to the login screen.");
    }
}
