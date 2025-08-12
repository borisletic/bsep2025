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

    public void saveToken(User user, String jti, String token) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1); // 24 hours

        UserToken userToken = new UserToken(user, jti, expiresAt);

        // Set device information (this would be extracted from request in real implementation)
        // For now, setting basic info
        userToken.setDeviceType("Web");
        userToken.setBrowser("Unknown");
        userToken.setOperatingSystem("Unknown");

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
            token.setIpAddress(getClientIpAddress(request));
            token.setUserAgent(request.getHeader("User-Agent"));
            userTokenRepository.save(token);
        }
    }

    public List<UserTokenDTO> getActiveTokensForUser(User user, String currentJti) {
        List<UserToken> activeTokens = userTokenRepository.findActiveTokensByUser(user, LocalDateTime.now());

        return activeTokens.stream().map(token -> {
            UserTokenDTO dto = new UserTokenDTO();
            dto.setId(token.getId());
            dto.setTokenId(token.getTokenId());
            dto.setIpAddress(token.getIpAddress());
            dto.setDeviceType(token.getDeviceType());
            dto.setBrowser(token.getBrowser());
            dto.setOperatingSystem(token.getOperatingSystem());
            dto.setLocation(token.getLocation());
            dto.setLastActivity(token.getLastActivity());
            dto.setCreatedAt(token.getCreatedAt());
            dto.setExpiresAt(token.getExpiresAt());
            dto.setRevoked(token.isRevoked());
            dto.setCurrent(token.getTokenId().equals(currentJti));
            return dto;
        }).collect(Collectors.toList());
    }

    public void revokeToken(String jti) {
        userTokenRepository.revokeToken(jti);
    }

    public void revokeAllTokensForUser(User user) {
        userTokenRepository.revokeAllTokensForUser(user);
    }

    public void cleanupExpiredTokens() {
        userTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Dodati ove metode u postojeći UserTokenService.java

    public void revokeAllOtherTokensForUser(User user, String currentJti) {
        List<UserToken> allTokens = userTokenRepository.findActiveTokensByUser(user, LocalDateTime.now());

        for (UserToken token : allTokens) {
            if (!token.getTokenId().equals(currentJti)) {
                token.setRevoked(true);
                userTokenRepository.save(token);
            }
        }
    }

    public void saveTokenWithDeviceInfo(User user, String jti, String token, HttpServletRequest request) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1); // 24 hours

        UserToken userToken = new UserToken(user, jti, expiresAt);

        // Extract device information from request
        String userAgent = request.getHeader("User-Agent");
        userToken.setUserAgent(userAgent);
        userToken.setIpAddress(getClientIpAddress(request));

        // Parse user agent to get device info
        DeviceInfo deviceInfo = parseUserAgent(userAgent);
        userToken.setDeviceType(deviceInfo.getDeviceType());
        userToken.setBrowser(deviceInfo.getBrowser());
        userToken.setOperatingSystem(deviceInfo.getOperatingSystem());

        userTokenRepository.save(userToken);
    }

    private DeviceInfo parseUserAgent(String userAgent) {
        if (userAgent == null) {
            return new DeviceInfo("Unknown", "Unknown", "Unknown");
        }

        String deviceType = "Desktop";
        String browser = "Unknown";
        String os = "Unknown";

        // Simple user agent parsing
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            deviceType = "Mobile";
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            deviceType = "Tablet";
        }

        if (userAgent.contains("Chrome")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            browser = "Safari";
        } else if (userAgent.contains("Edge")) {
            browser = "Edge";
        }

        if (userAgent.contains("Windows")) {
            os = "Windows";
        } else if (userAgent.contains("Mac")) {
            os = "macOS";
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
        } else if (userAgent.contains("Android")) {
            os = "Android";
        } else if (userAgent.contains("iOS")) {
            os = "iOS";
        }

        return new DeviceInfo(deviceType, browser, os);
    }

    // Helper class for device information
    private static class DeviceInfo {
        private final String deviceType;
        private final String browser;
        private final String operatingSystem;

        public DeviceInfo(String deviceType, String browser, String operatingSystem) {
            this.deviceType = deviceType;
            this.browser = browser;
            this.operatingSystem = operatingSystem;
        }

        public String getDeviceType() { return deviceType; }
        public String getBrowser() { return browser; }
        public String getOperatingSystem() { return operatingSystem; }
    }
}