package rs.ac.uns.ftn.pkisystem.dto;

import java.time.LocalDateTime;

public class CertificateTemplateDTO {
    private Long id;
    private String name;
    private String description;
    private Long caCertificateId;
    private String caCertificateCommonName;
    private String commonNameRegex;
    private String subjectAlternativeNamesRegex;
    private Integer maxValidityDays;
    private String keyUsage;
    private String extendedKeyUsage;
    private String basicConstraints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerEmail;

    public CertificateTemplateDTO() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCaCertificateId() { return caCertificateId; }
    public void setCaCertificateId(Long caCertificateId) { this.caCertificateId = caCertificateId; }

    public String getCaCertificateCommonName() { return caCertificateCommonName; }
    public void setCaCertificateCommonName(String caCertificateCommonName) { this.caCertificateCommonName = caCertificateCommonName; }

    public String getCommonNameRegex() { return commonNameRegex; }
    public void setCommonNameRegex(String commonNameRegex) { this.commonNameRegex = commonNameRegex; }

    public String getSubjectAlternativeNamesRegex() { return subjectAlternativeNamesRegex; }
    public void setSubjectAlternativeNamesRegex(String subjectAlternativeNamesRegex) { this.subjectAlternativeNamesRegex = subjectAlternativeNamesRegex; }

    public Integer getMaxValidityDays() { return maxValidityDays; }
    public void setMaxValidityDays(Integer maxValidityDays) { this.maxValidityDays = maxValidityDays; }

    public String getKeyUsage() { return keyUsage; }
    public void setKeyUsage(String keyUsage) { this.keyUsage = keyUsage; }

    public String getExtendedKeyUsage() { return extendedKeyUsage; }
    public void setExtendedKeyUsage(String extendedKeyUsage) { this.extendedKeyUsage = extendedKeyUsage; }

    public String getBasicConstraints() { return basicConstraints; }
    public void setBasicConstraints(String basicConstraints) { this.basicConstraints = basicConstraints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}