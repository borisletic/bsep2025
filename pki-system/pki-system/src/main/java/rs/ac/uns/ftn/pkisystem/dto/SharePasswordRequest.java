package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SharePasswordRequest {
    @Email
    @NotBlank
    private String userEmail;

    @NotBlank
    private String encryptedPassword; // Encrypted with target user's public key

    public SharePasswordRequest() {}

    public SharePasswordRequest(String userEmail, String encryptedPassword) {
        this.userEmail = userEmail;
        this.encryptedPassword = encryptedPassword;
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
}