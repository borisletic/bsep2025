package rs.ac.uns.ftn.pkisystem.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.pkisystem.dto.*;
import rs.ac.uns.ftn.pkisystem.service.PasswordManagerService;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
@PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'END_USER')")
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000", "http://localhost:5173", "https://localhost:5173"})
public class PasswordManagerController {

    @Autowired
    private PasswordManagerService passwordManagerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PasswordEntryDTO>>> getPasswordEntries() {
        try {
            List<PasswordEntryDTO> entries = passwordManagerService.getPasswordEntriesForCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("Password entries retrieved successfully", entries));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PasswordEntryDTO>> getPasswordEntry(@PathVariable Long id) {
        try {
            PasswordEntryDTO entry = passwordManagerService.getPasswordEntry(id);
            return ResponseEntity.ok(ApiResponse.success("Password entry retrieved successfully", entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PasswordEntryDTO>> createPasswordEntry(@Valid @RequestBody PasswordEntryRequest request) {
        try {
            PasswordEntryDTO entry = passwordManagerService.createPasswordEntry(request);
            return ResponseEntity.ok(ApiResponse.success("Password entry created successfully", entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PasswordEntryDTO>> updatePasswordEntry(
            @PathVariable Long id,
            @Valid @RequestBody PasswordEntryRequest request) {
        try {
            PasswordEntryDTO entry = passwordManagerService.updatePasswordEntry(id, request);
            return ResponseEntity.ok(ApiResponse.success("Password entry updated successfully", entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePasswordEntry(@PathVariable Long id) {
        try {
            passwordManagerService.deletePasswordEntry(id);
            return ResponseEntity.ok(ApiResponse.success("Password entry deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<PasswordEntryDTO>> sharePassword(
            @PathVariable Long id,
            @Valid @RequestBody SharePasswordRequest request) {
        try {
            PasswordEntryDTO entry = passwordManagerService.sharePassword(id, request);
            return ResponseEntity.ok(ApiResponse.success("Password shared successfully", entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/share/{userId}")
    public ResponseEntity<ApiResponse<String>> unsharePassword(
            @PathVariable Long id,
            @PathVariable Long userId) {
        try {
            passwordManagerService.unsharePassword(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Password unshared successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PasswordEntryDTO>>> searchPasswordEntries(@RequestParam String query) {
        try {
            List<PasswordEntryDTO> entries = passwordManagerService.searchPasswordEntries(query);
            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", entries));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}