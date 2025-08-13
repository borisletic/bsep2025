package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SharePasswordRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Encrypted password is required")
    private String encryptedPassword;

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
}