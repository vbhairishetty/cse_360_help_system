/**
 * The UserDatabase class serves as a singleton for managing user and invitation data.
 * It provides methods to add, find, and validate users and invitations within a centralized
 * in-memory database structure.
 * 
 * <p>This class is intended to be used as a single instance, accessed through the getInstance() method,
 * ensuring that all user data is managed centrally without duplicating instances.</p>
 * 
 * @author YourName
 */
package model;

import java.util.HashMap;
import java.util.Map;

/**
 * UserDatabase is a singleton class that manages a collection of User objects and Invitation objects.
 * It provides methods to add users, find users, add invitations, and validate invitations.
 */
public class UserDatabase {

    /** The single instance of UserDatabase. */
    private static UserDatabase instance;
    
    /** A map storing users by their usernames. */
    private Map<String, User> users = new HashMap<>();
    
    /** A map storing invitations by their unique codes. */
    private Map<String, Invitation> invitations = new HashMap<>();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private UserDatabase() {}

    /**
     * Returns the single instance of UserDatabase, creating it if necessary.
     * 
     * @return The instance of UserDatabase.
     */
    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    /**
     * Checks if the current user is the first user in the database.
     * 
     * @return True if there are no users in the database, false otherwise.
     */
    public boolean isFirstUser() {
        return users.isEmpty();
    }

    /**
     * Adds a user to the database.
     * 
     * @param user The user to add to the database.
     */
    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    /**
     * Finds a user in the database by their username.
     * 
     * @param username The username of the user to find.
     * @return The User object if found, or null if the user does not exist.
     */
    public User findUser(String username) {
        return users.get(username);
    }

    /**
     * Adds an invitation to the database.
     * 
     * @param invitation The invitation to add to the database.
     */
    public void addInvitation(Invitation invitation) {
        invitations.put(invitation.getCode(), invitation);
    }

    /**
     * Validates an invitation based on the invitation code and email address.
     * 
     * @param code  The unique code of the invitation.
     * @param email The email address associated with the invitation.
     * @return The Invitation object if the code and email match, or null if invalid.
     */
    public Invitation validateInvitation(String code, String email) {
        Invitation invitation = invitations.get(code);
        if (invitation != null && invitation.getUsername().equals(email)) {
            return invitation;
        }
        return null;
    }
}
