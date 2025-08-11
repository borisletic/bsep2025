package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.Role;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByActivationToken(String activationToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);

    List<User> findByRole(Role role);

    List<User> findByActivatedTrue();

    List<User> findByActivatedFalse();

    @Query("SELECT u FROM User u WHERE u.activationTokenExpiry < :now AND u.activated = false")
    List<User> findExpiredUnactivatedUsers(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.passwordResetTokenExpiry < :now AND u.passwordResetToken IS NOT NULL")
    List<User> findExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.organization LIKE %:organization%")
    List<User> findByOrganizationContaining(@Param("organization") String organization);
}