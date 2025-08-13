package rs.ac.uns.ftn.pkisystem.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PasswordEntryDTO {
    private Long id;
    private String siteName;
    private String siteUrl;
    private String username;
    private String description;
    private UserDTO owner;
    private List<PasswordShareDTO> shares;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getSiteUrl() { return siteUrl; }
    public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UserDTO getOwner() { return owner; }
    public void setOwner(UserDTO owner) { this.owner = owner; }

    public List<PasswordShareDTO> getShares() { return shares; }
    public void setShares(List<PasswordShareDTO> shares) { this.shares = shares; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}