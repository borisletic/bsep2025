package rs.ac.uns.ftn.pkisystem.dto;

public class CaptchaResponse {
    private String sessionId;
    private String challenge;
    private String imageBase64; // Optional: for image-based CAPTCHA

    public CaptchaResponse() {}

    public CaptchaResponse(String sessionId, String challenge) {
        this.sessionId = sessionId;
        this.challenge = challenge;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getChallenge() { return challenge; }
    public void setChallenge(String challenge) { this.challenge = challenge; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}