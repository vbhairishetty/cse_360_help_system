/**
 * The Singleton class is responsible for maintaining a single instance of critical application components,
 * including the database handler, connection, and scene manager.
 * 
 * <p>This class follows the Singleton design pattern, ensuring that only one instance of this class
 * is created and shared throughout the application.</p>
 * 
 * <p>The Singleton class provides global access to its instance via the {@code getInstance()} method,
 * and it ensures that the database connection is maintained and accessible as needed.</p>
 * 
 * @author YourName
 */
package System;

import java.sql.Connection;
import java.sql.SQLException;

import database.DatabaseHandler;
import model.User;

/**
 * Singleton is a class that ensures only one instance of the key components of the application is created.
 * It holds references to the DatabaseHandler, Connection, SceneManager, and other session-level data.
 */
public class Singleton {
    
    /** The single instance of Singleton. */
    private static volatile Singleton instance;
    
    /** The single instance of DatabaseHandler for database operations. */
    private static volatile DatabaseHandler dbHandler;
    
    /** The single instance of the database connection. */
    private static volatile Connection connection;
    
    /** The single instance of SceneManager for managing application scenes. */
    private static volatile SceneManager sceneManager;
    
    /** The currently logged-in user. */
    private User loggedInUser;
    
    /** The username of the logged-in user. */
    private String username;
    
    /** The current user's email address used to validate otp and invitation */
    private String email;

    /**
     * Private constructor to prevent instantiation from other classes.
     * Initializes the DatabaseHandler, connection, and SceneManager.
     */
    private Singleton() {
        try {
            // Create only one instance of DatabaseHandler and connection
            sceneManager = new SceneManager();
            dbHandler = new DatabaseHandler();
            connection = dbHandler.getConnection();
            loggedInUser = SessionManager.getCurrentUser();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error initializing DatabaseHandler or creating connection.");
            dbHandler = null;
        }
    }

    /**
     * Public method to provide access to the singleton instance.
     * Uses double-checked locking to ensure thread safety.
     * 
     * @return The instance of Singleton.
     */
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    /**
     * Gets the DatabaseHandler instance, ensuring the connection is active.
     * 
     * @return The instance of DatabaseHandler.
     */
    public DatabaseHandler getDbHandler() {
    	
    	if (dbHandler == null) {
            System.out.println("DatabaseHandler is not initialized. Please check the connection settings.");
            return null;  // Return null or handle this case as needed
        }
        try {
            dbHandler.ensureConnection(); // Ensure the connection is active before returning the handler
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to ensure database connection.");
        }
        return dbHandler;
    }

    /**
     * Gets the database connection instance.
     * 
     * @return The database connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Gets the SceneManager instance for managing scenes.
     * 
     * @return The SceneManager instance.
     */
    public SceneManager getManager() {
        return sceneManager;
    }

    /**
     * Gets the currently logged-in user.
     * 
     * @return The logged-in user.
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Sets the logged-in user.
     * 
     * @param loggedInUser The user to set as the logged-in user.
     */
    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

 
    /**
     * Sets the username of the logged-in user.
     * 
     * @param username The username of the logged-in user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the username of the logged-in user.
     * 
     * @return The username of the logged-in user.
     */
    public String getEmail() {
        return email;
    }

	public Object getSessionManager() {
		// TODO Auto-generated method stub
		return null;
	}
}
