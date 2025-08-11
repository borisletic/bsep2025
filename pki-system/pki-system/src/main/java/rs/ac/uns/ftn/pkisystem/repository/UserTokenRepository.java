package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.entity.UserToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByTokenId(String tokenId);

    List<UserToken> findByUser(User user);

    List<UserToken> findByUserAndRevokedFalse(User user);

    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user AND ut.revoked = false AND ut.expiresAt > :now")
    List<UserToken> findActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT ut FROM UserToken ut WHERE ut.expiresAt < :now")
    List<UserToken> findExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM UserToken ut WHERE ut.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserToken ut SET ut.revoked = true WHERE ut.user = :user")
    void revokeAllTokensForUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE UserToken ut SET ut.revoked = true WHERE ut.tokenId = :tokenId")
    void revokeToken(@Param("tokenId") String tokenId);

    @Query("SELECT COUNT(ut) FROM UserToken ut WHERE ut.user = :user AND ut.revoked = false AND ut.expiresAt > :now")
    long countActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
}