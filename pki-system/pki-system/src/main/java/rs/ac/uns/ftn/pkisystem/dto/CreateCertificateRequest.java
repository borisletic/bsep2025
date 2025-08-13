package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import rs.ac.uns.ftn.pkisystem.entity.CertificateType;
import java.time.LocalDateTime;

public class CreateCertificateRequest {
    @NotBlank(message = "Subject DN is required")
    private String subjectDN;

    @NotNull(message = "Certificate type is required")
    private CertificateType type;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid to date is required")
    @Future(message = "Valid to date must be in the future")
    private LocalDateTime validTo;

    private Long issuerId; // For intermediate and end-entity certificates

    private String keyUsage;
    private String extendedKeyUsage;
    private boolean basicConstraintsCA;
    private Integer pathLengthConstraint;

    // For end-entity certificates with CSR
    private String csrData;
    private String privateKeyData;

    // Getters and Setters
    public String getSubjectDN() { return subjectDN; }
    public void setSubjectDN(String subjectDN) { this.subjectDN = subjectDN; }

    public CertificateType getType() { return type; }
    public void setType(CertificateType type) { this.type = type; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }

    public Long getIssuerId() { return issuerId; }
    public void setIssuerId(Long issuerId) { this.issuerId = issuerId; }

    public String getKeyUsage() { return keyUsage; }
    public void setKeyUsage(String keyUsage) { this.keyUsage = keyUsage; }

    public String getExtendedKeyUsage() { return extendedKeyUsage; }
    public void setExtendedKeyUsage(String extendedKeyUsage) { this.extendedKeyUsage = extendedKeyUsage; }

    public boolean isBasicConstraintsCA() { return basicConstraintsCA; }
    public void setBasicConstraintsCA(boolean basicConstraintsCA) { this.basicConstraintsCA = basicConstraintsCA; }

    public Integer getPathLengthConstraint() { return pathLengthConstraint; }
    public void setPathLengthConstraint(Integer pathLengthConstraint) { this.pathLengthConstraint = pathLengthConstraint; }

    public String getCsrData() { return csrData; }
    public void setCsrData(String csrData) { this.csrData = csrData; }

    public String getPrivateKeyData() { return privateKeyData; }
    public void setPrivateKeyData(String privateKeyData) { this.privateKeyData = privateKeyData; }
}