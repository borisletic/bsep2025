package rs.ac.uns.ftn.pkisystem.dto;

public class AuthenticationResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserDTO user;

    public AuthenticationResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}