package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HelpArticle implements Serializable {
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;
    private String id;
    private String header;
    private String articleLevel;
    private String title;
    private String shortDescription;
    private String keywords;
    private String body;
    private String encryptedBody;
    private String links;
    private String sensitiveTitle;
    private String sensitiveDescription;
    private List<String> groups;
    private String accessLevel;
    private boolean isSpecialAccess;
    private long specialAccessGroupId;
    private String createdBy;
    private Set<Role> allowedRoles;

    // Constructors
    public HelpArticle() {
        this.id = UUID.randomUUID().toString(); // Generate unique ID if not set explicitly
    }

    public HelpArticle(String id, String header, String level, String title, String shortDescription, String keywords,
                       String body, String encryptedBody, String links, String sensitiveTitle, String sensitiveDescription,
                       List<String> groups, String accessLevel, boolean isSpecialAccess, long specialAccessGroupId,
                       String createdBy, Set<Role> allowedRoles) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.header = header;
        this.articleLevel = level;
        this.title = title;
        this.shortDescription = shortDescription;
        this.keywords = keywords;
        this.body = body;
        this.encryptedBody = encryptedBody;
        this.links = links;
        this.sensitiveTitle = sensitiveTitle;
        this.sensitiveDescription = sensitiveDescription;
        this.groups = groups;
        this.accessLevel = accessLevel;
        this.isSpecialAccess = isSpecialAccess;
        this.specialAccessGroupId = specialAccessGroupId;
        this.createdBy = createdBy;
        this.allowedRoles = allowedRoles;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getLevel() {
        return articleLevel;
    }

    public void setLevel(String level) {
        this.articleLevel = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getEncryptedBody() {
        return encryptedBody;
    }

    public void setEncryptedBody(String encryptedBody) {
        this.encryptedBody = encryptedBody;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public String getSensitiveTitle() {
        return sensitiveTitle;
    }

    public void setSensitiveTitle(String sensitiveTitle) {
        this.sensitiveTitle = sensitiveTitle;
    }

    public String getSensitiveDescription() {
        return sensitiveDescription;
    }

    public void setSensitiveDescription(String sensitiveDescription) {
        this.sensitiveDescription = sensitiveDescription;
    }

    public List<String> getGroups() {
        return groups;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean isSpecialAccess() {
        return isSpecialAccess;
    }

    public void setSpecialAccess(boolean specialAccess) {
        isSpecialAccess = specialAccess;
    }

    public long getSpecialAccessGroupId() {
        return specialAccessGroupId;
    }

    public void setSpecialAccessGroupId(long specialAccessGroupId) {
        this.specialAccessGroupId = specialAccessGroupId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Set<Role> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(Set<Role> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }
    
    public String getGroupsCol() {
        // Convert the list of groups to a comma-separated string
        return groups != null ? String.join(",", groups) : "";
    }

    public void updateGroupsCol(String newGroup) {
        if (newGroup == null || newGroup.trim().isEmpty()) {
            return; // Do nothing for empty groups
        }

        if (groups == null) {
            groups = new ArrayList<>(); // Initialize if null
        }

        // Avoid duplicates
        if (!groups.contains(newGroup)) {
            groups.add(newGroup);
        }
    }

    public void setGroups(List<String> groups) {
        // Ensure the list is modifiable
        if (groups == null) {
            this.groups = new ArrayList<>();
        } else {
            this.groups = new ArrayList<>(groups); // Copy the list to ensure it's modifiable
        }
    }


}
