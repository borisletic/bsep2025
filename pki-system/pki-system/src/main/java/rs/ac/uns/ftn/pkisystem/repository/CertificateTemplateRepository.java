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

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {

    List<CertificateTemplate> findByOwner(User owner);

    List<CertificateTemplate> findByCaCertificate(Certificate caCertificate);

    Optional<CertificateTemplate> findByNameAndOwner(String name, User owner);

    @Query("SELECT ct FROM CertificateTemplate ct WHERE ct.owner = :owner OR (:userRole = 'ADMIN')")
    List<CertificateTemplate> findAccessibleTemplates(@Param("owner") User owner, @Param("userRole") String userRole);

    boolean existsByNameAndOwner(String name, User owner);
}