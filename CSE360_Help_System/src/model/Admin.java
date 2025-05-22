package model;

import java.util.List;

/**
 * The Admin class represents a type of user with administrative privileges.
 * It extends the {@link User} class and adds functionalities specific to
 * administrators, such as inviting users and resetting passwords.
 * 
 * <p>This class is part of a system where different roles are assigned
 * to users, and Admins have the ability to manage other users.</p>
 * 
 * @see User
 */
public class Admin extends User {

    /**
     * Constructs an Admin object with the specified username and password.
     * The admin role is automatically added to the user.
     *
     * @param userName the username of the admin
     * @param password the password of the admin
     */
    public Admin(String userName, char[] password) {
        super(userName, password);
        this.addRole(Role.ADMIN);
    }
    
    /**
     * Invites a user to the system. This simulates an admin inviting a user.
     * 
     * @param user the user being invited
     */
    public void inviteUser(User user) {
        System.out.println("Admin has invited " + user.getUsername());
    }

    /**
     * Resets the password for the specified user.
     * 
     * @param user the user whose password is being reset
     * @param newPassword the new password to set for the user
     */
    public void resetUserPassword(User user, char[] newPassword) {
        user.setPassword(newPassword);
        System.out.println("Password reset for " + user.getUsername());
    }
    
    /**
     * Lists all users in the system.
     * This is a placeholder method, assuming a UserManager class exists
     * to manage users.
     * 
     * @return a list of all users
     */
    public List<User> listAllUsers() {
        // Logic to list all users
        return null; // placeholder
    }
}