package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotNull;
import rs.ac.uns.ftn.pkisystem.entity.RevocationReason;

public class RevokeCertificateRequest {
    @NotNull
    private RevocationReason reason;

    private String description;

    public RevokeCertificateRequest() {}

    public RevokeCertificateRequest(RevocationReason reason, String description) {
        this.reason = reason;
        this.description = description;
    }

    public RevocationReason getReason() { return reason; }
    public void setReason(RevocationReason reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}