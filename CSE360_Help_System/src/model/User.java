/**
 * The User class represents a user entity with various personal details, credentials,
 * and roles that define the user's access within the system. This class includes methods
 * to manage user attributes, roles, and sensitive data, such as passwords and OTPs.
 * 
 * <p>This class serves as a model for storing and managing user information, including role
 * management and secure handling of sensitive data.</p>
 * 
 * @author YourName
 */
package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User is a model class that represents a user in the system with details such as username, password,
 * personal information, OTP, and roles. This class includes methods for managing user details, roles,
 * and secure password handling.
 */
public class User {
    /** The username of the user. */
    private String userName;
    
    /** The email address of the user. */
    private String email;
    
    /** The first name of the user. */
    private String firstName;
    
    /** The last name of the user. */
    private String lastName;
    
    /** The preferred name of the user. */
    private String prefName = null;
    
    /** The middle name of the user. */
    private String midName = null;
    
    /** The password of the user, stored as a character array for security purposes. */
    protected char[] password;
    
    /** One-time password (OTP) for verification purposes. */
    private String otp;
    
    /** Expiration time of the OTP. */
    private LocalDateTime otpExpiration;
    
    /** The invitation code associated with the user. */
    private String invitation;
    
    /** List of roles assigned to the user. */
    private List<Role> roles = new ArrayList<>();
    
    /** The current role of the user. */
    private Role currentRole;

    /**
     * Gets the OTP expiration time.
     * 
     * @return The expiration time of the OTP.
     */
    public LocalDateTime getOtpExpiration() {
        return otpExpiration;
    }

    /**
     * Sets the OTP expiration time.
     * 
     * @param otpExpiration The expiration time to set for the OTP.
     */
    public void setOtpExpiration(LocalDateTime otpExpiration) {
        this.otpExpiration = otpExpiration;
    }

    /**
     * Constructs a User with the specified username and password.
     * 
     * @param userName The username of the user.
     * @param password The password of the user.
     */
    public User(String userName, char[] password) {
        this.userName = userName;
        this.password = password;
        this.roles = new ArrayList<>();
    }

    /**
     * Constructs a User with the specified username, invitation code, and roles.
     * 
     * @param username   The username of the user.
     * @param invitation The invitation code for the user.
     * @param roles      The list of roles assigned to the user.
     */
    public User(String username, String invitation, List<Role> roles) {
        this.userName = username;
        this.invitation = invitation;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }

    /**
     * Default constructor for User.
     */
    public User() {
        this.roles = new ArrayList<>();
    }

    /**
     * Creates a user with detailed information.
     * 
     * @param username   The username of the user.
     * @param firstName  The first name of the user.
     * @param lastName   The last name of the user.
     * @param middleName The middle name of the user.
     * @param prefName   The preferred name of the user.
     */
    public void createUser(String username, String firstName, String lastName, String middleName, String prefName) {
        this.userName = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.midName = middleName;
        this.prefName = prefName;
    }

    /**
     * Gets the email address of the user.
     * 
     * @return The email address of the user.
     */
    public String getEmail() { return this.email; }

    /**
     * Sets the email address of the user.
     * 
     * @param email The email address to set.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the username of the user.
     * 
     * @return The username of the user.
     */
    public String getUsername() { return this.userName; }

    /**
     * Sets the username of the user.
     * 
     * @param userName The username to set.
     */
    public void setUserName(String userName) { this.userName = userName; }

    /**
     * Gets the first name of the user.
     * 
     * @return The first name of the user.
     */
    public String getFirstName() { return this.firstName; }

    /**
     * Sets the first name of the user.
     * 
     * @param firstName The first name to set.
     */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /**
     * Gets the last name of the user.
     * 
     * @return The last name of the user.
     */
    public String getLastName() { return this.lastName; }

    /**
     * Sets the last name of the user.
     * 
     * @param lastName The last name to set.
     */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /**
     * Gets the middle name of the user.
     * 
     * @return The middle name of the user.
     */
    public String getMidName() { return this.midName; }

    /**
     * Sets the middle name of the user.
     * 
     * @param midName The middle name to set.
     */
    public void setMidName(String midName) { this.midName = midName; }

    /**
     * Gets the preferred name of the user.
     * 
     * @return The preferred name of the user.
     */
    public String getPrefName() { return this.prefName; }

    /**
     * Sets the preferred name of the user.
     * 
     * @param prefName The preferred name to set.
     */
    public void setPrefName(String prefName) { this.prefName = prefName; }

    /**
     * Gets the password of the user.
     * 
     * @return The password of the user.
     */
    public char[] getPassword() { return password; }

    /**
     * Sets the password of the user and clears the previous password securely.
     * 
     * @param password The new password to set.
     */
    public void setPassword(char[] password) {
        clearPassword();
        this.password = password;
    }

    /**
     * Clears the password by overwriting the existing character array.
     */
    public void clearPassword() {
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = '\0';  // Clear the password by overwriting it
            }
        }
    }

    /**
     * Gets the list of roles assigned to the user.
     * 
     * @return The list of roles assigned to the user.
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the list of roles assigned to the user.
     * 
     * @param roles The list of roles to set.
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }

    /**
     * Adds a role to the user's list of roles if it is not already present.
     * 
     * @param role The role to add.
     */
    public void addRole(Role role) {
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }

    /**
     * Removes a role from the user's list of roles if it exists.
     * 
     * @param selectedRole The role to remove.
     */
    public void removeRole(Role selectedRole) {
        if (!roles.isEmpty() && roles.contains(selectedRole)) {
            roles.remove(selectedRole);
        }
    }

    /**
     * Gets the current role of the user.
     * 
     * @return The current role of the user.
     */
    public Role getCurrentRole() {
        return currentRole;
    }

    /**
     * Sets the current role of the user.
     * 
     * @param role The role to set as the current role.
     */
    public void setCurrentRole(Role role) {
        this.currentRole = role;
    }

    /**
     * Checks if the user has the specified role.
     * 
     * @param role The role to check.
     * @return True if the user has the role, false otherwise.
     */
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    /**
     * Gets the roles of the user as a comma-separated string.
     * 
     * @return The roles of the user as a string.
     */
    public String getRolesAsString() {
        return roles.stream()
                    .map(Role::name)  // Convert each role to a string
                    .collect(Collectors.joining(", "));
    }

    /**
     * Logs the user out by clearing sensitive data and resetting the current role.
     */
    public void logout() {
        clearPassword();  // Clear password on logout
        currentRole = null;  // Reset the role
        System.out.println(userName + " has been logged out.");
    }

    /**
     * Gets the one-time password (OTP) for the user.
     * 
     * @return The OTP for the user.
     */
    public String getOtp() {
        return this.otp;
    }

    /**
     * Sets the one-time password (OTP) for the user.
     * 
     * @param otp The OTP to set.
     */
    public void setOtp(String otp) {
        this.otp = otp;
    }

    /**
     * Gets the invitation code associated with the user.
     * 
     * @return The invitation code for the user.
     */
    public String getInvitation() {
        return this.invitation;
    }

    /**
     * Sets the invitation code associated with the user.
     * 
     * @param invite The invitation code to set.
     */
    public void setInvitation(String invite) {
        this.invitation = invite;
    }
}
