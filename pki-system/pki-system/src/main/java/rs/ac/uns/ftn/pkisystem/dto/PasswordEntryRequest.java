package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordEntryRequest {
    @NotBlank(message = "Site name is required")
    private String siteName;

    private String siteUrl;

    @NotBlank(message = "Username is required")
    private String username;

    private String description;

    @NotBlank(message = "Encrypted password is required")
    private String encryptedPassword;

    // Getters and Setters
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getSiteUrl() { return siteUrl; }
    public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
}