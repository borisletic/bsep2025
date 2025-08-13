package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.PasswordEntry;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.util.List;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
    List<PasswordEntry> findByOwner(User owner);

    @Query("SELECT DISTINCT pe FROM PasswordEntry pe " +
            "JOIN pe.shares ps " +
            "WHERE ps.user = :user")
    List<PasswordEntry> findEntriesSharedWithUser(@Param("user") User user);
}