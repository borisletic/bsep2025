package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.Certificate;
import rs.ac.uns.ftn.pkisystem.entity.CertificateTemplate;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "certificate_templates")
class CertificateTemplate {
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

    // Constructors, getters and setters
    public CertificateTemplate() {}

    // All getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Certificate getCaCertificate() { return caCertificate; }
    public void setCaCertificate(Certificate caCertificate) { this.caCertificate = caCertificate; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

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

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {

    List<CertificateTemplate> findByOwner(User owner);

    List<CertificateTemplate> findByCaCertificate(Certificate caCertificate);

    Optional<CertificateTemplate> findByNameAndOwner(String name, User owner);

    @Query("SELECT ct FROM CertificateTemplate ct WHERE ct.owner = :owner OR " +
            "(:userRole = 'ADMIN')")
    List<CertificateTemplate> findAccessibleTemplates(@Param("owner") User owner, @Param("userRole") String userRole);

    boolean existsByNameAndOwner(String name, User owner);
}