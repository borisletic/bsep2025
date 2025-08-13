package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.Certificate;
import rs.ac.uns.ftn.pkisystem.entity.CertificateType;
import rs.ac.uns.ftn.pkisystem.entity.CertificateStatus;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByOwner(User owner);
    List<Certificate> findByType(CertificateType type);
    List<Certificate> findByStatus(CertificateStatus status);
    Optional<Certificate> findBySerialNumber(String serialNumber);

    @Query("SELECT c FROM Certificate c WHERE c.owner = :owner OR c.issuer IN " +
            "(SELECT cert FROM Certificate cert WHERE cert.owner = :owner)")
    List<Certificate> findCertificatesAccessibleByUser(@Param("owner") User owner);

    @Query("SELECT c FROM Certificate c WHERE c.type = 'ROOT' OR c.type = 'INTERMEDIATE'")
    List<Certificate> findCACertificates();
}