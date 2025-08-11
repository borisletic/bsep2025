package rs.ac.uns.ftn.pkisystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.pkisystem.dto.ApiResponse;
import rs.ac.uns.ftn.pkisystem.dto.UserDTO;
import rs.ac.uns.ftn.pkisystem.dto.UserTokenDTO;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;
import rs.ac.uns.ftn.pkisystem.service.UserService;
import rs.ac.uns.ftn.pkisystem.service.UserTokenService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'END_USER')")
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserTokenService userTokenService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile() {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                    .orElseThrow(() -> new SecurityException("User not authenticated"));

            UserDTO userDTO = userService.convertToDTO(currentUser);
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", userDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tokens")
    public ResponseEntity<ApiResponse<List<UserTokenDTO>>> getActiveTokens() {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                    .orElseThrow(() -> new SecurityException("User not authenticated"));

            // Get current JTI from request header (would need to be implemented)
            String currentJti = ""; // Extract from JWT token

            List<UserTokenDTO> tokens = userTokenService.getActiveTokensForUser(currentUser, currentJti);
            return ResponseEntity.ok(ApiResponse.success("Active tokens retrieved successfully", tokens));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/tokens/{tokenId}/revoke")
    public ResponseEntity<ApiResponse<String>> revokeToken(@PathVariable String tokenId) {
        try {
            userTokenService.revokeToken(tokenId);
            return ResponseEntity.ok(ApiResponse.success("Token revoked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/tokens/revoke-all")
    public ResponseEntity<ApiResponse<String>> revokeAllTokens() {
        try {
            User currentUser = SecurityUtils.getCurrentUser()
                    .orElseThrow(() -> new SecurityException("User not authenticated"));

            userTokenService.revokeAllTokensForUser(currentUser);
            return ResponseEntity.ok(ApiResponse.success("All tokens revoked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}