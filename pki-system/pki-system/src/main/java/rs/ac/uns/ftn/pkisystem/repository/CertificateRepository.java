package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.Certificate;
import rs.ac.uns.ftn.pkisystem.entity.CertificateType;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findBySerialNumber(String serialNumber);

    List<Certificate> findByOwner(User owner);

    List<Certificate> findByOwnerAndCertificateType(User owner, CertificateType certificateType);

    List<Certificate> findByIssuer(Certificate issuer);

    List<Certificate> findByCertificateType(CertificateType certificateType);

    List<Certificate> findByRevokedTrue();

    List<Certificate> findByRevokedFalse();

    @Query("SELECT c FROM Certificate c WHERE c.validTo < :now AND c.revoked = false")
    List<Certificate> findExpiredCertificates(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Certificate c WHERE c.validTo BETWEEN :now AND :warningDate AND c.revoked = false")
    List<Certificate> findCertificatesExpiringBefore(@Param("now") LocalDateTime now, @Param("warningDate") LocalDateTime warningDate);

    @Query("SELECT c FROM Certificate c WHERE c.commonName LIKE %:commonName%")
    List<Certificate> findByCommonNameContaining(@Param("commonName") String commonName);

    @Query("SELECT c FROM Certificate c WHERE c.organization = :organization")
    List<Certificate> findByOrganization(@Param("organization") String organization);

    // Get certificate chain
    @Query(
            value = "WITH RECURSIVE cert_chain AS (" +
                    "SELECT c.id, c.serial_number, c.common_name, c.issuer_id, 0 as level " +
                    "FROM certificates c WHERE c.id = :certificateId " +
                    "UNION ALL " +
                    "SELECT c.id, c.serial_number, c.common_name, c.issuer_id, cc.level + 1 " +
                    "FROM certificates c INNER JOIN cert_chain cc ON c.id = cc.issuer_id) " +
                    "SELECT * FROM cert_chain ORDER BY level DESC",
            nativeQuery = true
    )
    List<Object[]> getCertificateChain(@Param("certificateId") Long certificateId);

    // Get all certificates that user can access (for CA users)
    @Query("SELECT c FROM Certificate c WHERE " +
            "(:userRole = 'ADMIN') OR " +
            "(:userRole = 'CA_USER' AND c.owner = :user) OR " +
            "(:userRole = 'END_USER' AND c.owner = :user AND c.certificateType = 'END_ENTITY')")
    List<Certificate> findAccessibleCertificates(@Param("user") User user, @Param("userRole") String userRole);

    // Get CA certificates that user can use for signing
    @Query("SELECT c FROM Certificate c WHERE " +
            "c.certificateType IN ('ROOT', 'INTERMEDIATE') AND c.revoked = false AND c.validTo > :now AND " +
            "((:userRole = 'ADMIN') OR (:userRole = 'CA_USER' AND c.owner = :user))")
    List<Certificate> findSigningCertificates(@Param("user") User user, @Param("userRole") String userRole, @Param("now") LocalDateTime now);
}