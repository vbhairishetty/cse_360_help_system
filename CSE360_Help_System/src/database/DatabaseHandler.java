package database;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import model.HelpArticle;
import model.Message;
import model.Role;
import model.User;

public class DatabaseHandler {
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	static final String DATABASE_NAME = "compass360db";
	static final String JDBC_URL = "jdbc:mysql://localhost:3306/compass360db";

	private static final String JDBC_USERNAME = "root"; 
	private static final String JDBC_PASSWORD =  "Sai@18122004";
	
	private static final String ENCRYPTION_KEY = "testEncryptionKe";

	private Connection connection;
	private static DatabaseHandler instance;

	// Connect to MySQL server first (no specific database)
    /**
     * Constructor initializes the connection to the database and ensures the
     * required tables are created if they don't exist.
     *
     * @throws SQLException if unable to connect or create tables.
     */
	public DatabaseHandler() throws SQLException {
        try {
            // Connect to MySQL server first (no specific database)
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            this.connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
            System.out.println("Connected to MySQL server.");

            createDatabase("compass360db");

            // Reconnect to the newly created or existing database
            this.connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
            System.out.println("Connected to the 'compass360db' database.");

            createUsersTable();
            createHelpArticlesTable();
            createGroupsTable();
            createHelpArticlesGroupsTable();
            createGroupAdminsTable();
            createGroupInstructorsTable();
            createGroupStudentsTable();
            createSpecialAccessRightsTable();
            createArticleRolePermissionsTable();
            createSpecialAccessGroupRightsTable();
            createMessagesTable();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error connecting to database.");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
	
	public static synchronized DatabaseHandler getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

	
	public Connection getConnection() {
		return this.connection;
	}

	// Method to create a new database if it doesn't exist
	private void createDatabase(String dbName) throws SQLException {
		String createDbSQL = "CREATE DATABASE IF NOT EXISTS " + dbName;
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(createDbSQL);
			System.out.println("Database '" + dbName + "' created or already exists.");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Error creating database.");
		}
	}

	/**
     * Creates the 'users' table if it does not already exist. This table stores
     * user account details and roles.
     *
     * @throws SQLException if unable to execute table creation.
     */
    public void createUsersTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "userName VARCHAR(255) PRIMARY KEY, "
                + "email VARCHAR(255),"
                + "firstName VARCHAR(255), "
                + "lastName VARCHAR(255), "
                + "prefName VARCHAR(255), "
                + "midName VARCHAR(255), "
                + "password BLOB, "
                + "otp VARCHAR(255), "
                + "otpExpiration DATETIME, "
                + "invitation VARCHAR(255), "
                + "roles VARCHAR(255) NOT NULL, "
                + "currentRole VARCHAR(255), "
                + "topic VARCHAR(255)"
                + ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table 'users' created or already exists.");
        }
    }
    
    
    public void createSpecialAccessRightsTable() throws SQLException{
    	String query = "CREATE TABLE IF NOT EXISTS special_access_group_rights (\n"
    			+ "    group_id BIGINT NOT NULL,\n"
    			+ "    user_name VARCHAR(255) NOT NULL,\n"
    			+ "    has_admin_rights BOOLEAN DEFAULT FALSE,\n"
    			+ "    can_view_body BOOLEAN DEFAULT FALSE,\n"
    			+ "    role_type ENUM('ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL,\n"
    			+ "    PRIMARY KEY (group_id, user_name),\n"
    			+ "    FOREIGN KEY (group_id) REFERENCES article_groups(group_id),\n"
    			+ "    FOREIGN KEY (user_name) REFERENCES users(userName)\n"
    			+ ");\n"
    			+ "";
    	try(Statement stmt = connection.createStatement()){
    		stmt.execute(query);
    		System.out.println("Table 'special_access_group_rights' created or already exists.");
    	}
    }

    /**
     * Creates the 'help_articles' table if it does not already exist. This table
     * stores information about help articles for the help system.
     *
     * @throws SQLException if unable to execute table creation.
     */
    public void createHelpArticlesTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS help_articles ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "header VARCHAR(255), "
                + "article_level ENUM('beginner', 'intermediate', 'advanced', 'expert'), "
                + "title VARCHAR(255), "
                + "short_description TEXT, "
                + "keywords TEXT, "
                + "body TEXT, "
                + "encrypted_body BLOB, "
                + "links TEXT, "
                + "sensitive_title VARCHAR(255), "
                + "sensitive_description TEXT, "
                + "groups_col VARCHAR(255), "
                + "access_level ENUM('PUBLIC', 'RESTRICTED', 'CONFIDENTIAL') DEFAULT 'PUBLIC', "
                + "created_by VARCHAR(255), "
                + "is_special_access BOOLEAN DEFAULT FALSE, "
                + "allowed_roles TEXT, "
                + "UNIQUE (title, header) "
                + ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table 'help_articles' created or already exists.");
        }
    }

    // Create the article_groups table to handle grouping of articles
    public void createGroupsTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS article_groups ("
                + "group_id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(255) UNIQUE NOT NULL, "
                + "is_special_access BOOLEAN DEFAULT FALSE"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
            System.out.println("Table 'article_groups' created or already exists.");
        }
    }

    // Create the help_article_groups table to link articles to groups
    public void createHelpArticlesGroupsTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS help_article_groups ("
                + "article_id BIGINT NOT NULL, "
                + "group_id BIGINT NOT NULL, "
                + "PRIMARY KEY (article_id, group_id), "
                + "FOREIGN KEY (article_id) REFERENCES help_articles(id), "
                + "FOREIGN KEY (group_id) REFERENCES article_groups(group_id)"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
            System.out.println("Table 'help_article_groups' created or already exists.");
        }
    }

    // Create the group_admins table to store admin rights for groups
    public void createGroupAdminsTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS group_admins ("
                + "group_id BIGINT NOT NULL, "
                + "admin_id VARCHAR(255) NOT NULL, "
                + "is_special_group_admin BOOLEAN DEFAULT FALSE, "
                + "PRIMARY KEY (group_id, admin_id), "
                + "FOREIGN KEY (group_id) REFERENCES article_groups(group_id), "
                + "FOREIGN KEY (admin_id) REFERENCES users(userName)"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
            System.out.println("Table 'group_admins' created or already exists.");
        }
    }

    // Create the group_instructors table to store instructor rights for groups
    public void createGroupInstructorsTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS group_instructors ("
                + "group_id BIGINT NOT NULL, "
                + "instructor_id VARCHAR(255) NOT NULL, "
                + "can_view_body BOOLEAN DEFAULT FALSE, "
                + "is_group_admin BOOLEAN DEFAULT FALSE, "
                + "PRIMARY KEY (group_id, instructor_id), "
                + "FOREIGN KEY (group_id) REFERENCES article_groups(group_id), "
                + "FOREIGN KEY (instructor_id) REFERENCES users(userName)"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
            System.out.println("Table 'group_instructors' created or already exists.");
        }
    }

    // Create the group_students table to store student viewing rights for special access groups
    public void createGroupStudentsTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS group_students ("
                + "group_id BIGINT NOT NULL, "
                + "student_id VARCHAR(255) NOT NULL, "
                + "PRIMARY KEY (group_id, student_id), "
                + "FOREIGN KEY (group_id) REFERENCES article_groups(group_id), "
                + "FOREIGN KEY (student_id) REFERENCES users(userName)"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
            System.out.println("Table 'group_students' created or already exists.");
        }
    }
	
	//TODO: do documentation
	public void createHelpArticlesGroups() throws SQLException {
	    String query = "CREATE TABLE IF NOT EXISTS help_article_groups ("
	            + "article_id BIGINT NOT NULL, "
	            + "group_id BIGINT NOT NULL, "
	            + "PRIMARY KEY (article_id, group_id), "
	            + "FOREIGN KEY (article_id) REFERENCES help_articles(id), "
	            + "FOREIGN KEY (group_id) REFERENCES article_groups(group_id)"
	            + ")";
	    try (Statement stmt = connection.createStatement()) {
	        stmt.execute(query);
	    }
	}
	
	// Create a new table for article role permissions
	public void createArticleRolePermissionsTable() throws SQLException {
	    String query = "CREATE TABLE IF NOT EXISTS article_role_permissions ("
	            + "article_id BIGINT NOT NULL, "
	            + "role VARCHAR(50) NOT NULL, "
	            + "PRIMARY KEY (article_id, role), "
	            + "FOREIGN KEY (article_id) REFERENCES help_articles(id) "
	            + "ON DELETE CASCADE"
	            + ")";
	    try (Statement stmt = connection.createStatement()) {
	        stmt.execute(query);
	        System.out.println("Table 'article_role_permissions' created or already exists.");
	    }
	}
	
	public void createSpecialAccessGroupRightsTable() throws SQLException {
	    String query = "CREATE TABLE IF NOT EXISTS special_access_group_rights ("
	            + "group_id BIGINT NOT NULL, "
	            + "user_name VARCHAR(255) NOT NULL, "
	            + "has_admin_rights BOOLEAN DEFAULT FALSE, "
	            + "can_view_body BOOLEAN DEFAULT FALSE, "
	            + "role_type ENUM('ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL, "
	            + "PRIMARY KEY (group_id, user_name), "
	            + "FOREIGN KEY (group_id) REFERENCES special_access_groups(group_id), "
	            + "FOREIGN KEY (user_name) REFERENCES users(userName)"
	            + ");";
	    try (Statement stmt = connection.createStatement()) {
	        stmt.execute(query);
	        System.out.println("Table 'special_access_group_rights' created or already exists.");
	    }
	}
	
	public void createMessagesTable() throws SQLException {
	    String query = "CREATE TABLE IF NOT EXISTS messages (\n"
	            + "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n"
	            + "    sender VARCHAR(255) NOT NULL,\n"
	            + "    recipient_role VARCHAR(50) NOT NULL,\n"
	            + "    content TEXT NOT NULL,\n"
	            + "    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n"
	            + ");";
	    
	    try (Statement stmt = connection.createStatement()) {
	        stmt.execute(query);
	        System.out.println("Table 'messages' created or already exists.");
	    }
	}




	/**
	 * Checks if the 'users' table is empty, which indicates whether an admin
	 * account should be set up.
	 * 
	 * @return true if no users exist, false otherwise.
	 * @throws SQLException if unable to query the database.
	 */
	public boolean isUserTableEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS userCount FROM users";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("userCount") == 0;
			}
		}
		return false;
	}

	/**
	 * Checks if a user already exists in the system.
	 *
	 * @param username the username to check.
	 * @return true if the user exists, false otherwise.
	 * @throws SQLException if unable to query the database.
	 */
	private boolean doesUserExist(String username) throws SQLException {
		String query = "SELECT COUNT(*) FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			return rs.next() && rs.getInt(1) > 0;
		}
	}

	/**
	 * Inserts the initial admin user into the database. This method is used when the system
	 * does not have any users, and it assigns the ADMIN role to the first created user.
	 * 
	 * @param adminUser the User object representing the admin to be inserted.
	 * @throws SQLException if there is an issue with the database operation.
	 */
	public void insertInitialAdmin(User adminUser) throws SQLException {
		// Check if the users table is empty to ensure this is the first user
		if (isUserTableEmpty()) {
			String query = "INSERT INTO users (userName, password, roles, email, firstName, lastName, prefName, midName, invitation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try (PreparedStatement stmt = connection.prepareStatement(query)) {
				stmt.setString(1, adminUser.getUsername());
				stmt.setBytes(2, new String(adminUser.getPassword()).getBytes()); // Convert char[] password to bytes
				stmt.setString(3, Role.ADMIN.name()); // Assign the ADMIN role
				stmt.setString(4, adminUser.getEmail()); // update the email address
				stmt.setString(5, adminUser.getFirstName());
				stmt.setString(6, adminUser.getLastName());
				stmt.setString(7, adminUser.getPrefName());
				stmt.setString(8, adminUser.getMidName());
				stmt.setString(9, "-101");


				stmt.executeUpdate();
				System.out.println("Initial Admin account created successfully: " + adminUser.getUsername());
			}
		} else {
			System.out.println("Admin account creation failed. The system already has users.");
		}
	}


	public void addAdminUser(User adminUser) throws SQLException {
		// Check if the users table is empty to ensure this is the first user
		String query = "INSERT INTO users (userName, password, roles, email, firstName, lastName, prefName, midName, invitation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, adminUser.getUsername());
			stmt.setBytes(2, new String(adminUser.getPassword()).getBytes()); // Convert char[] password to bytes
			stmt.setString(3, Role.ADMIN.name()); // Assign the ADMIN role
			stmt.setString(4, adminUser.getEmail()); // update the email address
			stmt.setString(5, adminUser.getFirstName());
			stmt.setString(6, adminUser.getLastName());
			stmt.setString(7, adminUser.getPrefName());
			stmt.setString(8, adminUser.getMidName());
			stmt.setString(9, "-101");


			stmt.executeUpdate();
			System.out.println("Initial Admin account created successfully: " + adminUser.getUsername());
		}
	}
	
	/**
	 * Checks if a user requires setup. This method will check if the user has missing required fields
	 * such as email, first name, and last name.
	 * 
	 * @param username the username of the user to check.
	 * @return true if the user requires setup, false otherwise.
	 * @throws SQLException if unable to query the database.
	 */
	public boolean doesUserRequireSetup(String username) throws SQLException {
		String query = "SELECT email, firstName, lastName FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String email = rs.getString("email");
				String firstName = rs.getString("firstName");
				String lastName = rs.getString("lastName");

				// Check if any of these fields are null or empty
				return (email == null || email.isEmpty()) ||
						(firstName == null || firstName.isEmpty()) ||
						(lastName == null || lastName.isEmpty());
			}
		}
		return false; // If the user does not exist or is fully set up
	}


	/**
	 * Creates a user using an invitation code, sets up roles.
	 * 
	 * @param username the username of the new user.
	 * @param invitationCode the invitation code provided to the user.
	 * @param roles the roles assigned to this user.
	 * @throws SQLException if unable to insert the user or if database errors occur.
	 */
	public void createUser(String email, String invitationCode, List<Role> roles) throws SQLException {

		if (doesUserExist(email)) {
			System.out.println("User with email " + email + " already exists. Creation failed.");
			return;
		}
		if(roles.size() < 1) {
			System.out.println("Atleast one Role should be assigned.");
			return;
		}
		String rolesName = "";
		for(Role role : roles)
			rolesName += role.name()+", ";
		rolesName = rolesName.substring(0, rolesName.length()-2);

		// Proceed to create the user
		String query = "INSERT INTO users (email, invitation, roles) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, email);
			stmt.setString(2, invitationCode);
			stmt.setString(3, rolesName);

			stmt.executeUpdate();
			System.out.println("User " + email + " created successfully using invitation.");
		}
	}
	
	public void createUserTesting(String userName, String password, List<Role> roles) throws SQLException {

		if(roles.size() < 1) {
			System.out.println("Atleast one Role should be assigned.");
			return;
		}
		String rolesName = "";
		for(Role role : roles)
			rolesName += role.name()+", ";
		rolesName = rolesName.substring(0, rolesName.length()-2);

		// Proceed to create the user
		String query = "INSERT INTO users (userName, password, roles) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, userName);
			stmt.setBytes(2, password.getBytes());
			stmt.setString(3, rolesName);

			stmt.executeUpdate();
			System.out.println("User " + userName + " created successfully using invitation.");
		}
	}


	/**
	 * Handles user login by verifying username and password, and checks if the user requires additional setup.
	 * 
	 * @param username the username of the user.
	 * @param password the password to verify.
	 * @return "LOGIN_SUCCESS" if the login is successful, "OTP_REQUIRED" if the user must set up a new password,
	 *         "INVITATION_PENDING" if the user account is not fully set up, "SETUP_REQUIRED" if profile completion is needed,
	 *         "INCORRECT_PASSWORD" if the password does not match, and "NOT_FOUND" if the username does not exist.
	 * @throws SQLException if unable to query the database.
	 */
	public String loginUser(String username, String password) throws SQLException {
		String query = "SELECT password, otp, invitation, email, firstName, lastName, roles FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				byte[] storedPasswordBytes = rs.getBytes("password");
				String storedPassword = new String(storedPasswordBytes);
				String oneTimePassword = rs.getString("otp");
				String invitation = rs.getString("invitation");
				String email = rs.getString("email");
				String firstName = rs.getString("firstName");
				String lastName = rs.getString("lastName");

				// Check if the user has a one-time password set
				if (oneTimePassword != null && !oneTimePassword.isEmpty()) {
					return "OTP_REQUIRED"; // User must set up a new password
				}

				// Check if the user was invited but not fully set up yet
				if ((invitation == null || !invitation.equals("-101") ) && !rs.getString("roles").contains("ADMIN")) { // 
					return "INVITATION_PENDING"; // User needs to set up their account using the invitation code
				}

				// Check if the user's profile is incomplete (requires setup)
				if (email == null || email.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
					return "SETUP_REQUIRED"; // User must complete their profile setup
				}

				// Verify the password
				if (storedPassword.equals(password)) {
					return "LOGIN_SUCCESS"; // Successful login
				} else {
					return "INCORRECT_PASSWORD"; // Password does not match
				}
			}

			return "NOT_FOUND"; // Username does not exist
		}
	}

	/**
	 * returns user object from the users table, useful to login the user in session management
	 * @param username the username of the user.
	 * @return User object of null if user not found
	 * @throws SQLException if unable to query the database.
	 */

	public User getUser(String username) throws SQLException {
		String query = "SELECT userName, email, firstName, lastName, prefName, midName, password, otp, otpExpiration, invitation, roles FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String userName = rs.getString("userName");
				String email = rs.getString("email");
				String firstName = rs.getString("firstName");
				String lastName = rs.getString("lastName");
				String prefName = rs.getString("prefName");
				String midName = rs.getString("midName");
				byte[] passwordBytes = rs.getBytes("password") != null ? rs.getBytes("password") : "".getBytes();
				String otp = rs.getString("otp");
				LocalDateTime otpExpiration = rs.getTimestamp("otpExpiration") != null ? rs.getTimestamp("otpExpiration").toLocalDateTime() : null;
				String invitation = rs.getString("invitation");
				String rolesString = rs.getString("roles");

				List<Role> roles = new ArrayList<>();
				if (rolesString != null && !rolesString.isEmpty()) {
					for (String roleName : rolesString.split(", ")) {
						roles.add(Role.valueOf(roleName));
					}
				}

				// Create User object with the fetched details
				User user = new User();
				user.setUserName(userName);
				user.setEmail(email);
				user.setFirstName(firstName);
				user.setLastName(lastName);
				user.setPrefName(prefName);
				user.setMidName(midName);
				user.setPassword(new String(passwordBytes).toCharArray()); // Convert byte array to char array
				user.setOtp(otp);
				user.setOtpExpiration(otpExpiration);
				user.setInvitation(invitation);
				user.setRoles(roles);

				return user;
			} else {
				return null; // User not found
			}
		}
	}


	/**
	 * Sets up the user's profile after initial account creation. This is typically used
	 * when a new user joins using an invitation and needs to provide additional details.
	 * 
	 * @param username the username of the user to set up.
	 * @param email the email address of the user.
	 * @param firstName the first name of the user.
	 * @param lastName the last name of the user.
	 * @param midName the middle name of the user (optional).
	 * @param prefName the preferred name of the user (optional).
	 * @throws SQLException if unable to update the user profile.
	 */
	public void setupUserProfile(String username, String email, String firstName, String lastName, String midName, String prefName) throws SQLException {
		String query = "UPDATE users SET firstName = ?, lastName = ?, midName = ?, prefName = ?, invitation = ? WHERE email = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, firstName);
			stmt.setString(2, lastName);
			stmt.setString(3, midName != null ? midName : ""); // Use empty string if not provided
			stmt.setString(4, prefName != null ? prefName : ""); // Use empty string if not provided
			stmt.setString(5, "-101");
			stmt.setString(6, email);

			System.out.println("SQL query run good...");

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Profile setup completed successfully for user: " + username);
			} else {
				System.out.println("Failed to set up profile. User " + username + " may not exist.");
			}
		}
	}

	/**
	 * Handles user login using a one-time password (OTP). If the OTP is valid, the user is prompted
	 * to update their password. After the password is successfully updated, the OTP is cleared.
	 * 
	 * @param username the username of the user trying to log in.
	 * @param otp the one-time password provided by the user.
	 * @param newPassword the new password to set after successful OTP verification.
	 * @throws SQLException if there are issues querying the database or updating the user.
	 */
	public String loginWithOtp(String username, String otp) throws SQLException {
		String query = "SELECT otp, otpExpiration FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String storedOtp = rs.getString("otp");
				Timestamp otpExpiration = rs.getTimestamp("otpExpiration");

				// Check if OTP matches and if it has not expired
				if (storedOtp != null && storedOtp.equals(otp)) {
					if (otpExpiration != null && otpExpiration.toLocalDateTime().isAfter(LocalDateTime.now())) {
						// OTP is valid, proceed to allow password update
						return "SUCCESS";
					} else {

						System.out.println("The OTP has expired. Please request a new OTP.");
						return "OTP_EXPIRED";
					}
				} else {
					System.out.println("The provided OTP is incorrect. Login failed.");
					return "INCORRECT_OTP";
				}
			} else {
				System.out.println("User not found.");
				return "USER_NOT_FOUND";
			}
		}
	}


	public String loginWithInvitation(String email, String invitationCode) throws SQLException {
		String query = "SELECT invitation FROM users WHERE email = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String storedInvite = rs.getString("invitation");

				if (!storedInvite.isBlank() && !storedInvite.equals(invitationCode)) {
					System.out.println("Invitation code is invalid.");
					return null;
				}
				return email;
			} else {
				System.out.println("User not found. Login failed.");
				return null;
			}
		}
		catch(SQLNonTransientConnectionException e) {
			reconnect();
		}
		return null;
	}

	/**
	 * Updates the user's password. This method requires the user to provide the current password
	 * for validation before setting a new password.
	 * 
	 * @param username the username of the user whose password needs to be updated.
	 * @param oldPassword the current password for validation.
	 * @param newPassword the new password to set.
	 * @throws SQLException if unable to update the password or if validation fails.
	 */
	public void updateUserPasswordAfterOTP(String username, String oldPassword, String newPassword) throws SQLException {
		// First, validate the old password
		String query = "SELECT password FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String storedPassword = new String(rs.getBytes("password"));

				// Check if the old password matches the stored password
				if (!storedPassword.isBlank() && !storedPassword.equals(oldPassword)) {
					System.out.println("Current password is incorrect. Password update failed.");
					return;
				}
			} else {
				System.out.println("User not found. Password update failed.");
				return;
			}
		}

		// If the old password is valid, proceed to update the password
		String updateQuery = "UPDATE users SET password = ?, otp = ?, otpExpiration = ?  WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
			stmt.setBytes(1, newPassword.getBytes()); // Convert new password to bytes
			stmt.setString(4, username);
			stmt.setString(3, null);
			stmt.setString(2, null);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Password updated successfully for user: " + username);
			} else {
				System.out.println("Password update failed. User " + username + " may not exist.");
			}
		}
	}

	/**
	 * Updates the user's password. This method requires the user to provide the current password
	 * for validation before setting a new password.
	 * 
	 * @param username the username of the user whose password needs to be updated.
	 * @param oldPassword the current password for validation.
	 * @param newPassword the new password to set.
	 * @throws SQLException if unable to update the password or if validation fails.
	 */
	public void updateUserPassword(String username, String newPassword) throws SQLException {
		String updateQuery = "UPDATE users SET password = ? WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
			stmt.setBytes(1, newPassword.getBytes()); // Convert new password to bytes
			stmt.setString(2, username);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Password updated successfully for user: " + username);
				resetOtp(username); // Reset OTP only if the password update was successful
			} else {
				System.out.println("Password update failed. User " + username + " may not exist.");
			}
		}
	}

	private void resetOtp(String username) throws SQLException {
		String resetOtpQuery = "UPDATE users SET OTP = NULL WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(resetOtpQuery)) {
			stmt.setString(1, username);
			stmt.executeUpdate();
			System.out.println("OTP reset for user: " + username);
		}
	}

	/**
	 * Updates the details of an existing user. This method allows users to update their
	 * email, names, and other optional fields.
	 * 
	 * @param username the username of the user to update.
	 * @param email the new email address of the user.
	 * @param firstName the new first name of the user.
	 * @param lastName the new last name of the user.
	 * @param midName the new middle name of the user (optional).
	 * @param prefName the new preferred name of the user (optional).
	 * @throws SQLException if unable to update the user details.
	 */
	public void updateUserDetails(String username, String email, String firstName, String lastName, String midName, String prefName) throws SQLException {
		String query = "UPDATE users SET email = ?, firstName = ?, lastName = ?, midName = ?, prefName = ? WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, email);
			stmt.setString(2, firstName);
			stmt.setString(3, lastName);
			stmt.setString(4, midName != null ? midName : ""); // Use empty string if not provided
			stmt.setString(5, prefName != null ? prefName : ""); // Use empty string if not provided
			stmt.setString(6, username);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("User details updated successfully for: " + username);
			} else {
				System.out.println("Failed to update details. User " + username + " may not exist.");
			}
		}
	}


	public String getName(String username) throws SQLException{
		String query = "SELECT oneTimePassword, invitation, email, firstName, prefName FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String oneTimePassword = rs.getString("oneTimePassword");
				String invitation = rs.getString("invitation");
				String email = rs.getString("email");
				String firstName = rs.getString("firstName");
				String prefName = rs.getString("prefName");

				// Check if the user has a one-time password set
				if (oneTimePassword != null && !oneTimePassword.isEmpty()) {
					return "OTP_REQUIRED"; // User must set up a new password
				}

				// Check if the user was invited but not fully set up yet
				if (!invitation.equals("-101")) {
					return "INVITATION_PENDING"; // User needs to set up their account using the invitation code
				}

				// Check if the user's profile is incomplete (requires setup)
				if (email == null || email.isEmpty() || firstName == null || firstName.isEmpty()) {
					return "SETUP_REQUIRED"; // User must complete their profile setup
				}

				return (prefName == null ||prefName.isBlank())?firstName:prefName;
			}

			return "NOT_FOUND"; // Username does not exist
		}

	}

	public List<Role> getRoles(String username) throws SQLException{
		String query = "SELECT password, otp, invitation, email, firstName, roles FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
//				byte[] storedPasswordBytes = rs.getBytes("password");
//				String storedPassword = new String(storedPasswordBytes);
				String oneTimePassword = rs.getString("otp");
				String invitation = rs.getString("invitation");
				String email = rs.getString("email");
				String firstName = rs.getString("firstName");
				String roles = rs.getString("roles");

				// Check if the user has a one-time password set
				if (oneTimePassword != null && !oneTimePassword.isEmpty()) {
					System.out.println("User must setup new password." );
					return new ArrayList<>(); // User must set up a new password
				}

				// Check if the user was invited but not fully set up yet
				if (!invitation.equals("-101")) {
					System.out.println("User must setup their account using invitation code." );
					return new ArrayList<>(); // User needs to set up their account using the invitation code
				}

				// Check if the user's profile is incomplete (requires setup)
				if (email == null || email.isEmpty() || firstName == null || firstName.isEmpty()) {
					System.out.println("User must setup their profile." );
					return new ArrayList<>(); // User must complete their profile setup
				}
				
				System.out.println(roles);
				String roleArr[] = roles.split(", ");
				for(String st: roleArr) System.out.println(st);
				List<Role> roleList = new ArrayList<>();
				for (String roleName : roleArr) {
					try {
						Role role = Role.valueOf(roleName);
						roleList.add(role);
					} catch (IllegalArgumentException e) {
						System.out.println("Invalid role name: " + roleName);
					}
				}
				return roleList;

			}


			return new ArrayList<>(); // Username does not exist
		}

	}


	/**
	 * Resets a user account by setting a one-time password and expiration time.
	 * 
	 * @param username the username of the user to reset.
	 * @param otp the one-time password to set.
	 * @param expiration the expiration date and time for the OTP.
	 * @throws SQLException if unable to reset the account.
	 */
	public void resetUserAccount(String username, String otp, LocalDateTime expiration) throws SQLException {
		String query = "UPDATE users SET otp = ?, otpExpiration = ? WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, otp);
			stmt.setTimestamp(2, Timestamp.valueOf(expiration));
			stmt.setString(3, username);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("User account reset successfully. OTP set for user: " + username);
			} else {
				System.out.println("Failed to reset account. User " + username + " may not exist.");
			}
		}
	}

	/**
	 * Deletes a user account after confirmation.
	 * 
	 * @param username the username of the user to delete.
	 * @throws SQLException if unable to delete the user.
	 */
	public void deleteUserAccount(String username) throws SQLException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Are you sure you want to delete user " + username + "? Type 'Yes' to confirm.");
		String confirmation = scanner.nextLine();

		if (!"Yes".equalsIgnoreCase(confirmation)) {
			System.out.println("User deletion canceled.");
			scanner.close();
			return;
		}

		String query = "DELETE FROM users WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, username);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("User " + username + " deleted successfully.");
			} else {
				System.out.println("User not found. Deletion failed.");
			}
		}
		scanner.close();
	}
	/**
	 * Lists all user accounts with their details, including username, name, and roles.
	 * 
	 * @return List<User> - List of all users from the database.
	 * @throws SQLException if unable to query the database.
	 */
	public List<User> listAllUserAccounts() throws SQLException {
		String query = "SELECT userName, prefName, firstName, lastName, roles FROM users";
		List<User> users = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String userName = rs.getString("userName");
				String firstName = rs.getString("firstName");
				String prefName = rs.getString("prefName");
//				String fullName = ((prefName == null || prefName.isBlank()) ? firstName : prefName) + " " + rs.getString("lastName");
				String rolesString = rs.getString("roles");

				List<Role> roles = new ArrayList<>();
				for (String roleName : rolesString.split(", ")) {
					roles.add(Role.valueOf(roleName));
				}

				User user = new User();
				user.setUserName(userName);
				user.setFirstName(firstName);
				user.setPrefName(prefName);
				user.setLastName(rs.getString("lastName"));
				user.setRoles(roles);

				users.add(user);
			}
		}
		return users;
	}


	/**
	 * Adds or removes a role from a user.
	 * 
	 * @param username the username of the user.
	 * @param role the role to add or remove.
	 * @param add if true, adds the role; if false, removes the role.
	 * @throws SQLException if unable to modify the roles.
	 */
	public void modifyUserRole(String username, Role role, boolean add) throws SQLException {
		// Get the current roles for the user
		User user = getUser(username);
		if (user == null) {
			System.out.println("User not found.");
			return;
		}

		List<Role> roles = user.getRoles();
		if (roles == null) {
			roles = new ArrayList<>();
		}

		if (add) {
			if (!roles.contains(role)) {
				roles.add(role);
			} else {
				System.out.println("The user already has the role: " + role);
				return; // No need to update if the role already exists
			}
		} else {
			if (roles.contains(role)) {
				roles.remove(role);
			} else {
				System.out.println("The user does not have the role: " + role);
				return; // No need to update if the role does not exist
			}
		}

		// Update the roles in the database
		String rolesName = roles.stream().map(Role::name).collect(Collectors.joining(", "));

		String query = "UPDATE users SET roles = ? WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, rolesName);
			stmt.setString(2, username);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Roles updated for user: " + username);
			} else {
				System.out.println("Failed to update roles for user: " + username);
			}
		}
	}

	/***
	 * 
	 * @param email
	 * @return
	 * @throws SQLException
	 */
	public User getUserByEmail(String email) throws SQLException {
		String query = "SELECT * FROM users WHERE email = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// Create and return User object based on the result set
				User user = new User();
				user.setUserName(rs.getString("userName"));
				user.setEmail(rs.getString("email"));
				user.setFirstName(rs.getString("firstName"));
				user.setLastName(rs.getString("lastName"));
				// Add other fields as needed
				return user;
			} else {
				return null; // No user found with the given email
			}
		}
	}

	/****
	 * 
	 * @param username
	 * @param otp
	 * @param expiration
	 * @throws SQLException
	 */
	
	public void assignOtpToUser(String username, String otp, LocalDateTime expiration) throws SQLException {
		String query = "UPDATE users SET otp = ?, otpExpiration = ? WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, otp);
			stmt.setTimestamp(2, Timestamp.valueOf(expiration));
			stmt.setString(3, username);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("OTP assigned to user: " + username);
			} else {
				System.out.println("Failed to assign OTP. User " + username + " may not exist.");
			}
		}
	}



	/***********
	 *     
	 * Help Articles
	 */
	
	// Add HelpArticle with encryption handling as text
    public void addHelpArticle(HelpArticle article, String userName) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String query = "INSERT INTO help_articles (header, article_level, title, short_description, keywords, body, " +
                    "encrypted_body, links, sensitive_title, sensitive_description, groups_col, access_level, " +
                    "created_by, is_special_access, allowed_roles, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, article.getHeader());
                stmt.setString(2, article.getLevel());
                stmt.setString(3, article.getTitle());
                stmt.setString(4, article.getShortDescription());
                stmt.setString(5, article.getKeywords());

                // Handle encryption
                if (article.isSpecialAccess()) {
                    stmt.setString(6, null); // Body is not stored if encrypted
                    String encryptedContent;
                    try {
                        encryptedContent = EncryptionUtility.Encrypt(article.getBody(), ENCRYPTION_KEY);
                    } catch (Exception e) {
                        encryptedContent = "";
                        e.printStackTrace();
                    }
                    stmt.setString(7, encryptedContent); // Store encrypted body
                } else {
                    stmt.setString(6, article.getBody()); // Store plain body
                    stmt.setString(7, null);
                }

                stmt.setString(8, article.getLinks());
                stmt.setString(9, article.getSensitiveTitle());
                stmt.setString(10, article.getSensitiveDescription());
                stmt.setString(11, article.getGroups() != null ? String.join(",", article.getGroups()) : null);
                stmt.setString(12, article.getAccessLevel());
                stmt.setString(13, userName);
                stmt.setBoolean(14, article.isSpecialAccess());
                stmt.setString(15, article.getAllowedRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(",")));
                stmt.setString(16, article.getId());

                stmt.executeUpdate();

                // Get the generated ID and add role permissions
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long articleId = rs.getLong(1);
                        for (String group : article.getGroups()) {
                            long groupId = addGroupIfNotExists(group);
                            linkArticleToGroup(articleId, groupId);
                        }
                        if (article.getAllowedRoles() != null && !article.getAllowedRoles().isEmpty()) {
                            updateArticleRolePermissions(String.valueOf(articleId), article.getAllowedRoles());
                        }
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
	
    public HelpArticle getHelpArticle(String articleId, String currentUserName, Role userRole) throws SQLException {
        String selectSQL =
                "SELECT ha.*, " +
                "GROUP_CONCAT(DISTINCT arp.role) as allowed_roles, " +
                "GROUP_CONCAT(DISTINCT ag.group_id) as group_ids, " +
                "GROUP_CONCAT(DISTINCT ag.name) as group_names " +
                "FROM help_articles ha " +
                "LEFT JOIN article_role_permissions arp ON ha.id = arp.article_id " +
                "LEFT JOIN help_article_groups hag ON ha.id = hag.article_id " +
                "LEFT JOIN article_groups ag ON hag.group_id = ag.group_id " +
                "WHERE ha.id = ? " +
                "GROUP BY ha.id";

        try (PreparedStatement stmt = connection.prepareStatement(selectSQL)) {
            stmt.setString(1, articleId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Extract data from ResultSet
                    String id = rs.getString("id");
                    String header = rs.getString("header");
                    String level = rs.getString("article_level");
                    String title = rs.getString("title");
                    String shortDescription = rs.getString("short_description");
                    String keywords = rs.getString("keywords");
                    String body = rs.getString("body");
                    String encryptedBody = rs.getString("encrypted_body");
                    String links = rs.getString("links");
                    String sensitiveTitle = rs.getString("sensitive_title");
                    String sensitiveDescription = rs.getString("sensitive_description");
                    String accessLevel = rs.getString("access_level");
                    boolean isSpecialAccess = rs.getBoolean("is_special_access");
                    long specialAccessGroupId = rs.getLong("special_access_group_id");
                    String createdBy = rs.getString("created_by");

                    // Parse allowed roles
                    Set<Role> allowedRoles = new HashSet<>();
                    String rolesStr = rs.getString("allowed_roles");
                    if (rolesStr != null && !rolesStr.isEmpty()) {
                        String[] rolesArray = rolesStr.split(",");
                        for (String role : rolesArray) {
                            try {
                                allowedRoles.add(Role.valueOf(role.trim()));
                            } catch (IllegalArgumentException e) {
                                System.err.println("Invalid role: " + role);
                            }
                        }
                    }

                    // Verify if the user has access
                    if (!hasAccess(currentUserName, userRole, accessLevel, allowedRoles, isSpecialAccess, specialAccessGroupId)) {
                        throw new SQLException("Access denied: You do not have permission to view this article.");
                    }

                    // Handle body content: decrypt if necessary
                    String bodyContent;
                    if (isSpecialAccess && encryptedBody != null) {
                        try {
                            bodyContent = EncryptionUtility.Decrypt(encryptedBody, ENCRYPTION_KEY);
                        } catch (Exception e) {
                            throw new SQLException("Error decrypting article content: " + e.getMessage());
                        }
                    } else {
                        bodyContent = body == null ? "" : body;
                    }
                    
                 // Parse group names
                    String groupNamesStr = rs.getString("group_names");
                    List<String> groupNames;
                    if (groupNamesStr != null && !groupNamesStr.isEmpty()) {
                        groupNames = List.of(groupNamesStr.split(","));
                    } else {
                        groupNames = List.of();
                    }

                    // Create and return the HelpArticle instance
                    return new HelpArticle(
                            id, header, level, title, shortDescription, keywords, bodyContent,
                            encryptedBody, links, sensitiveTitle, sensitiveDescription,
                            groupNames, accessLevel, isSpecialAccess,
                            specialAccessGroupId, createdBy, allowedRoles
                    );
                } else {
                    throw new SQLException("Article not found with ID: " + articleId);
                }
            }
        }
    }

    // Method to verify user access to an article
    private boolean hasAccess(String currentUserName, Role userRole, String accessLevel, Set<Role> allowedRoles, boolean isSpecialAccess, long specialAccessGroupId) throws SQLException {
        // Check if the access level is public
        if ("PUBLIC".equalsIgnoreCase(accessLevel)) {
            return true;
        }

        // Check role-based access
        if (allowedRoles != null && allowedRoles.contains(userRole)) {
            return true;
        }

        // Check special access group permission
        if (isSpecialAccess) {
            return hasSpecialAccessPermission(currentUserName, specialAccessGroupId);
        }

        // If none of the above conditions match, access is denied
        return false;
    }

    

 // Helper method to parse allowed roles from a comma-separated string
    private Set<Role> parseAllowedRoles(String rolesString) {
        Set<Role> roles = new HashSet<>();
        if (rolesString != null && !rolesString.trim().isEmpty()) {
            Arrays.stream(rolesString.split(","))
                    .map(String::trim)
                    .filter(roleStr -> !roleStr.isEmpty())
                    .forEach(roleStr -> {
                        try {
                            roles.add(Role.valueOf(roleStr));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid role found: " + roleStr);
                        }
                    });
        }
        return roles;
    }

	
	/**
     * Checks if the given user has special access permission for a specific group.
     *
     * @param userName The username to check for special access permission.
     * @param groupName The name of the group for which access is being verified.
     * @return true if the user has special access permission, false otherwise.
     * @throws SQLException if an error occurs while querying the database.
     */
	public boolean hasSpecialAccessPermission(String userName, long groupId) throws SQLException {
	    String query = "SELECT COUNT(*) FROM special_access_group_rights WHERE group_id = ? AND user_name = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setLong(1, groupId);  // Use group_id instead of group_name
	        stmt.setString(2, userName);
	        System.out.println("Executing query: " + query);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1) > 0;
	            }
	        }
	    }
	    return false;
	}

	public void assignArticleToGroup(String articleId, String groupName, String username) throws SQLException {
	    // Validate permissions
	    long groupId = getGroupIdByName(groupName);
	    if (!userHasPermissionForGroup(username, groupId)) {
	        throw new SQLException("User does not have admin rights for this group.");
	    }

	    // Retrieve existing groups
	    String currentGroups = getCurrentGroupsForArticle(articleId);
	    List<String> groupList = new ArrayList<>(Arrays.asList(currentGroups.split(",")));

	    // Check if the group already exists
	    if (!groupList.contains(groupName)) {
	        groupList.add(groupName);

	        // Concatenate updated groups
	        String updatedGroups = String.join(",", groupList);

	        // Update the `groups_col` field
	        String query = "UPDATE help_articles SET groups_col = ? WHERE id = ?";
	        try (PreparedStatement stmt = connection.prepareStatement(query)) {
	            stmt.setString(1, updatedGroups);
	            stmt.setString(2, articleId);
	            stmt.executeUpdate();
	            System.out.println("Group successfully assigned: " + groupName);
	        }
	    } else {
	        System.out.println("Group is already assigned to this article.");
	    }
	}


	public void removeArticleFromGroup(String articleId, String groupName, String username) throws SQLException {
	    // Validate permissions
	    long groupId = getGroupIdByName(groupName);
	    if (!userHasPermissionForGroup(username, groupId)) {
	        throw new SQLException("User does not have admin rights for this group.");
	    }

	    // Retrieve existing groups
	    String currentGroups = getCurrentGroupsForArticle(articleId);
	    List<String> groupList = new ArrayList<>(Arrays.asList(currentGroups.split(",")));

	    // Remove the group if it exists
	    if (groupList.contains(groupName)) {
	        groupList.remove(groupName);

	        // Concatenate updated groups
	        String updatedGroups = String.join(",", groupList);

	        // Update the `groups_col` field
	        String query = "UPDATE help_articles SET groups_col = ? WHERE id = ?";
	        try (PreparedStatement stmt = connection.prepareStatement(query)) {
	            stmt.setString(1, updatedGroups);
	            stmt.setString(2, articleId);
	            stmt.executeUpdate();
	            System.out.println("Group successfully removed: " + groupName);
	        }
	    } else {
	        System.out.println("Group is not assigned to this article.");
	    }
	}

	
	public List<String> getArticleGroups(String articleId) throws SQLException {
	    List<String> groups = new ArrayList<>();
	    String query = "SELECT groups_col FROM help_articles WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, articleId);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            String groupsCol = rs.getString("groups_col");
	            if (groupsCol != null && !groupsCol.isEmpty()) {
	                String[] groupArray = groupsCol.split(",");
	                groups.addAll(List.of(groupArray));
	            }
	        }
	    }
	    return groups;
	}

	
	private String getCurrentGroupsForArticle(String articleId) throws SQLException {
	    String query = "SELECT groups_col FROM help_articles WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, articleId);

	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("groups_col") != null ? rs.getString("groups_col") : "";
	        }
	    }
	    return "";
	}




    
	public List<HelpArticle> searchHelpArticles(String query, String level, List<String> groups, String userName, Role userRole) 
	        throws SQLException {
	    StringBuilder searchSQL = new StringBuilder(
	        "SELECT ha.*, " +
	        "GROUP_CONCAT(DISTINCT arp.role) as allowed_roles, " +
	        "GROUP_CONCAT(DISTINCT ag.group_id) as group_ids, " +
	        "GROUP_CONCAT(DISTINCT ag.name) as group_names " +
	        "FROM help_articles ha " +
	        "LEFT JOIN article_role_permissions arp ON ha.id = arp.article_id " +
	        "LEFT JOIN help_article_groups hag ON ha.id = hag.article_id " +
	        "LEFT JOIN article_groups ag ON hag.group_id = ag.group_id " +
	        "WHERE (ha.title LIKE ? OR ha.short_description LIKE ? OR ha.keywords LIKE ?) "
	    );

	    if (level != null) {
	        searchSQL.append(" AND ha.level = ?");
	    }
	    
	    if (groups != null && !groups.isEmpty()) {
	        searchSQL.append(" AND ag.name IN (")
	                .append(String.join(",", Collections.nCopies(groups.size(), "?")))
	                .append(")");
	    }
	    
	    searchSQL.append(" GROUP BY ha.id");

	    try (PreparedStatement stmt = connection.prepareStatement(searchSQL.toString())) {
	        // Set search parameters
	        int paramIndex = 1;
	        String searchPattern = "%" + query + "%";
	        stmt.setString(paramIndex++, searchPattern);
	        stmt.setString(paramIndex++, searchPattern);
	        stmt.setString(paramIndex++, searchPattern);

	        if (level != null) {
	            stmt.setString(paramIndex++, level);
	        }

	        if (groups != null && !groups.isEmpty()) {
	            for (String group : groups) {
	                stmt.setString(paramIndex++, group.trim());
	            }
	        }

	        List<HelpArticle> results = new ArrayList<>();
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                // Parse access control information
	                String accessLevel = rs.getString("access_level");
	                boolean isSpecialAccess = rs.getBoolean("is_special_access");
	                String groupIdsCol = rs.getString("group_ids");

	                // Parse allowed roles
	                Set<Role> allowedRoles = new HashSet<>();
	                String rolesStr = rs.getString("allowed_roles");
	                if (rolesStr != null && !rolesStr.isEmpty()) {
	                    Arrays.stream(rolesStr.split(","))
	                          .filter(role -> role != null && !role.trim().isEmpty())
	                          .forEach(role -> {
	                              try {
	                                  allowedRoles.add(Role.valueOf(role.trim()));
	                              } catch (IllegalArgumentException e) {
	                                  System.err.println("Invalid role found: " + role);
	                              }
	                          });
	                }

	                // Check access permissions
	                if (!isAccessible(accessLevel, userRole, allowedRoles)) {
	                    continue; // Skip articles that the user doesn't have access to
	                }

	                // Check special access permissions
	                if (isSpecialAccess && groupIdsCol != null && !groupIdsCol.trim().isEmpty()) {
	                    List<Long> groupIds = Arrays.stream(groupIdsCol.split(","))
	                                                .map(String::trim)
	                                                .map(Long::parseLong)
	                                                .collect(Collectors.toList());
	                    boolean hasPermission = groupIds.stream()
	                        .anyMatch(groupId -> {
	                            try {
	                                return hasSpecialAccessPermission(userName, groupId); // Using groupId for special access check
	                            } catch (SQLException e) {
	                                return false;
	                            }
	                        });

	                    if (!hasPermission) {
	                        continue; // Skip articles that require special access if the user doesn't have permission
	                    }
	                }

	                // Handle content based on access type
	                String bodyContent;
	                String encryptedBody = rs.getString("encrypted_body");
	                if (isSpecialAccess) {
	                    if (encryptedBody != null) {
	                        try {
	                            bodyContent = EncryptionUtility.Decrypt(encryptedBody, ENCRYPTION_KEY);
	                        } catch (Exception e) {
	                            System.err.println("Error decrypting article content: " + e.getMessage());
	                            continue; // Skip this article if there's an error decrypting
	                        }
	                    } else {
	                        bodyContent = "";
	                    }
	                } else {
	                    bodyContent = rs.getString("body");
	                    if (bodyContent == null) {
	                        bodyContent = "";
	                    }
	                }

	                // Parse groups
	                String groupsCol = rs.getString("group_names");
	                List<String> articleGroups = new ArrayList<>();
	                if (groupsCol != null && !groupsCol.trim().isEmpty()) {
	                    articleGroups = Arrays.stream(groupsCol.split(","))
	                        .map(String::trim)
	                        .filter(g -> !g.isEmpty())
	                        .collect(Collectors.toList());
	                }

	                // Create article object
	                HelpArticle article = new HelpArticle(
	                    rs.getString("id"),
	                    rs.getString("header"),
	                    rs.getString("article_level"),
	                    rs.getString("title"),
	                    rs.getString("short_description"),
	                    rs.getString("keywords"),
	                    bodyContent,
	                    encryptedBody,
	                    rs.getString("links"),
	                    rs.getString("sensitive_title"),
	                    rs.getString("sensitive_description"),
	                    articleGroups,
	                    accessLevel,
	                    isSpecialAccess,
	                    rs.getLong("special_access_group_id"),
	                    rs.getString("created_by"),
	                    allowedRoles
	                );

	                results.add(article);
	            }
	        }
	        return results;
	    } catch (SQLException e) {
	        System.err.println("Error searching articles: " + e.getMessage());
	        throw e;
	    }
	}



	public void addGroupAdmin(long groupId, String adminId, boolean isSpecialGroupAdmin) throws SQLException {
	    String query = "INSERT INTO group_admins (group_id, admin_id, is_special_group_admin) VALUES (?, ?, ?)";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setLong(1, groupId);
	        stmt.setString(2, adminId);
	        stmt.setBoolean(3, isSpecialGroupAdmin);
	        stmt.executeUpdate();
	        System.out.println("Admin added to group successfully.");
	    }
	}
	
	public String getDecryptedBody(long articleId, String userName, Role userRole) throws SQLException {
	    // Query for article information including role permissions
	    String accessQuery = 
	        "SELECT ha.access_level, ha.is_special_access, ha.encrypted_body, ha.body, " +
	        "GROUP_CONCAT(DISTINCT arp.role) as allowed_roles, " +
	        "GROUP_CONCAT(DISTINCT ag.group_id) as group_ids " +
	        "FROM help_articles ha " +
	        "LEFT JOIN article_role_permissions arp ON ha.id = arp.article_id " +
	        "LEFT JOIN help_article_groups hag ON ha.id = hag.article_id " +
	        "LEFT JOIN article_groups ag ON hag.group_id = ag.group_id " +
	        "WHERE ha.id = ? " +
	        "GROUP BY ha.id";

	    try (PreparedStatement stmt = connection.prepareStatement(accessQuery)) {
	        stmt.setLong(1, articleId);

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                String accessLevel = rs.getString("access_level");
	                boolean isSpecialAccess = rs.getBoolean("is_special_access");
	                String groupIdsCol = rs.getString("group_ids");

	                // Parse role permissions
	                Set<Role> allowedRoles = new HashSet<>();
	                String rolesStr = rs.getString("allowed_roles");
	                if (rolesStr != null && !rolesStr.isEmpty()) {
	                    Arrays.stream(rolesStr.split(","))
	                          .filter(role -> role != null && !role.trim().isEmpty())
	                          .forEach(role -> {
	                              try {
	                                  allowedRoles.add(Role.valueOf(role.trim()));
	                              } catch (IllegalArgumentException e) {
	                                  System.err.println("Invalid role found: " + role);
	                              }
	                          });
	                }

	                // Check access permissions
	                if (!isAccessible(accessLevel, userRole, allowedRoles)) {
	                    throw new SQLException("Access denied: Insufficient privileges for this content.");
	                }

	                // For non-special access articles, return regular body
	                if (!isSpecialAccess) {
	                    String body = rs.getString("body");
	                    if (body != null) {
	                        return body;
	                    }
	                    throw new SQLException("Article body not found.");
	                }

	                // Handle special access articles
	                if (isSpecialAccess) {
	                    // Check special access permissions
	                    if (groupIdsCol != null && !groupIdsCol.trim().isEmpty()) {
	                        List<Long> groupIds = Arrays.stream(groupIdsCol.split(","))
	                                                    .map(String::trim)
	                                                    .map(Long::parseLong)
	                                                    .collect(Collectors.toList());

	                        boolean hasPermission = groupIds.stream()
	                            .anyMatch(groupId -> {
	                                try {
	                                    return hasSpecialAccessPermission(userName, groupId);
	                                } catch (SQLException e) {
	                                    return false;
	                                }
	                            });

	                        if (!hasPermission) {
	                            throw new SQLException("Access denied: Special access required for this content.");
	                        }
	                    }

	                    // Get and decrypt the body
	                    byte[] encryptedBody = rs.getBytes("encrypted_body");
	                    if (encryptedBody != null) {
	                        try {
	                            return EncryptionUtility.Decrypt(new String(encryptedBody), ENCRYPTION_KEY);
	                        } catch (Exception e) {
	                            throw new SQLException("Error decrypting article content: " + e.getMessage());
	                        }
	                    }
	                    throw new SQLException("Encrypted content not found for special access article.");
	                }
	            }
	            throw new SQLException("Article not found.");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error retrieving article content: " + e.getMessage());
	        throw e;
	    }
	}

	
	// Update existing article with role permissions
	public void updateHelpArticle(HelpArticle article, String userName, Role userRole) throws SQLException {
	    connection.setAutoCommit(false);
	    try {
	        // Verify permissions
	        // TODO: Uncomment these 3 lines if permission check is implemented
	        // if (!isUserAllowedToModify(article.getId(), userName, userRole)) {
	        //     throw new SQLException("User does not have permission to modify this article");
	        // }

	        // Fetch existing groups_col to avoid overwriting
	        String existingGroupsQuery = "SELECT groups_col FROM help_articles WHERE id = ?";
	        String existingGroupsCol = "";
	        try (PreparedStatement fetchStmt = connection.prepareStatement(existingGroupsQuery)) {
	            fetchStmt.setString(1, article.getId());
	            ResultSet rs = fetchStmt.executeQuery();
	            if (rs.next()) {
	                existingGroupsCol = rs.getString("groups_col");
	            }
	        }

	        // Retain existing groups_col if it exists
	        String updatedGroupsCol = existingGroupsCol != null && !existingGroupsCol.isEmpty()
	                ? existingGroupsCol
	                : String.join(",", article.getGroupsCol());

	        String updateSQL = "UPDATE help_articles SET header = ?, article_level = ?, title = ?, short_description = ?, " +
	                "keywords = ?, body = ?, encrypted_body = ?, links = ?, sensitive_title = ?, " +
	                "sensitive_description = ?, groups_col = ?, access_level = ?, is_special_access = ? " +
	                "WHERE id = ?";

	        try (PreparedStatement stmt = connection.prepareStatement(updateSQL)) {
	            stmt.setString(1, article.getHeader());
	            stmt.setString(2, article.getLevel());
	            stmt.setString(3, article.getTitle());
	            stmt.setString(4, article.getShortDescription());
	            stmt.setString(5, article.getKeywords());

	            if (article.isSpecialAccess()) {
	                stmt.setString(6, null);
	                String encryptedContent;
	                try {
	                    encryptedContent = EncryptionUtility.Encrypt(article.getBody(), ENCRYPTION_KEY);
	                } catch (Exception e) {
	                    encryptedContent = "";
	                    e.printStackTrace();
	                }
	                stmt.setBytes(7, encryptedContent.getBytes());
	            } else {
	                stmt.setString(6, article.getBody());
	                stmt.setBytes(7, null);
	            }

	            stmt.setString(8, article.getLinks());
	            stmt.setString(9, article.getSensitiveTitle());
	            stmt.setString(10, article.getSensitiveDescription());
	            stmt.setString(11, updatedGroupsCol); // Use the retained or updated groups_col
	            stmt.setString(12, article.getAccessLevel());
	            stmt.setBoolean(13, article.isSpecialAccess());
	            stmt.setString(14, article.getId());

	            stmt.executeUpdate();

	            // Update role permissions
	            updateArticleRolePermissions(article.getId(), article.getAllowedRoles());

	            // Do not update group associations here, as Assign/Remove handles it separately
	        }

	        connection.commit();
	    } catch (SQLException e) {
	        connection.rollback();
	        throw e;
	    } finally {
	        connection.setAutoCommit(true);
	    }
	}

	public void saveMessage(String sender, String recipientRole, String messageContent) throws SQLException {
	    String insertSQL = "INSERT INTO messages (sender, recipient_role, content, timestamp) VALUES (?, ?, ?, NOW())";

	    try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
	        stmt.setString(1, sender);
	        stmt.setString(2, recipientRole);
	        stmt.setString(3, messageContent);
	        stmt.executeUpdate();
	    }
	    
	}
	
	public List<Message> getMessagesForRole(String recipientRole) throws SQLException {
	    String query = "SELECT sender, content, timestamp FROM messages WHERE recipient_role = ?";
	    List<Message> messages = new ArrayList<>();

	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, recipientRole);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            String sender = rs.getString("sender");
	            String content = rs.getString("content");
	            LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
	            messages.add(new Message(sender, content, timestamp));
	        }
	        System.out.println("Messages fetched: " + messages.size()); // Debugging
	    } catch (SQLException e) {
	        System.err.println("Error in getMessagesForRole: " + e.getMessage());
	        throw e;
	    }

	    return messages;
	}



	private void updateArticleGroups(String articleId, List<String> groups) throws SQLException {
	    // Remove existing group associations
	    String deleteQuery = "DELETE FROM help_article_groups WHERE article_id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
	        stmt.setString(1, articleId);
	        stmt.executeUpdate();
	    }

	    // Add new group associations
	    for (String group : groups) {
	        long groupId = addGroupIfNotExists(group); // Ensure the group exists and get its ID
	        linkArticleToGroup(articleId, groupId);   // Pass articleId as a String
	    }
	}

	// Helper method to link an article to a group
	private void linkArticleToGroup(String articleId, long groupId) throws SQLException {
	    String insertQuery = "INSERT INTO help_article_groups (article_id, group_id) VALUES (?, ?)";
	    try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
	        stmt.setString(1, articleId);
	        stmt.setLong(2, groupId);
	        stmt.executeUpdate();
	    }
	}
	
	/**
	 * Assigns a group to a help article dynamically.
	 * Updates the groups_col in the help_articles table to include the group name.
	 *
	 * @param articleId The ID of the help article.
	 * @param groupId   The ID of the group to be added.
	 * @throws SQLException If an error occurs while updating the database.
	 */
	public void addGroupToArticle(long articleId, long groupId) throws SQLException {
	    // Retrieve the group name based on groupId
	    String groupName = getGroupNameById(groupId);
	    if (groupName == null) {
	        throw new SQLException("Group with ID " + groupId + " does not exist.");
	    }

	    // Update the help_articles table to include the group
	    String query = "UPDATE help_articles SET groups_col = CONCAT(IFNULL(groups_col, ''), IF(groups_col IS NULL OR groups_col = '', '', ','), ?) WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, groupName);
	        stmt.setLong(2, articleId);

	        int rowsAffected = stmt.executeUpdate();
	        if (rowsAffected == 0) {
	            throw new SQLException("Help article with ID " + articleId + " not found.");
	        }
	        System.out.println("Group " + groupName + " assigned to article ID: " + articleId);
	    }
	}

	/**
	 * Removes a group from a help article dynamically.
	 * Updates the groups_col in the help_articles table to exclude the group name.
	 *
	 * @param articleId The ID of the help article.
	 * @param groupId   The ID of the group to be removed.
	 * @throws SQLException If an error occurs while updating the database.
	 */
	public void removeGroupFromArticle(long articleId, long groupId) throws SQLException {
	    // Retrieve the group name based on groupId
	    String groupName = getGroupNameById(groupId);
	    if (groupName == null) {
	        throw new SQLException("Group with ID " + groupId + " does not exist.");
	    }

	    // Update the help_articles table to remove the group name from groups_col
	    String query = "UPDATE help_articles SET groups_col = TRIM(BOTH ',' FROM REPLACE(CONCAT(',', groups_col, ','), CONCAT(',', ?, ','), ',')) WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, groupName);
	        stmt.setLong(2, articleId);

	        int rowsAffected = stmt.executeUpdate();
	        if (rowsAffected == 0) {
	            throw new SQLException("Help article with ID " + articleId + " not found.");
	        }
	        System.out.println("Group " + groupName + " removed from article ID: " + articleId);
	    }
	}

	public boolean userHasPermissionForGroup(String username, long groupId) throws SQLException {
	    String query = "SELECT COUNT(*) FROM group_admins WHERE userName = ? AND group_id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, username);
	        stmt.setLong(2, groupId);

	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0; // True if at least one record exists
	        }
	    }
	    return false;
	}


	
	
	/**
	 * Retrieves the group name based on its ID.
	 *
	 * @param groupId The ID of the group.
	 * @return The name of the group, or null if not found.
	 * @throws SQLException If an error occurs while querying the database.
	 */
	private String getGroupNameById(long groupId) throws SQLException {
	    String query = "SELECT name FROM article_groups WHERE group_id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setLong(1, groupId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getString("name");
	            }
	        }
	    }
	    return null;
	}



	private boolean isUserAllowedToModify(String articleId, String userName, Role userRole) throws SQLException {
	    if (userRole == Role.ADMIN) {
	        return true;
	    }
	    
	    String query = "SELECT created_by FROM help_articles WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, articleId);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("created_by").equals(userName);
	        }
	    }
	    return false;
	}
	

	public void backupHelpSystemData(String filenamePrefix, List<String> groups, Role userRole, String currentUser) 
	        throws SQLException, IOException {
	    // Verify user authorization
	    if (!isAuthorized(userRole)) {
	        throw new SQLException("You do not have permission to perform a backup.");
	    }

	    // Setup backup file
	    String filename = filenamePrefix + "_backup.dat";
	    File backupFile = new File(filename);
	    File parentDir = backupFile.getParentFile();
	    if (parentDir != null && !parentDir.exists()) {
	        if (!parentDir.mkdirs()) {
	            throw new IOException("Failed to create directory: " + parentDir);
	        }
	    }

	    // Construct query with role permissions
	    StringBuilder query = new StringBuilder(
	        "SELECT ha.*, " +
	        "GROUP_CONCAT(DISTINCT ag.name) AS group_names, " +
	        "GROUP_CONCAT(DISTINCT arp.role) AS allowed_roles " +
	        "FROM help_articles ha " +
	        "LEFT JOIN help_article_groups hag ON ha.id = hag.article_id " +
	        "LEFT JOIN article_groups ag ON hag.group_id = ag.group_id " +
	        "LEFT JOIN article_role_permissions arp ON ha.id = arp.article_id " +
	        "WHERE ha.created_by = ? "
	    );

	    if (groups != null && !groups.isEmpty()) {
	        query.append("GROUP BY ha.id HAVING (")
	             .append(groups.stream()
	                     .map(group -> "group_names LIKE ?")
	                     .collect(Collectors.joining(" OR ")))
	             .append(")");
	    } else {
	        query.append("GROUP BY ha.id");
	    }

	    try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
	        stmt.setString(1, currentUser);
	        
	        // Set group parameters if needed
	        int parameterIndex = 2;
	        if (groups != null && !groups.isEmpty()) {
	            for (String group : groups) {
	                stmt.setString(parameterIndex++, "%" + group + "%");
	            }
	        }

	        // Process results and create backup
	        try (ResultSet rs = stmt.executeQuery();
	             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(backupFile))) {
	            
	            boolean hasData = false;
	            while (rs.next()) {
	                hasData = true;
	                
	                // Convert allowed roles string to Set<Role>
	                Set<Role> allowedRoles = new HashSet<>();
	                String rolesStr = rs.getString("allowed_roles");
	                if (rolesStr != null && !rolesStr.isEmpty()) {
	                    Arrays.stream(rolesStr.split(","))
	                          .filter(role -> role != null && !role.trim().isEmpty())
	                          .forEach(role -> {
	                              try {
	                                  allowedRoles.add(Role.valueOf(role.trim()));
	                              } catch (IllegalArgumentException e) {
	                                  System.err.println("Invalid role found in backup: " + role);
	                              }
	                          });
	                }
	                
	                // Handle body content based on access type
	                String bodyContent;
	                String encryptedBody = rs.getString("encrypted_body");
	                if (rs.getBoolean("is_special_access") && encryptedBody != null) {
	                    try {
	                        bodyContent = EncryptionUtility.Decrypt(encryptedBody, ENCRYPTION_KEY);
	                    } catch (Exception e) {
	                        bodyContent = "";
	                        System.out.println("Couldn't decrypt the body of the article");
	                        e.printStackTrace();
	                    }
	                } else {
	                    bodyContent = rs.getString("body");
	                }

	                // Parse groups
	                List<String> articleGroups = new ArrayList<>();
	                String groupsStr = rs.getString("group_names");
	                if (groupsStr != null && !groupsStr.trim().isEmpty()) {
	                    articleGroups = Arrays.asList(groupsStr.split(","));
	                }

	                // Create HelpArticle object with all fields including role permissions
	                HelpArticle article = new HelpArticle(
	                    rs.getString("id"),
	                    rs.getString("header"),
	                    rs.getString("article_level"),
	                    rs.getString("title"),
	                    rs.getString("short_description"),
	                    rs.getString("keywords"),
	                    bodyContent,
	                    encryptedBody,
	                    rs.getString("links"),
	                    rs.getString("sensitive_title"),
	                    rs.getString("sensitive_description"),
	                    articleGroups,
	                    rs.getString("access_level"),
	                    rs.getBoolean("is_special_access"),
	                    rs.getLong("special_access_group_id"),
	                    rs.getString("created_by"),
	                    allowedRoles  // Include the role permissions
	                );

	                oos.writeObject(article);
	            }

	            if (!hasData) {
	                System.out.println("No data found to backup.");
	            } else {
	                System.out.println("Backup completed successfully to " + filename);
	            }
	        }
	    } catch (SQLException | IOException e) {
	        System.out.println("Error during backup: " + e.getMessage());
	        throw e;
	    }
	}


	public void restoreHelpSystemData(List<String> filenames, boolean overwrite, Role userRole, String userName) throws SQLException, IOException, ClassNotFoundException {
	    if (!isAuthorized(userRole)) {
	        System.out.println("You do not have permission to perform a restore.");
	        return;
	    }

	    if (overwrite) {
	        try (Statement stmt = connection.createStatement()) {
	            stmt.executeUpdate("DELETE FROM help_articles");
	            System.out.println("All existing articles removed before restoring from backup.");
	        }
	    }

	    Set<String> existingIds = overwrite ? new HashSet<>() : getExistingArticleIds();

	    for (String filename : filenames) {
	        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
	            while (true) {
	                try {
	                    HelpArticle article = (HelpArticle) ois.readObject();  // Deserialize each HelpArticle object

	                    // Check if the user is the author or has special access rights
	                    if (isUserAllowedToRestore(userName, article)) {
	                        if (!existingIds.contains(article.getId())) {
	                            addHelpArticle(article, userName);  // Add only if ID is unique
	                            existingIds.add(article.getId());
	                            System.out.println("Article with ID " + article.getId() + " restored from " + filename);
	                        } else {
	                            System.out.println("Skipping duplicate article with ID " + article.getId() + " from " + filename);
	                        }
	                    }
	                } catch (EOFException e) {
	                    break;  // End of file reached
	                } catch (IOException | ClassNotFoundException e) {
	                    System.out.println("Error reading article from " + filename + ": " + e.getMessage());
	                    throw e;  // Rethrow to ensure caller is aware of the failure
	                }
	            }
	            System.out.println("Restore from " + filename + " completed successfully.");
	        } catch (FileNotFoundException e) {
	            System.out.println("Backup file " + filename + " not found.");
	        } catch (IOException e) {
	            System.out.println("Error opening file " + filename + ": " + e.getMessage());
	            throw e; // Rethrow to ensure the caller knows there was an issue with reading the file
	        }
	    }
	}

	
	private boolean isUserAllowedToRestore(String userName, HelpArticle article) throws SQLException {
	    String query = "SELECT COUNT(*) FROM special_access_group_rights sag "
	            + "JOIN help_article_groups hag ON sag.group_id = hag.group_id "
	            + "WHERE hag.article_id = ? AND sag.user_name = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, article.getId());
	        stmt.setString(2, userName);
	        System.out.println("Executing query: " + query);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1) > 0;
	            }
	        }
	    }
	    return false;
	}



	/**
	 * Retrieves the IDs of all existing help articles.
	 *
	 * @return A Set of article IDs currently in the database.
	 * @throws SQLException if a database error occurs.
	 */
	private Set<String> getExistingArticleIds() throws SQLException {
		Set<String> existingIds = new HashSet<>();
		String query = "SELECT id FROM help_articles";

		try (PreparedStatement stmt = connection.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				existingIds.add(rs.getString("id"));
			}
		}
		return existingIds;
	}

	/**
	 * Checks if a user role has permission to perform backup or restore.
	 *
	 * @param userRole The role of the user attempting the operation.
	 * @return true if the user is authorized, false otherwise.
	 */
	private boolean isAuthorized(Role userRole) {
		return userRole == Role.ADMIN || userRole == Role.INSTRUCTOR;
	}

	public static byte[] generateFixedLengthIV(String input) throws NoSuchAlgorithmException {
	    MessageDigest digest = MessageDigest.getInstance("SHA-256");
	    byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
	    return Arrays.copyOf(hash, 16);  // Get the first 16 bytes to use as the IV
	}



	/**
	 * Retrieves a list of all help articles available in the database.
	 * This method queries the 'help_articles' table, constructs 'HelpArticle' objects for each row, and returns them as a list.
	 *
	 * @return List of HelpArticle objects representing all articles in the database.
	 * @throws SQLException if there is an error during the SQL operation.
	 */
	 // Updated listAllArticles method to decrypt encrypted_body when necessary
	// Updated listAllArticles method to use EncryptionUtility
	// List all articles with access control
	// List all articles with proper role permissions
	public List<HelpArticle> listAllArticles(String userName, Role userRole) throws SQLException {
	    List<HelpArticle> articles = new ArrayList<>();
	    String query = "SELECT ha.*, GROUP_CONCAT(arp.role) as allowed_roles, GROUP_CONCAT(DISTINCT ag.group_id) as group_ids " +
	                  "FROM help_articles ha " +
	                  "LEFT JOIN article_role_permissions arp ON ha.id = arp.article_id " +
	                  "LEFT JOIN help_article_groups hag ON ha.id = hag.article_id " +
	                  "LEFT JOIN article_groups ag ON hag.group_id = ag.group_id " +
	                  "GROUP BY ha.id";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(query);
	         ResultSet rs = stmt.executeQuery()) {
	        while (rs.next()) {
	            String accessLevel = rs.getString("access_level");
	            boolean isSpecialAccess = rs.getBoolean("is_special_access");
	            String groupIdsCol = rs.getString("group_ids");
	            
	            // Get allowed roles
	            Set<Role> allowedRoles = new HashSet<>();
	            String rolesStr = rs.getString("allowed_roles");
	            if (rolesStr != null && !rolesStr.isEmpty()) {
	                Arrays.stream(rolesStr.split(","))
	                      .forEach(role -> {
	                          try {
	                              allowedRoles.add(Role.valueOf(role.trim()));
	                          } catch (IllegalArgumentException e) {
	                              System.err.println("Invalid role: " + role);
	                          }
	                      });
	            }
	            
	            // Check access permissions
	            if (!isAccessible(accessLevel, userRole, allowedRoles)) {
	                continue;
	            }
	            
	            // Check special access permission
	            if (isSpecialAccess && groupIdsCol != null && !groupIdsCol.trim().isEmpty()) {
	                List<Long> groupIds = Arrays.stream(groupIdsCol.split(","))
	                                            .map(String::trim)
	                                            .map(Long::parseLong)
	                                            .collect(Collectors.toList());

	                boolean hasPermission = groupIds.stream()
	                        .anyMatch(groupId -> {
	                            try {
	                                return hasSpecialAccessPermission(userName, groupId);
	                            } catch (SQLException e) {
	                                return false;
	                            }
	                        });

	                if (!hasPermission) {
	                    continue;
	                }
	            }
	            
	            // Handle body content
	            String bodyContent;
	            String encryptedBody = rs.getString("encrypted_body");
	            if (isSpecialAccess && encryptedBody != null) {
	                try {
	                    bodyContent = EncryptionUtility.Decrypt(encryptedBody, ENCRYPTION_KEY);
	                } catch (Exception e) {
	                    System.err.println("Error decrypting article content: " + e.getMessage());
	                    continue;
	                }
	            } else {
	                bodyContent = rs.getString("body");
	                if (bodyContent == null) {
	                    bodyContent = "";
	                }
	            }
	            
	            HelpArticle article = new HelpArticle(
	                rs.getString("id"),
	                rs.getString("header"),
	                rs.getString("article_level"),
	                rs.getString("title"),
	                rs.getString("short_description"),
	                rs.getString("keywords"),
	                bodyContent,
	                encryptedBody,
	                rs.getString("links"),
	                rs.getString("sensitive_title"),
	                rs.getString("sensitive_description"),
	                Arrays.asList(groupIdsCol != null ? groupIdsCol.split(",") : new String[0]),
	                accessLevel,
	                isSpecialAccess,
	                rs.getLong("special_access_group_id"),
	                rs.getString("created_by"),
	                allowedRoles
	            );
	            
	            articles.add(article);
	        }
	    }
	    return articles;
	}


	
	// Update the isAccessible method to check role permissions
	private boolean isAccessible(String accessLevel, Role userRole, Set<Role> allowedRoles) {
	    // First check if there are specific role permissions
	    if (allowedRoles != null && !allowedRoles.isEmpty()) {
	        return allowedRoles.contains(userRole);
	    }
	    
	    // Fall back to default access level permissions
	    switch (accessLevel) {
	        case "PUBLIC":
	            return true;
	        case "RESTRICTED":
	            return userRole == Role.ADMIN || userRole == Role.INSTRUCTOR || userRole == Role.TA;
	        case "CONFIDENTIAL":
	            return userRole == Role.ADMIN || userRole == Role.INSTRUCTOR;
	        default:
	            return false;
	    }
	}


	// Update role permissions for an article
	public void updateArticleRolePermissions(String articleId, Set<Role> set) throws SQLException {
	    // First delete existing permissions
	    String deleteQuery = "DELETE FROM article_role_permissions WHERE article_id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
	        stmt.setString(1, articleId);
	        stmt.executeUpdate();
	    }
	    
	    if (set != null && !set.isEmpty()) {
	        String insertQuery = "INSERT INTO article_role_permissions (article_id, role) VALUES (?, ?)";
	        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
	            for (Role role : set) {
	                stmt.setString(1, articleId);
	                stmt.setString(2, role.name());
	                stmt.addBatch();
	            }
	            stmt.executeBatch();
	        }
	    }
	}

	// Get role permissions for an article
	public Set<Role> getArticleRolePermissions(String articleId) throws SQLException {
	    Set<Role> roles = new HashSet<>();
	    String query = "SELECT role FROM article_role_permissions WHERE article_id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, articleId);
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            try {
	                roles.add(Role.valueOf(rs.getString("role")));
	            } catch (IllegalArgumentException e) {
	                System.err.println("Invalid role found in database: " + rs.getString("role"));
	            }
	        }
	    }
	    return roles;
	}

	/**
	 * Deletes a help article from the database.
	 *
	 * @param articleId The ID of the article to delete.
	 * @param userRole The role of the user performing the delete operation.
	 * @throws SQLException if an error occurs during the database operation.
	 */
	public void deleteHelpArticle(String articleId, Role userRole) throws SQLException {
	    if (!isAuthorized(userRole)) {
	        System.out.println("You do not have permission to delete articles.");
	        return;
	    }

	    // Start by deleting any related entries in help_article_groups
	    String deleteRelationsSQL = "DELETE FROM help_article_groups WHERE article_id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(deleteRelationsSQL)) {
	        stmt.setString(1, articleId);
	        stmt.executeUpdate(); // Execute update to remove related entries
	    }

	    // Now, try to delete the article itself
	    String deleteSQL = "DELETE FROM help_articles WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
	        stmt.setString(1, articleId);

	        int rowsAffected = stmt.executeUpdate();
	        if (rowsAffected > 0) {
	            System.out.println("Article with ID " + articleId + " deleted successfully.");
	        } else {
	            System.out.println("Article with ID " + articleId + " not found.");
	        }
	    }
	}

	
	
	// Add a new group to the groups table if it does not exist
    public long addGroupIfNotExists(String groupName) throws SQLException {
        String query = "INSERT INTO article_groups (name) VALUES (?) ON DUPLICATE KEY UPDATE group_id = LAST_INSERT_ID(group_id)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, groupName);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Unable to insert or retrieve group ID.");
    }

    // Link a help article to a group
    public void linkArticleToGroup(long articleId, long groupId) throws SQLException {
        String query = "INSERT INTO help_article_groups (article_id, group_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, articleId);
            stmt.setLong(2, groupId);
            stmt.executeUpdate();
        }
    }
    
    public List<String> getAllGroupNames() throws SQLException {
        List<String> groupNames = new ArrayList<>();
        String query = "SELECT name FROM article_groups";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                groupNames.add(rs.getString("name"));
            }
        }
        return groupNames;
    }
    
 // CRUD methods for help_articles to utilize special_access_group_id
