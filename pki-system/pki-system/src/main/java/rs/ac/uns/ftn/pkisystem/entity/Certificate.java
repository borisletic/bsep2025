package rs.ac.uns.ftn.pkisystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "certificates")
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private String subjectDN;

    @Column(nullable = false)
    private String issuerDN;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    @Enumerated(EnumType.STRING)
    private CertificateType type;

    @Enumerated(EnumType.STRING)
    private CertificateStatus status = CertificateStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String certificateData; // Base64 encoded certificate

    @Column(name = "keystore_alias")
    private String keystoreAlias;

    @Column(name = "keystore_password")
    private String keystorePassword; // Encrypted

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id")
    private Certificate issuer; // Parent certificate for chain

    @OneToMany(mappedBy = "issuer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Certificate> issuedCertificates = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "revocation_reason")
    private String revocationReason;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Certificate() {}

    public Certificate(String serialNumber, String subjectDN, String issuerDN,
                       LocalDateTime validFrom, LocalDateTime validTo, CertificateType type) {
        this.serialNumber = serialNumber;
        this.subjectDN = subjectDN;
        this.issuerDN = issuerDN;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.type = type;
    }

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

    public String getKeystoreAlias() { return keystoreAlias; }
    public void setKeystoreAlias(String keystoreAlias) { this.keystoreAlias = keystoreAlias; }

    public String getKeystorePassword() { return keystorePassword; }
    public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }

    public Certificate getIssuer() { return issuer; }
    public void setIssuer(Certificate issuer) { this.issuer = issuer; }

    public List<Certificate> getIssuedCertificates() { return issuedCertificates; }
    public void setIssuedCertificates(List<Certificate> issuedCertificates) { this.issuedCertificates = issuedCertificates; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == CertificateStatus.ACTIVE &&
                now.isAfter(validFrom) &&
                now.isBefore(validTo);
    }
}