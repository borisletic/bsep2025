package rs.ac.uns.ftn.pkisystem.dto;

import rs.ac.uns.ftn.pkisystem.entity.CertificateType;
import rs.ac.uns.ftn.pkisystem.entity.CertificateStatus;
import java.time.LocalDateTime;

public class CertificateDTO {
    private Long id;
    private String serialNumber;
    private String subjectDN;
    private String issuerDN;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private CertificateType type;
    private CertificateStatus status;
    private String certificateData;
    private UserDTO owner;
    private Long issuerId;
    private String revocationReason;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getSubjectDN() { return subjectDN; }
    public void setSubjectDN(String subjectDN) { this.subjectDN = subjectDN; }

    public String getIssuerDN() { return issuerDN; }
    public void setIssuerDN(String issuerDN) { this.issuerDN = issuerDN; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }

    public CertificateType getType() { return type; }
    public void setType(CertificateType type) { this.type = type; }

    public CertificateStatus getStatus() { return status; }
    public void setStatus(CertificateStatus status) { this.status = status; }

    public String getCertificateData() { return certificateData; }
    public void setCertificateData(String certificateData) { this.certificateData = certificateData; }

    public UserDTO getOwner() { return owner; }
    public void setOwner(UserDTO owner) { this.owner = owner; }

    public Long getIssuerId() { return issuerId; }
    public void setIssuerId(Long issuerId) { this.issuerId = issuerId; }

    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}