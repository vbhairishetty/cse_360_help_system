/**
 * The Invitation class represents an invitation sent to a user via email,
 * assigning them a specific role and providing an access code.
 * 
 * <p>This class is used for managing invitations to users for various purposes,
 * such as inviting them to join a platform or assigning them a role.</p>
 * 
 * @author YourName
 */
package model;

/**
 * Invitation is a model class that represents an invitation with details such as email, role, and code.
 */
public class Invitation {
    /** The email address to which the invitation is sent. */
    private String email;
    
    /** The role assigned to the invited user. */
    private Role role;
    
    /** The unique code associated with the invitation. */
    private String code;

    /**
     * Constructs a new Invitation with the specified email, role, and code.
     * 
     * @param email The email address to which the invitation is sent.
     * @param role  The role assigned to the invited user.
     * @param code  The unique code for the invitation.
     */
    public Invitation(String email, Role role, String code) {
        this.email = email;
        this.role = role;
        this.code = code;
    }

    /**
     * Gets the email address to which the invitation is sent.
     * 
     * @return The email address of the invited user.
     */
    public String getUsername() {
        return email;
    }

    /**
     * Gets the role assigned to the invited user.
     * 
     * @return The role assigned to the invited user.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Gets the unique code associated with the invitation.
     * 
     * @return The unique code for the invitation.
     */
    public String getCode() {
        return code;
    }
}
