package rs.ac.uns.ftn.pkisystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_templates")
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "ca_certificate_id", nullable = false)
    private Certificate caCertificate;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String commonNameRegex;
    private String subjectAlternativeNamesRegex;
    private Integer maxValidityDays = 365;

    @Column(columnDefinition = "TEXT")
    private String keyUsage;

    @Column(columnDefinition = "TEXT")
    private String extendedKeyUsage;

    @Column(columnDefinition = "TEXT")
    private String basicConstraints;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public CertificateTemplate() {}

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Certificate getCaCertificate() {
        return caCertificate;
    }

    public void setCaCertificate(Certificate caCertificate) {
        this.caCertificate = caCertificate;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getCommonNameRegex() {
        return commonNameRegex;
    }

    public void setCommonNameRegex(String commonNameRegex) {
        this.commonNameRegex = commonNameRegex;
    }

    public String getSubjectAlternativeNamesRegex() {
        return subjectAlternativeNamesRegex;
    }

    public void setSubjectAlternativeNamesRegex(String subjectAlternativeNamesRegex) {
        this.subjectAlternativeNamesRegex = subjectAlternativeNamesRegex;
    }

    public Integer getMaxValidityDays() {
        return maxValidityDays;
    }

    public void setMaxValidityDays(Integer maxValidityDays) {
        this.maxValidityDays = maxValidityDays;
    }

    public String getKeyUsage() {
        return keyUsage;
    }

    public void setKeyUsage(String keyUsage) {
        this.keyUsage = keyUsage;
    }

    public String getExtendedKeyUsage() {
        return extendedKeyUsage;
    }

    public void setExtendedKeyUsage(String extendedKeyUsage) {
        this.extendedKeyUsage = extendedKeyUsage;
    }

    public String getBasicConstraints() {
        return basicConstraints;
    }

    public void setBasicConstraints(String basicConstraints) {
        this.basicConstraints = basicConstraints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}