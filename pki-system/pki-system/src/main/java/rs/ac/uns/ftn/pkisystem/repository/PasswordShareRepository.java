package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.PasswordEntry;
import rs.ac.uns.ftn.pkisystem.entity.PasswordShare;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordShareRepository extends JpaRepository<PasswordShare, Long> {

    List<PasswordShare> findByPasswordEntry(PasswordEntry passwordEntry);

    List<PasswordShare> findByUser(User user);

    Optional<PasswordShare> findByPasswordEntryAndUser(PasswordEntry passwordEntry, User user);

    boolean existsByPasswordEntryAndUser(PasswordEntry passwordEntry, User user);

    @Query("SELECT ps FROM PasswordShare ps WHERE ps.passwordEntry.owner = :owner")
    List<PasswordShare> findAllSharedByOwner(@Param("owner") User owner);

    @Query("SELECT ps.user FROM PasswordShare ps WHERE ps.passwordEntry = :entry")
    List<User> findUsersWithAccessToEntry(@Param("entry") PasswordEntry entry);

    void deleteByPasswordEntryAndUser(PasswordEntry passwordEntry, User user);

    void deleteByPasswordEntry(PasswordEntry passwordEntry);
}