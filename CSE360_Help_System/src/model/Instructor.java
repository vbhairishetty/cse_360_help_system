package model;

/**
 * Represents an instructor user, inheriting from the User class.
 * Instructors have a role of INSTRUCTOR and can grade assignments.
 */
public class Instructor extends User {

    /**
     * Constructor for creating an Instructor with a username and password.
     * Adds the INSTRUCTOR role to the user.
     *
     * @param userName the username of the instructor
     * @param password the password of the instructor
     */
    public Instructor(String userName, char[] password) {
        super(userName, password);
        this.addRole(Role.INSTRUCTOR);
    }
    
    /**
     * Grades an assignment for a student.
     *
     * @param studentName the name of the student being graded
     * @param grade the grade assigned to the student
     */
    public void gradeAssignment(String studentName, int grade) {
        System.out.println("Instructor graded " + studentName + ": " + grade);
    }
}