package model;

import java.util.ArrayList;
import java.util.List;

/**
 * The Student class represents a user with a student role.
 * It extends the User class and adds specific functionality for students.
 */
public class Student extends User {
    private List<String> searchRequests;

    /**
     * Constructs a new Student object with a specified username and password.
     * 
     * @param userName the username of the student
     * @param password the password of the student
     */
    public Student(String userName, char[] password) {
        super(userName, password);
        this.addRole(Role.STUDENT);
        this.searchRequests = new ArrayList<>();
    }

    /**
     * Allows the student to ask a question.
     * 
     * @param question the question asked by the student
     */
    public void askQuestion(String question) {
        System.out.println("Student asked: " + question);
    }

    /**
     * Send a generic message to the help system.
     * 
     * @param message the generic message from the student
     */
    public void sendGenericMessage(String message) {
        System.out.println("Generic Message: " + message);
    }

    /**
     * Send a specific message to the help system.
     * 
     * @param message the specific message from the student
     */
    public void sendSpecificMessage(String message) {
        System.out.println("Specific Message: " + message);
        // Optionally, add this message to a request log for tracking purposes.
        searchRequests.add(message);
    }

    /**
     * Record a search request for analysis purposes.
     * 
     * @param request the search request made by the student
     */
    public void addSearchRequest(String request) {
        searchRequests.add(request);
    }

    /**
     * Get all the search requests made by the student.
     * 
     * @return a list of search requests
     */
    public List<String> getSearchRequests() {
        return searchRequests;
    }
}
