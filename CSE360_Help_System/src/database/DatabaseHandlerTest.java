//package database;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.sql.SQLException;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//import model.HelpArticle;
//import model.Role;
//import model.User;
//
//public class DatabaseHandlerTest {
//    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        DatabaseHandler dbHandler = null;
//        try {
//            dbHandler = new DatabaseHandler();
//
//            // 1. Test Initial Admin Account Creation
//            System.out.println("== Initial Admin Account Creation ==");
//            if (dbHandler.isUserTableEmpty()) {
//                User adminUser = new User("adminUser", "adminPassword".toCharArray());
//                dbHandler.insertInitialAdmin(adminUser);
//            } else {
//                System.out.println("Admin account creation skipped as users already exist.");
//            }
//
//            // 2. Test New User Creation Using Invitation
//            System.out.println("\n== Create New User with Invitation ==");
//            String invitationCode = UUID.randomUUID().toString();
//            List<Role> roles = Arrays.asList(Role.STUDENT, Role.TA);
//            dbHandler.createUser("newUser", invitationCode, roles);
//            dbHandler.updateUserPassword("newUser", "", "password123");
//            dbHandler.setupUserProfile("newUser", "newuser@example.com", "John", "Doe", "Alexander", "Johnny");
//            System.out.println("User profile setup completed.");
//
//            // 3. Test Backup and Restore Operations on Help Articles
//            System.out.println("\n== Setting Up Sample Help Articles and Testing Backup and Restore ==");
//            HelpArticle article1 = new HelpArticle(1, "Header1", "beginner", "Title1", "ShortDesc1", "Keywords1",
//                    "Body1", "Links1", "SensitiveTitle1", "SensitiveDesc1", Arrays.asList("Eclipse"), "PUBLIC");
//            HelpArticle article2 = new HelpArticle(2, "Header2", "advanced", "Title2", "ShortDesc2", "Keywords2",
//                    "Body2", "Links2", "SensitiveTitle2", "SensitiveDesc2", Arrays.asList("IntelliJ"), "RESTRICTED");
//            HelpArticle article3 = new HelpArticle(3, "Header3", "expert", "Title3", "ShortDesc3", "Keywords3",
//                    "Body3", "Links3", "SensitiveTitle3", "SensitiveDesc3", Arrays.asList("Eclipse", "H2"), "CONFIDENTIAL");
//
//            dbHandler.addHelpArticle(article1, true);
//            dbHandler.addHelpArticle(article2, true);
//            dbHandler.addHelpArticle(article3, true);
//            System.out.println("Sample articles added.");
//            
//        
//
//            // 4. Perform Group-Based Backups
//            System.out.println("\n== Performing Group-Based Backups ==");
//            dbHandler.backupHelpSystemData("admin_backup_Eclipse", Arrays.asList("Eclipse"), Role.ADMIN);
//            dbHandler.backupHelpSystemData("admin_backup_IntelliJ", Arrays.asList("IntelliJ"), Role.ADMIN);
//            dbHandler.backupHelpSystemData("admin_backup_Eclipse_H2", Arrays.asList("Eclipse", "H2"), Role.ADMIN);
//            System.out.println("Backups completed.");
//
//            // 5. Verify and Perform Restore Operations
//            System.out.println("\n== Verifying Backup Files and Performing Restores ==");
//            restoreAndListIfFileExists(dbHandler, "admin_backup_Eclipse_backup.dat", true, "Eclipse group backup (overwrite)");
//            restoreAndListIfFileExists(dbHandler, "admin_backup_IntelliJ_backup.dat", false, "IntelliJ group backup (merge with existing)");
//            restoreAndListIfFileExists(dbHandler, "admin_backup_Eclipse_H2_backup.dat", false, "Eclipse and H2 groups backup (merge with existing)");
//
//            // 6. Role-Based Article Search
//            System.out.println("\n== Testing Role-Based Article Search ==");
//            testRoleBasedSearch(dbHandler, Role.STUDENT, "Student");
//            testRoleBasedSearch(dbHandler, Role.TA, "TA");
//            testRoleBasedSearch(dbHandler, Role.ADMIN, "Admin");
//
//            // 7. Update Articles
//            System.out.println("\n== Testing Update Permission for Admin and Instructor ==");
//            article1.setTitle("Updated Title by Admin");
//            dbHandler.updateHelpArticle(article1, Role.ADMIN);
//            article2.setTitle("Updated Title by Instructor");
//            dbHandler.updateHelpArticle(article2, Role.INSTRUCTOR);
//            listAllArticles(dbHandler, "All articles after updates by Admin and Instructor");
//
//            // 8. Delete Articles
//            System.out.println("\n== Testing Delete Permission for Admin and Instructor ==");
//            dbHandler.deleteHelpArticle(article1.getId(), Role.ADMIN);
//            dbHandler.deleteHelpArticle(article2.getId(), Role.INSTRUCTOR);
//            listAllArticles(dbHandler, "All articles after deletions by Admin and Instructor");
//
//            // 9. Final Listing of All Articles
//            System.out.println("\n== Final Listing of All Articles ==");
//            listAllArticles(dbHandler, "Final state of all articles");
//
//            // 10. List All User Accounts
//            System.out.println("\n== List All User Accounts ==");
//            dbHandler.listAllUserAccounts();
//
//            // 11. Test Delete User Account
//            System.out.println("\n== Delete User Account ==");
//            dbHandler.deleteUserAccount("newUser");
//            System.out.println("User 'newUser' deleted.");
//
//            // 12. Final Listing of All Users
//            System.out.println("\n== List All User Accounts After Deletion ==");
//            dbHandler.listAllUserAccounts();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            if (dbHandler != null) {
//                try {
//                    dbHandler.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    /**
//     * Restores articles from a backup file if it exists and lists all articles after restore.
//     *
//     * @param dbHandler The database handler.
//     * @param filename The name of the backup file.
//     * @param overwrite Whether to overwrite existing data.
//     * @param description A description of the restore operation.
//     * @throws SQLException
//     * @throws IOException
//     * @throws ClassNotFoundException
//     */
//    private static void restoreAndListIfFileExists(DatabaseHandler dbHandler, String filename, boolean overwrite, String description)
//            throws SQLException, IOException, ClassNotFoundException {
//        if (Files.exists(Paths.get(filename))) {
//            System.out.println("\n== Restoring from " + description + " ==");
//            dbHandler.restoreHelpSystemData(Collections.singletonList(filename), overwrite, Role.ADMIN);
//            listAllArticles(dbHandler, "Articles after restoring from " + description);
//        } else {
//            System.out.println("Backup file " + filename + " not found, skipping restore.");
//        }
//    }
//
//    /**
//     * Tests role-based article search and prints the number of accessible articles.
//     *
//     * @param dbHandler The database handler.
//     * @param role The role for testing.
//     * @param roleName The name of the role for display.
//     * @throws SQLException
//     */
//    private static void testRoleBasedSearch(DatabaseHandler dbHandler, Role role, String roleName) throws SQLException {
//        List<HelpArticle> accessibleArticles = dbHandler.searchHelpArticles("Title", null, null, role);
//        System.out.println(roleName + " can access " + accessibleArticles.size() + " articles.");
//    }
//
//    /**
//     * Lists all articles in the database and prints each article's details.
//     *
//     * @param dbHandler The database handler.
//     * @param description A description of the current state for display.
//     * @throws SQLException
//     */
//    private static void listAllArticles(DatabaseHandler dbHandler, String description) throws SQLException {
//        System.out.println("\n== " + description + " ==");
//        List<HelpArticle> allArticles = dbHandler.searchHelpArticles("", null, null, Role.ADMIN);
//        for (HelpArticle article : allArticles) {
//            System.out.println("ID: " + article.getId() + ", Title: " + article.getTitle() + ", Access Level: " + article.getAccessLevel());
//        }
//    }
//}
