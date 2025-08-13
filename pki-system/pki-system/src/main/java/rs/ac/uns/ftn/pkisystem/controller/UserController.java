package rs.ac.uns.ftn.pkisystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.pkisystem.dto.ApiResponse;
import rs.ac.uns.ftn.pkisystem.dto.UserDTO;
import rs.ac.uns.ftn.pkisystem.dto.UserTokenDTO;
import rs.ac.uns.ftn.pkisystem.service.UserService;
import rs.ac.uns.ftn.pkisystem.service.UserTokenService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'END_USER')")
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000", "http://localhost:5173", "https://localhost:5173"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserTokenService userTokenService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile() {
        try {
            UserDTO user = userService.getCurrentUserProfile();
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tokens")
    public ResponseEntity<ApiResponse<List<UserTokenDTO>>> getUserTokens() {
        try {
            List<UserTokenDTO> tokens = userTokenService.getUserTokens();
            return ResponseEntity.ok(ApiResponse.success("Tokens retrieved successfully", tokens));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/tokens/{tokenId}")
    public ResponseEntity<ApiResponse<String>> revokeToken(@PathVariable String tokenId) {
        try {
            userTokenService.revokeToken(tokenId);
            return ResponseEntity.ok(ApiResponse.success("Token revoked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/end-entity")
    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getEndEntityUsers() {
        try {
            List<UserDTO> users = userService.getEndEntityUsers();
            return ResponseEntity.ok(ApiResponse.success("End entity users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}