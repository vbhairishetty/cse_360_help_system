package System;

import model.Role;
import model.User;

/**
 * The SessionManager class is responsible for managing the current user session.
 * It provides static methods to handle user login, role assignment, invitation handling, and logout operations.
 *
 * <p>This class ensures that only one instance of the current session is maintained and
 * offers methods to retrieve and manipulate session data.</p>
 *
 * <p>Note: This is a utility class with a private constructor and all methods are static.</p>
 *
 * @author YourName
 */
public class SessionManager {

    /** The current user of the session. */
    private static User currentUser;

    /** The current role of the user in the session. */
    private static Role currentRole;

    /** The role assigned during the invitation process. */
    private static Role invitationRole;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SessionManager() {}

    /**
     * Logs in a user and sets the current session user.
     *
     * @param user The user to log in.
     */
    public static void login(User user) {
        currentUser = user;
        System.out.println("User " + user.getUsername() + " logged in.");
    }

    /**
     * Sets the current role for the logged-in user.
     *
     * @param role The role to set for the current user.
     * @throws IllegalStateException If no user is logged in.
     */
    public static void setCurrentRole(Role role) {
        if (currentUser == null) {
            throw new IllegalStateException("No user is logged in.");
        }
        currentRole = role;
        currentUser.setCurrentRole(role);
        System.out.println("Role set to " + role);
    }

    /**
     * Sets the role from the invitation during account setup.
     *
     * @param user The user receiving the invitation.
     * @param role The role assigned from the invitation.
     */
    public static void setInvitationRole(User user, Role role) {
        invitationRole = role; // Store the role from the invitation
        user.addRole(role); // Assign the invitation role to the user
        System.out.println("Invitation role set to " + role);
    }

    /**
     * Gets the invitation role, used during account setup.
     *
     * @return The role assigned during the invitation.
     */
    public static Role getInvitationRole() {
        return invitationRole;
    }

    /**
     * Gets the current logged-in user.
     *
     * @return The current user of the session, or null if no user is logged in.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Gets the current role of the logged-in user.
     *
     * @return The current role of the user, or null if no role is set.
     */
    public static Role getCurrentUserRole() {
        return currentRole;
    }

    /**
     * Checks if a user is logged in.
     *
     * @return True if a user is logged in, false otherwise.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Logs out the current user and clears the session.
     * Clears sensitive data from the user object.
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("User " + currentUser.getUsername() + " logged out.");
            currentUser.logout(); // Clear sensitive data from the user object
            currentUser = null;
            currentRole = null;
            invitationRole = null;
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    /**
     * Checks if the current user has multiple roles.
     *
     * @return True if the current user has more than one role, false otherwise.
     */
    public static boolean hasMultipleRoles() {
        return currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().size() > 1;
    }

    /**
     * Gets the list of roles for the current user.
     *
     * @return An array of roles for the current user, or an empty array if no user is logged in.
     */
    public static Role[] getUserRoles() {
        if (currentUser != null) {
            return currentUser.getRoles().toArray(new Role[0]);
        }
        return new Role[0];
    }
}
