package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequest {
    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}