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

    List<PasswordEntry> findBySiteName(String siteName);

    @Query("SELECT pe FROM PasswordEntry pe WHERE pe.owner = :user OR " +
            "pe.id IN (SELECT ps.passwordEntry.id FROM PasswordShare ps WHERE ps.user = :user)")
    List<PasswordEntry> findAllAccessibleByUser(@Param("user") User user);

    @Query("SELECT pe FROM PasswordEntry pe WHERE " +
            "(pe.siteName LIKE %:searchTerm% OR pe.siteUrl LIKE %:searchTerm% OR pe.username LIKE %:searchTerm%) AND " +
            "(pe.owner = :user OR pe.id IN (SELECT ps.passwordEntry.id FROM PasswordShare ps WHERE ps.user = :user))")
    List<PasswordEntry> searchAccessibleByUser(@Param("user") User user, @Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(pe) FROM PasswordEntry pe WHERE pe.owner = :user")
    long countByOwner(@Param("user") User user);

    @Query("SELECT COUNT(ps) FROM PasswordShare ps WHERE ps.passwordEntry.id = :entryId")
    long countSharesForEntry(@Param("entryId") Long entryId);
}