// public void addHelpArticle(HelpArticle article, String userName, boolean overwrite) throws SQLException {
//     String query = "INSERT INTO help_articles (header, level, title, short_description, keywords, encrypted_body, links, sensitive_title, sensitive_description, access_level, created_by, is_special_access, special_access_group_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//     try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
//         stmt.setString(1, article.getHeader());
//         stmt.setString(2, article.getLevel());
//         stmt.setString(3, article.getTitle());
//         stmt.setString(4, article.getShortDescription());
//         stmt.setString(5, article.getKeywords());
//
//         // Encrypt article body if special access
//         String encryptedContent;
//         try {
//             encryptedContent = article.isSpecialAccess() ? EncryptionUtility.encrypt(article.getBody(), EncryptionUtils.getInitializationVector(article.getTitle().toCharArray())) : article.getBody();
//             stmt.setString(6, encryptedContent);
//
//             stmt.setString(7, article.getLinks());
//             stmt.setString(8, article.getSensitiveTitle());
//             stmt.setString(9, article.getSensitiveDescription());
//             stmt.setString(10, article.getAccessLevel());
//             stmt.setString(11, userName);
//             stmt.setBoolean(12, article.isSpecialAccess());
//             stmt.setLong(13, article.getSpecialAccessGroupId());
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         stmt.executeUpdate();
//
//         try (ResultSet rs = stmt.getGeneratedKeys()) {
//             if (rs.next()) {
//                 long articleId = rs.getLong(1);
//                 for (String group : article.getGroups()) {
//                     long groupId = addGroupIfNotExists(group);
//                     linkArticleToGroup(articleId, groupId);
//                 }
//             }
//         }
//         System.out.println("Help article added successfully.");
//     }
// }

    // Methods to handle rights in special_access_group_rights
    public void addSpecialAccessAdmin(long groupId, String adminId) throws SQLException {
        String query = "INSERT INTO special_access_group_rights (group_id, user_name, has_admin_rights, role_type) " +
                       "VALUES (?, ?, TRUE, 'ADMIN') " +
                       "ON DUPLICATE KEY UPDATE has_admin_rights = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, groupId);
            stmt.setString(2, adminId);
            stmt.executeUpdate();
            System.out.println("Special access admin added or updated successfully.");
        }
    }

    public void removeSpecialAccessAdmin(long groupId, String adminId) throws SQLException {
        String query = "DELETE FROM special_access_group_rights WHERE group_id = ? AND user_name = ? AND role_type = 'ADMIN'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, groupId);
            stmt.setString(2, adminId);
            System.out.println("Executing query: " + query);
            stmt.executeUpdate();
            System.out.println("Special access admin removed successfully.");
        }
    }


    public void grantViewRights(long groupId, String userName) throws SQLException {
        String query = "INSERT INTO special_access_group_rights (group_id, user_name, can_view_body, role_type) VALUES (?, ?, TRUE, 'STUDENT') ON DUPLICATE KEY UPDATE can_view_body = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, groupId);
            stmt.setString(2, userName);
            System.out.println("Executing query: " + query);
            stmt.executeUpdate();
            System.out.println("View rights granted successfully.");
        }
    }

    public void revokeViewRights(long groupId, String userName) throws SQLException {
        String query = "UPDATE special_access_group_rights SET can_view_body = FALSE WHERE group_id = ? AND user_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, groupId);
            stmt.setString(2, userName);
            System.out.println("Executing query: " + query);
            stmt.executeUpdate();
            System.out.println("View rights revoked successfully.");
        }
    }

    
 // Add a new special access group to the system
    public long createSpecialAccessGroup(String groupName, String createdBy) throws SQLException {
        String query = "INSERT INTO article_groups (name, is_special_access) VALUES (?, TRUE)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, groupName);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long groupId = rs.getLong(1);
                    // Add the first instructor as an admin for this group
                    addSpecialAccessAdmin(groupId, createdBy);
                    System.out.println("Special access group created successfully.");
                    return groupId;
                }
            }
        }
        throw new SQLException("Failed to create special access group.");
    }

    
 // Add a user to a special access group with specified rights (admin, viewer, etc.)
    public void addUserToSpecialAccessGroup(long groupId, String userId, boolean isAdmin, boolean canView) throws SQLException {
        String query = "INSERT INTO special_access_group_rights (group_id, user_name, has_admin_rights, can_view_body, role_type) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE has_admin_rights = ?, can_view_body = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, groupId);
            stmt.setString(2, userId);
            stmt.setBoolean(3, isAdmin);
            stmt.setBoolean(4, canView);
            stmt.setString(5, isAdmin ? "ADMIN" : "INSTRUCTOR");
            stmt.setBoolean(6, isAdmin);
            stmt.setBoolean(7, canView);
            System.out.println("Executing query: " + query);
            stmt.executeUpdate();
            System.out.println("User added to special access group successfully.");
        }
    }

    public void removeUserFromSpecialAccessGroup(long groupId, String userId) throws SQLException {
        String query = "DELETE FROM special_access_group_rights WHERE group_id = ? AND user_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, groupId);
            stmt.setString(2, userId);
            System.out.println("Executing query: " + query);
            stmt.executeUpdate();
            System.out.println("User removed from special access group successfully.");
        }
    }

    // Check if a user is the last admin in a special access group
    private boolean isLastAdminInGroup(long groupId, String userId) throws SQLException {
        String query = "SELECT COUNT(*) AS adminCount FROM special_access_group_rights WHERE group_id = ? AND has_admin_rights = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("adminCount") == 1) {
                // If there's only one admin and it's the user being removed
                return isAdminInGroup(groupId, userId);
            }
        }
        return false;
    }

    private boolean isAdminInGroup(long groupId, String userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM special_access_group_rights WHERE group_id = ? AND user_name = ? AND has_admin_rights = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, groupId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    
 // Link help article to a special access group dynamically
    public void assignArticleToSpecialAccessGroup(long articleId, long groupId) throws SQLException {
        String query = "INSERT INTO help_article_groups (article_id, group_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, articleId);
            stmt.setLong(2, groupId);
            stmt.executeUpdate();
            System.out.println("Help article assigned to special access group successfully.");
        }
    }
 

	public void ensureConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			reconnect(); // Reconnect to the database if the connection is closed
		}
	}

	public void reconnect() {
		try {
			if (connection == null || connection.isClosed()) {
				connection = DriverManager.getConnection(JDBC_URL + "/compass360db", JDBC_USERNAME, JDBC_PASSWORD);
				System.out.println("Reconnected to the 'compass360db' database.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error reconnecting to the database.");
		}
	}

	// Close database connection
	public void close() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			connection.close();
		}
	}
	
	/**
	 * Retrieves the ID of a group by its name.
	 * 
	 * @param groupName The name of the group to look up
	 * @return The ID of the group
	 * @throws SQLException if there's an error executing the query or if the group doesn't exist
	 */
	public long getGroupIdByName(String groupName) throws SQLException {
	    String query = "SELECT group_id FROM article_groups WHERE name = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, groupName);
	        ResultSet rs = stmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getLong("group_id");
	        } else {
	            throw new SQLException("Group not found: " + groupName);
	        }
	    }
	}

	/**
	 * Retrieves information about all groups, including their members and special access status.
	 * 
	 * @return List of GroupInfo objects containing group details
	 * @throws SQLException if there's an error executing the query
	 */
	public List<GroupInfo> loadGroupData() throws SQLException {
	    List<GroupInfo> groups = new ArrayList<>();
	    
	    String query = """
	        SELECT 
	            ag.group_id,
	            ag.name,
	            ag.is_special_access,
	            COUNT(DISTINCT sagr.user_name) as member_count,
	            GROUP_CONCAT(DISTINCT 
	                CASE 
	                    WHEN sagr.has_admin_rights = TRUE THEN sagr.user_name 
	                END
	            ) as admin_users,
	            GROUP_CONCAT(DISTINCT 
	                CASE 
	                    WHEN sagr.can_view_body = TRUE THEN sagr.user_name 
	                END
	            ) as viewing_users
	        FROM 
	            article_groups ag
	        LEFT JOIN 
	            special_access_group_rights sagr ON ag.group_id = sagr.group_id
	        GROUP BY 
	            ag.group_id, ag.name, ag.is_special_access
	    """;
	    
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        
	        while (rs.next()) {
	            GroupInfo group = new GroupInfo(
	                rs.getLong("group_id"),
	                rs.getString("name"),
	                rs.getBoolean("is_special_access"),
	                rs.getInt("member_count")
	            );
	            
	            // Parse admin users
	            String adminUsers = rs.getString("admin_users");
	            if (adminUsers != null && !adminUsers.isEmpty()) {
	                group.setAdminUsers(Arrays.asList(adminUsers.split(",")));
	            }
	            
	            // Parse viewing users
	            String viewingUsers = rs.getString("viewing_users");
	            if (viewingUsers != null && !viewingUsers.isEmpty()) {
	                group.setViewingUsers(Arrays.asList(viewingUsers.split(",")));
	            }
	            
	            groups.add(group);
	        }
	    }
	    
	    return groups;
	}

	/**
	 * Gets members of a specific group with their roles and permissions.
	 * 
	 * @param groupId The ID of the group
	 * @return List of GroupMember objects containing member details
	 * @throws SQLException if there's an error executing the query
	 */
	public List<GroupMember> getGroupMembers(long groupId) throws SQLException {
	    List<GroupMember> members = new ArrayList<>();
	    
	    String query = """
	        SELECT 
	            u.userName,
	            u.firstName,
	            u.lastName,
	            sagr.has_admin_rights,
	            sagr.can_view_body,
	            sagr.role_type
	        FROM 
	            users u
	        JOIN 
	            special_access_group_rights sagr ON u.userName = sagr.user_name
	        WHERE 
	            sagr.group_id = ?
	    """;
	    
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setLong(1, groupId);
	        ResultSet rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	            GroupMember member = new GroupMember(
	                rs.getString("userName"),
	                rs.getString("firstName"),
	                rs.getString("lastName"),
	                rs.getBoolean("has_admin_rights"),
	                rs.getBoolean("can_view_body"),
	                rs.getString("role_type")
	            );
	            members.add(member);
	        }
	    }
	    
	    return members;
	}

	// Helper class to store group information
	public static class GroupInfo {
	    private final long id;
	    private final String name;
	    private final boolean isSpecialAccess;
	    private final int memberCount;
	    private List<String> adminUsers;
	    private List<String> viewingUsers;
	    
	    public GroupInfo(long id, String name, boolean isSpecialAccess, int memberCount) {
	        this.id = id;
	        this.name = name;
	        this.isSpecialAccess = isSpecialAccess;
	        this.memberCount = memberCount;
	        this.adminUsers = new ArrayList<>();
	        this.viewingUsers = new ArrayList<>();
	    }
	    
	    // Getters and setters
	    public long getId() { return id; }
	    public String getName() { return name; }
	    public boolean isSpecialAccess() { return isSpecialAccess; }
	    public int getMemberCount() { return memberCount; }
	    public List<String> getAdminUsers() { return adminUsers; }
	    public void setAdminUsers(List<String> adminUsers) { this.adminUsers = adminUsers; }
	    public List<String> getViewingUsers() { return viewingUsers; }
	    public void setViewingUsers(List<String> viewingUsers) { this.viewingUsers = viewingUsers; }
	}

	// Helper class to store group member information
	public static class GroupMember {
	    private final String username;
	    private final String firstName;
	    private final String lastName;
	    private final boolean hasAdminRights;
	    private final boolean canViewBody;
	    private final String roleType;
	    
	    public GroupMember(String username, String firstName, String lastName, 
	                      boolean hasAdminRights, boolean canViewBody, String roleType) {
	        this.username = username;
	        this.firstName = firstName;
	        this.lastName = lastName;
	        this.hasAdminRights = hasAdminRights;
	        this.canViewBody = canViewBody;
	        this.roleType = roleType;
	    }
	    
	    // Getters
	    public String getUsername() { return username; }
	    public String getFirstName() { return firstName; }
	    public String getLastName() { return lastName; }
	    public boolean isHasAdminRights() { return hasAdminRights; }
	    public boolean isCanViewBody() { return canViewBody; }
	    public String getRoleType() { return roleType; }
	    
	    public String getFullName() {
	        return firstName + " " + lastName;
	    }
	}

}
