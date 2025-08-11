package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthenticationRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String captchaToken;

    private String captchaAnswer;

    public AuthenticationRequest() {}

    public AuthenticationRequest(String email, String password, String captchaToken, String captchaAnswer) {
        this.email = email;
        this.password = password;
        this.captchaToken = captchaToken;
        this.captchaAnswer = captchaAnswer;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCaptchaToken() { return captchaToken; }
    public void setCaptchaToken(String captchaToken) { this.captchaToken = captchaToken; }

    public String getCaptchaAnswer() { return captchaAnswer; }
    public void setCaptchaAnswer(String captchaAnswer) { this.captchaAnswer = captchaAnswer; }
}