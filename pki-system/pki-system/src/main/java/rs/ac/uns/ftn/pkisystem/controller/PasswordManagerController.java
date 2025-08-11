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
@CrossOrigin(origins = {"https://localhost:3000", "https://127.0.0.1:3000"})
public class PasswordManagerController {

    @Autowired
    private PasswordManagerService passwordManagerService;

    @PostMapping
    public ResponseEntity<ApiResponse<PasswordEntryDTO>> createPasswordEntry(@Valid @RequestBody PasswordEntryRequest request) {
        try {
            PasswordEntryDTO passwordEntry = passwordManagerService.createPasswordEntry(request);
            return ResponseEntity.ok(ApiResponse.success("Password entry created successfully", passwordEntry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PasswordEntryDTO>>> getPasswordEntries() {
        try {
            List<PasswordEntryDTO> passwordEntries = passwordManagerService.getPasswordEntriesForCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("Password entries retrieved successfully", passwordEntries));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PasswordEntryDTO>> getPasswordEntry(@PathVariable Long id) {
        try {
            PasswordEntryDTO passwordEntry = passwordManagerService.getPasswordEntryById(id);
            return ResponseEntity.ok(ApiResponse.success("Password entry retrieved successfully", passwordEntry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PasswordEntryDTO>> updatePasswordEntry(
            @PathVariable Long id,
            @Valid @RequestBody PasswordEntryRequest request) {
        try {
            PasswordEntryDTO passwordEntry = passwordManagerService.updatePasswordEntry(id, request);
            return ResponseEntity.ok(ApiResponse.success("Password entry updated successfully", passwordEntry));
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
    public ResponseEntity<ApiResponse<String>> sharePassword(
            @PathVariable Long id,
            @Valid @RequestBody SharePasswordRequest request) {
        try {
            passwordManagerService.sharePassword(id, request);
            return ResponseEntity.ok(ApiResponse.success("Password shared successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/share/{userEmail}")
    public ResponseEntity<ApiResponse<String>> removePasswordShare(
            @PathVariable Long id,
            @PathVariable String userEmail) {
        try {
            passwordManagerService.removePasswordShare(id, userEmail);
            return ResponseEntity.ok(ApiResponse.success("Password share removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PasswordEntryDTO>>> searchPasswordEntries(@RequestParam String query) {
        try {
            List<PasswordEntryDTO> passwordEntries = passwordManagerService.searchPasswordEntries(query);
            return ResponseEntity.ok(ApiResponse.success("Password entries found", passwordEntries));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}