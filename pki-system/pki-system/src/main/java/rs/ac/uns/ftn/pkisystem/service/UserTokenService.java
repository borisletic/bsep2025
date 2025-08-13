package rs.ac.uns.ftn.pkisystem.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.pkisystem.dto.UserTokenDTO;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.entity.UserToken;
import rs.ac.uns.ftn.pkisystem.repository.UserTokenRepository;
import rs.ac.uns.ftn.pkisystem.security.JwtUtil;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserTokenService {

    @Autowired
    private UserTokenRepository userTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditService auditService;

    public void saveToken(User user, String jti, String token) {
        LocalDateTime expiresAt = jwtUtil.getExpirationAsLocalDateTime(token);

        UserToken userToken = new UserToken(user, jti, expiresAt);

        // TODO: Extract device information from request headers
        userToken.setDeviceType("Web");
        userToken.setBrowser("Unknown");
        userToken.setOperatingSystem("Unknown");
        userToken.setIpAddress("Unknown");

        userTokenRepository.save(userToken);
    }

    public boolean isTokenActive(String jti) {
        Optional<UserToken> token = userTokenRepository.findByTokenId(jti);
        return token.isPresent() && !token.get().isRevoked() && !token.get().isExpired();
    }

    public void updateLastActivity(String jti, HttpServletRequest request) {
        Optional<UserToken> tokenOpt = userTokenRepository.findByTokenId(jti);
        if (tokenOpt.isPresent()) {
            UserToken token = tokenOpt.get();
            token.setLastActivity(LocalDateTime.now());

            // Update IP address from request
            String ipAddress = getClientIpAddress(request);
            token.setIpAddress(ipAddress);

            userTokenRepository.save(token);
        }
    }

    public List<UserTokenDTO> getUserTokens() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        List<UserToken> tokens = userTokenRepository.findByUserAndRevokedFalse(currentUser);

        return tokens.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void revokeToken(String tokenId) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        UserToken token = userTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        if (!token.getUser().equals(currentUser)) {
            throw new SecurityException("Access denied");
        }

        token.setRevoked(true);
        userTokenRepository.save(token);

        auditService.logEvent("TOKEN_REVOKED", "User token revoked", "TOKEN", token.getId());
    }

    public void revokeAllUserTokens(User user) {
        List<UserToken> tokens = userTokenRepository.findByUserAndRevokedFalse(user);
        tokens.forEach(token -> token.setRevoked(true));
        userTokenRepository.saveAll(tokens);

        auditService.logEvent("ALL_TOKENS_REVOKED", "All user tokens revoked", "USER", user.getId());
    }

    private UserTokenDTO convertToDTO(UserToken token) {
        UserTokenDTO dto = new UserTokenDTO();
        dto.setId(token.getId());
        dto.setTokenId(token.getTokenId());
        dto.setDeviceType(token.getDeviceType());
        dto.setBrowser(token.getBrowser());
        dto.setOperatingSystem(token.getOperatingSystem());
        dto.setIpAddress(token.getIpAddress());
        dto.setCreatedAt(token.getCreatedAt());
        dto.setLastActivity(token.getLastActivity());
        dto.setExpiresAt(token.getExpiresAt());
        dto.setCurrent(false); // TODO: Determine if this is the current token
        return dto;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}