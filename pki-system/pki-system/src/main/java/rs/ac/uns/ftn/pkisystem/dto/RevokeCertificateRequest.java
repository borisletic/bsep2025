package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotBlank;

public class RevokeCertificateRequest {
    @NotBlank(message = "Revocation reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}