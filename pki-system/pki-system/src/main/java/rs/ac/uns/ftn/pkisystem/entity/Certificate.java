package rs.ac.uns.ftn.pkisystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "certificates")
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    @NotBlank
    private String commonName;

    private String organization;
    private String organizationalUnit;
    private String country;
    private String state;
    private String locality;
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CertificateType certificateType;

    @NotNull
    private LocalDateTime validFrom;

    @NotNull
    private LocalDateTime validTo;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @ManyToOne
    @JoinColumn(name = "issuer_id")
    private Certificate issuer;

    @OneToMany(mappedBy = "issuer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Certificate> issuedCertificates = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Certificate status
    private boolean revoked = false;

    @Enumerated(EnumType.STRING)
    private RevocationReason revocationReason;

    private LocalDateTime revocationDate;

    // Certificate data
    @Column(columnDefinition = "TEXT", nullable = false)
    private String certificateData; // PEM format

    @Column(columnDefinition = "TEXT", nullable = false)
    private String publicKey; // PEM format

    // Extensions (stored as JSON)
    @Column(columnDefinition = "TEXT")
    private String keyUsage;

    @Column(columnDefinition = "TEXT")
    private String extendedKeyUsage;

    @Column(columnDefinition = "TEXT")
    private String basicConstraints;

    @Column(columnDefinition = "TEXT")
    private String subjectAlternativeNames;

    // Keystore information (only for CA certificates)
    private String keystorePath;

    @Column(columnDefinition = "TEXT")
    private String keystorePasswordEncrypted;

    private String keystoreAlias;

    // Constructors
    public Certificate() {}

    // Getters and Setters
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

    public Certificate getIssuer() { return issuer; }
    public void setIssuer(Certificate issuer) { this.issuer = issuer; }

    public List<Certificate> getIssuedCertificates() { return issuedCertificates; }
    public void setIssuedCertificates(List<Certificate> issuedCertificates) { this.issuedCertificates = issuedCertificates; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

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

    public String getKeyUsage() { return keyUsage; }
    public void setKeyUsage(String keyUsage) { this.keyUsage = keyUsage; }

    public String getExtendedKeyUsage() { return extendedKeyUsage; }
    public void setExtendedKeyUsage(String extendedKeyUsage) { this.extendedKeyUsage = extendedKeyUsage; }

    public String getBasicConstraints() { return basicConstraints; }
    public void setBasicConstraints(String basicConstraints) { this.basicConstraints = basicConstraints; }

    public String getSubjectAlternativeNames() { return subjectAlternativeNames; }
    public void setSubjectAlternativeNames(String subjectAlternativeNames) { this.subjectAlternativeNames = subjectAlternativeNames; }

    public String getKeystorePath() { return keystorePath; }
    public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }

    public String getKeystorePasswordEncrypted() { return keystorePasswordEncrypted; }
    public void setKeystorePasswordEncrypted(String keystorePasswordEncrypted) { this.keystorePasswordEncrypted = keystorePasswordEncrypted; }

    public String getKeystoreAlias() { return keystoreAlias; }
    public void setKeystoreAlias(String keystoreAlias) { this.keystoreAlias = keystoreAlias; }

    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return !revoked && now.isAfter(validFrom) && now.isBefore(validTo);
    }

    public boolean isCertificateAuthority() {
        return certificateType == CertificateType.ROOT || certificateType == CertificateType.INTERMEDIATE;
    }
}