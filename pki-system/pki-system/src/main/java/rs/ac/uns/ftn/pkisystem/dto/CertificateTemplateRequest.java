package rs.ac.uns.ftn.pkisystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class CertificateTemplateRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long caCertificateId;

    private String commonNameRegex;
    private String subjectAlternativeNamesRegex;

    @Min(1)
    @Max(3650)
    private Integer maxValidityDays = 365;

    private String keyUsage;
    private String extendedKeyUsage;
    private String basicConstraints;

    public CertificateTemplateRequest() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCaCertificateId() { return caCertificateId; }
    public void setCaCertificateId(Long caCertificateId) { this.caCertificateId = caCertificateId; }

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
}