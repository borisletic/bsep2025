package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.entity.Role;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByActivationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    List<User> findByRole(Role role);
}