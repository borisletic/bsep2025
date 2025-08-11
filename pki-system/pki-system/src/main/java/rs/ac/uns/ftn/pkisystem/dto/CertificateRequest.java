package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.ac.uns.ftn.pkisystem.entity.CertificateType;

public class CertificateRequest {
    @NotBlank
    private String commonName;

    private String organization;
    private String organizationalUnit;
    private String country;
    private String state;
    private String locality;
    private String email;

    @NotNull
    private CertificateType certificateType;

    @NotNull
    private Integer validityDays;

    private Long issuerId; // Certificate that will sign this certificate

    private Long templateId; // Optional template

    // Extensions
    private String keyUsage;
    private String extendedKeyUsage;
    private String basicConstraints;
    private String subjectAlternativeNames;

    // CSR data (for end-entity certificates)
    private String csrData;

    public CertificateRequest() {}

    // Getters and setters
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

    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }

    public Long getIssuerId() { return issuerId; }
    public void setIssuerId(Long issuerId) { this.issuerId = issuerId; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getKeyUsage() { return keyUsage; }
    public void setKeyUsage(String keyUsage) { this.keyUsage = keyUsage; }

    public String getExtendedKeyUsage() { return extendedKeyUsage; }
    public void setExtendedKeyUsage(String extendedKeyUsage) { this.extendedKeyUsage = extendedKeyUsage; }

    public String getBasicConstraints() { return basicConstraints; }
    public void setBasicConstraints(String basicConstraints) { this.basicConstraints = basicConstraints; }

    public String getSubjectAlternativeNames() { return subjectAlternativeNames; }
    public void setSubjectAlternativeNames(String subjectAlternativeNames) { this.subjectAlternativeNames = subjectAlternativeNames; }

    public String getCsrData() { return csrData; }
    public void setCsrData(String csrData) { this.csrData = csrData; }
}