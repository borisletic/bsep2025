package rs.ac.uns.ftn.pkisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.pkisystem.entity.UserToken;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenId(String tokenId);
    List<UserToken> findByUserAndRevokedFalse(User user);
    void deleteByTokenId(String tokenId);
}