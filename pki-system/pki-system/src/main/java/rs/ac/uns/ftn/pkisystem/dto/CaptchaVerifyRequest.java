package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotBlank;

public class CaptchaVerifyRequest {
    @NotBlank
    private String sessionId;

    @NotBlank
    private String answer;

    public CaptchaVerifyRequest() {}

    public CaptchaVerifyRequest(String sessionId, String answer) {
        this.sessionId = sessionId;
        this.answer = answer;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}