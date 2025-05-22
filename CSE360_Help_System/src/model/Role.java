package model;

/**
 * Enum representing different roles within the system.
 * Each role is associated with a specific role ID.
 */
public enum Role {
    
    /** Admin role with role ID 1. */
    ADMIN(1),

    /** Instructor role with role ID 2. */
    INSTRUCTOR(2),

    /** Teaching Assistant role with role ID 3. */
    TA(3),

    /** Student role with role ID 4. */
    STUDENT(4),

    /** Past Student role with role ID 5. */
    PAST_STUDENT(5);
    
    /** The unique ID assigned to each role. */
    private final int roleId;

    /**
     * Constructor to assign a specific ID to each role.
     * 
     * @param roleId the ID assigned to the role
     */
    Role(int roleId) {
        this.roleId = roleId;
    }

    /**
     * Retrieves the role ID associated with the role.
     * 
     * @return the role ID
     */
    public int getRoleId() {
        return roleId;
    }
}