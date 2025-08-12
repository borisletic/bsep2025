package rs.ac.uns.ftn.pkisystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.pkisystem.dto.ApiResponse;
import rs.ac.uns.ftn.pkisystem.dto.UserTokenDTO;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.security.JwtUtil;
import rs.ac.uns.ftn.pkisystem.service.AuditService;
import rs.ac.uns.ftn.pkisystem.service.UserService;
import rs.ac.uns.ftn.pkisystem.service.UserTokenService;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000"})
public class UserTokenController {

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditService auditService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserTokenDTO>>> getActiveTokens(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());

            // Get current JWT token ID from request
            String currentJti = jwtUtil.getJtiFromAuthentication(authentication);

            List<UserTokenDTO> activeTokens = userTokenService.getActiveTokensForUser(user, currentJti);

            return ResponseEntity.ok(ApiResponse.success("Active tokens retrieved", activeTokens));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve active tokens: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{tokenId}")
    public ResponseEntity<ApiResponse<String>> revokeToken(
            @PathVariable String tokenId,
            Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());

            // Verify that the token belongs to the current user
            List<UserTokenDTO> userTokens = userTokenService.getActiveTokensForUser(user, null);
            boolean tokenBelongsToUser = userTokens.stream()
                    .anyMatch(token -> token.getTokenId().equals(tokenId));

            if (!tokenBelongsToUser) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Token not found or does not belong to current user"));
            }

            userTokenService.revokeToken(tokenId);

            auditService.logEvent("TOKEN_REVOKED", "Token revoked manually", "USER", user.getId(), user.getEmail());

            return ResponseEntity.ok(ApiResponse.success("Token revoked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to revoke token: " + e.getMessage()));
        }
    }

    @DeleteMapping("/all-others")
    public ResponseEntity<ApiResponse<String>> revokeAllOtherTokens(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            String currentJti = jwtUtil.getJtiFromAuthentication(authentication);

            userTokenService.revokeAllOtherTokensForUser(user, currentJti);

            auditService.logEvent("ALL_OTHER_TOKENS_REVOKED", "All other tokens revoked", "USER", user.getId(), user.getEmail());

            return ResponseEntity.ok(ApiResponse.success("All other sessions terminated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to revoke tokens: " + e.getMessage()));
        }
    }
}