package rs.ac.uns.ftn.pkisystem.dto;

import rs.ac.uns.ftn.pkisystem.entity.CertificateType;
import rs.ac.uns.ftn.pkisystem.entity.RevocationReason;

import java.time.LocalDateTime;

public class CertificateDTO {
    private Long id;
    private String serialNumber;
    private String commonName;
    private String organization;
    private String organizationalUnit;
    private String country;
    private String state;
    private String locality;
    private String email;
    private CertificateType certificateType;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime createdAt;
    private boolean revoked;
    private RevocationReason revocationReason;
    private LocalDateTime revocationDate;
    private String certificateData;
    private String publicKey;

    // Issuer information
    private Long issuerId;
    private String issuerCommonName;
    private String issuerSerialNumber;

    // Owner information
    private Long ownerId;
    private String ownerEmail;
    private String ownerName;

    // Extensions
    private String keyUsage;
    private String extendedKeyUsage;
    private String basicConstraints;
    private String subjectAlternativeNames;

    public CertificateDTO() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getOrganizationalUnit() { return organizationalUnit; }
    public void setOrganizationalUnit(String organizationalUnit) { this.organizationalUnit = organizationalUnit; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public CertificateType getCertificateType() { return certificateType; }
    public void setCertificateType(CertificateType certificateType) { this.certificateType = certificateType; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public RevocationReason getRevocationReason() { return revocationReason; }
    public void setRevocationReason(RevocationReason revocationReason) { this.revocationReason = revocationReason; }

    public LocalDateTime getRevocationDate() { return revocationDate; }
    public void setRevocationDate(LocalDateTime revocationDate) { this.revocationDate = revocationDate; }

    public String getCertificateData() { return certificateData; }
    public void setCertificateData(String certificateData) { this.certificateData = certificateData; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public Long getIssuerId() { return issuerId; }
    public void setIssuerId(Long issuerId) { this.issuerId = issuerId; }

    public String getIssuerCommonName() { return issuerCommonName; }
    public void setIssuerCommonName(String issuerCommonName) { this.issuerCommonName = issuerCommonName; }

    public String getIssuerSerialNumber() { return issuerSerialNumber; }
    public void setIssuerSerialNumber(String issuerSerialNumber) { this.issuerSerialNumber = issuerSerialNumber; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getKeyUsage() { return keyUsage; }
    public void setKeyUsage(String keyUsage) { this.keyUsage = keyUsage; }

    public String getExtendedKeyUsage() { return extendedKeyUsage; }
    public void setExtendedKeyUsage(String extendedKeyUsage) { this.extendedKeyUsage = extendedKeyUsage; }

    public String getBasicConstraints() { return basicConstraints; }
    public void setBasicConstraints(String basicConstraints) { this.basicConstraints = basicConstraints; }

    public String getSubjectAlternativeNames() { return subjectAlternativeNames; }
    public void setSubjectAlternativeNames(String subjectAlternativeNames) { this.subjectAlternativeNames = subjectAlternativeNames; }
}