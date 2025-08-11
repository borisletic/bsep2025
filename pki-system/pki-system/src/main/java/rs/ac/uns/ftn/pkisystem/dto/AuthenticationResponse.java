package rs.ac.uns.ftn.pkisystem.dto;

import rs.ac.uns.ftn.pkisystem.entity.Role;

public class AuthenticationResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String organization;
    private Role role;
    private boolean activated;

    public AuthenticationResponse() {}

    public AuthenticationResponse(String token, Long userId, String email, String firstName,
                                  String lastName, String organization, Role role, boolean activated) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
        this.role = role;
        this.activated = activated;
    }

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }
}