package model;

/**
 * Represents a Teaching Assistant (TA) user, extending the base User class.
 * A TA is automatically assigned the TA role upon creation.
 */
public class TA extends User {

    /**
     * Constructs a new TA with the specified username and password.
     * The TA role is automatically added to this user.
     *
     * @param userName the username for the TA
     * @param password the password for the TA, represented as a char array
     */
    public TA(String userName, char[] password) {
        super(userName, password);
        this.addRole(Role.TA);
    }

    /**
     * Assigns a token to another TA user. This is a TA-specific functionality.
     *
     * @param ta the TA user to whom the token is being assigned
     */
    public void assignTokenToAnotherTA(User ta) {
        System.out.println("Assigned token to " + ta.getUsername());
    }
}