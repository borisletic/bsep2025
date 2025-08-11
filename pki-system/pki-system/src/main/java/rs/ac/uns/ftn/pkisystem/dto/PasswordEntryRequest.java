package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordEntryRequest {
    @NotBlank
    private String siteName;

    private String siteUrl;

    @NotBlank
    private String username;

    @NotBlank
    private String encryptedPassword; // Encrypted with user's public key

    private String description;

    public PasswordEntryRequest() {}

    // Getters and setters
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getSiteUrl() { return siteUrl; }
    public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}