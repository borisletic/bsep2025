package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.pkisystem.dto.PasswordEntryDTO;
import rs.ac.uns.ftn.pkisystem.dto.PasswordEntryRequest;
import rs.ac.uns.ftn.pkisystem.dto.PasswordShareDTO;
import rs.ac.uns.ftn.pkisystem.dto.SharePasswordRequest;
import rs.ac.uns.ftn.pkisystem.entity.PasswordEntry;
import rs.ac.uns.ftn.pkisystem.entity.PasswordShare;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.pkisystem.repository.PasswordEntryRepository;
import rs.ac.uns.ftn.pkisystem.repository.PasswordShareRepository;
import rs.ac.uns.ftn.pkisystem.repository.UserRepository;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PasswordManagerService {

    @Autowired
    private PasswordEntryRepository passwordEntryRepository;

    @Autowired
    private PasswordShareRepository passwordShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;

    public PasswordEntryDTO createPasswordEntry(PasswordEntryRequest request) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        PasswordEntry passwordEntry = new PasswordEntry();
        passwordEntry.setOwner(currentUser);
        passwordEntry.setSiteName(request.getSiteName());
        passwordEntry.setSiteUrl(request.getSiteUrl());
        passwordEntry.setUsername(request.getUsername());
        passwordEntry.setDescription(request.getDescription());

        passwordEntry = passwordEntryRepository.save(passwordEntry);

        // Create password share for the owner
        PasswordShare ownerShare = new PasswordShare(passwordEntry, currentUser, request.getEncryptedPassword());
        passwordShareRepository.save(ownerShare);

        auditService.logEvent("PASSWORD_CREATED",
                "Password entry created for: " + request.getSiteName(),
                "PASSWORD", passwordEntry.getId());

        return convertToDTO(passwordEntry);
    }

    public List<PasswordEntryDTO> getPasswordEntriesForCurrentUser() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Get owned entries
        List<PasswordEntry> ownedEntries = passwordEntryRepository.findByOwner(currentUser);

        // Get shared entries
        List<PasswordEntry> sharedEntries = passwordEntryRepository.findEntriesSharedWithUser(currentUser);

        // Combine and deduplicate
        List<PasswordEntry> allEntries = new ArrayList<>(ownedEntries);
        sharedEntries.stream()
                .filter(entry -> !ownedEntries.contains(entry))
                .forEach(allEntries::add);

        return allEntries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PasswordEntryDTO getPasswordEntry(Long id) {
        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Check if user has access
        if (!hasAccessToPasswordEntry(currentUser, passwordEntry)) {
            throw new SecurityException("Access denied to password entry");
        }

        return convertToDTO(passwordEntry);
    }

    public PasswordEntryDTO updatePasswordEntry(Long id, PasswordEntryRequest request) {
        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Only owner can update
        if (!passwordEntry.getOwner().equals(currentUser)) {
            throw new SecurityException("Only the owner can update password entry");
        }

        passwordEntry.setSiteName(request.getSiteName());
        passwordEntry.setSiteUrl(request.getSiteUrl());
        passwordEntry.setUsername(request.getUsername());
        passwordEntry.setDescription(request.getDescription());

        passwordEntry = passwordEntryRepository.save(passwordEntry);

        // Update owner's encrypted password
        PasswordShare ownerShare = passwordShareRepository.findByPasswordEntryAndUser(passwordEntry, currentUser)
                .orElseThrow(() -> new IllegalStateException("Owner share not found"));
        ownerShare.setEncryptedPassword(request.getEncryptedPassword());
        passwordShareRepository.save(ownerShare);

        auditService.logEvent("PASSWORD_UPDATED",
                "Password entry updated: " + request.getSiteName(),
                "PASSWORD", passwordEntry.getId());

        return convertToDTO(passwordEntry);
    }

    public void deletePasswordEntry(Long id) {
        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Only owner can delete
        if (!passwordEntry.getOwner().equals(currentUser)) {
            throw new SecurityException("Only the owner can delete password entry");
        }

        passwordEntryRepository.delete(passwordEntry);

        auditService.logEvent("PASSWORD_DELETED",
                "Password entry deleted: " + passwordEntry.getSiteName(),
                "PASSWORD", passwordEntry.getId());
    }

    public PasswordEntryDTO sharePassword(Long id, SharePasswordRequest request) {
        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Only owner can share
        if (!passwordEntry.getOwner().equals(currentUser)) {
            throw new SecurityException("Only the owner can share password entry");
        }

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        // Check if already shared
        if (passwordShareRepository.findByPasswordEntryAndUser(passwordEntry, targetUser).isPresent()) {
            throw new IllegalArgumentException("Password is already shared with this user");
        }

        // Create new share
        PasswordShare share = new PasswordShare(passwordEntry, targetUser, request.getEncryptedPassword());
        passwordShareRepository.save(share);

        auditService.logEvent("PASSWORD_SHARED",
                "Password shared with: " + targetUser.getEmail(),
                "PASSWORD", passwordEntry.getId());

        return convertToDTO(passwordEntry);
    }

    public void unsharePassword(Long id, Long userId) {
        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Only owner can unshare
        if (!passwordEntry.getOwner().equals(currentUser)) {
            throw new SecurityException("Only the owner can unshare password entry");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        // Cannot unshare from owner
        if (targetUser.equals(currentUser)) {
            throw new IllegalArgumentException("Cannot unshare from owner");
        }

        passwordShareRepository.deleteByPasswordEntryAndUser(passwordEntry, targetUser);

        auditService.logEvent("PASSWORD_UNSHARED",
                "Password unshared from: " + targetUser.getEmail(),
                "PASSWORD", passwordEntry.getId());
    }

    public List<User> getEligibleUsersForSharing() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Return all users except current user
        return userRepository.findAll().stream()
                .filter(user -> !user.equals(currentUser))
                .filter(User::isActivated)
                .collect(Collectors.toList());
    }

    private boolean hasAccessToPasswordEntry(User user, PasswordEntry passwordEntry) {
        // Owner has access
        if (passwordEntry.getOwner().equals(user)) {
            return true;
        }

        // Check if password is shared with user
        return passwordShareRepository.findByPasswordEntryAndUser(passwordEntry, user).isPresent();
    }

    private PasswordEntryDTO convertToDTO(PasswordEntry passwordEntry) {
        PasswordEntryDTO dto = new PasswordEntryDTO();
        dto.setId(passwordEntry.getId());
        dto.setSiteName(passwordEntry.getSiteName());
        dto.setSiteUrl(passwordEntry.getSiteUrl());
        dto.setUsername(passwordEntry.getUsername());
        dto.setDescription(passwordEntry.getDescription());
        dto.setOwner(userService.convertToDTO(passwordEntry.getOwner()));
        dto.setCreatedAt(passwordEntry.getCreatedAt());
        dto.setUpdatedAt(passwordEntry.getUpdatedAt());

        // Convert shares
        List<PasswordShareDTO> shares = passwordEntry.getShares().stream()
                .map(this::convertShareToDTO)
                .collect(Collectors.toList());
        dto.setShares(shares);

        return dto;
    }

    private PasswordShareDTO convertShareToDTO(PasswordShare share) {
        PasswordShareDTO dto = new PasswordShareDTO();
        dto.setId(share.getId());
        dto.setUser(userService.convertToDTO(share.getUser()));
        dto.setEncryptedPassword(share.getEncryptedPassword());
        dto.setCreatedAt(share.getCreatedAt());
        return dto;
    }

    public List<PasswordEntryDTO> searchPasswordEntries(String query) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Get all accessible entries first
        List<PasswordEntry> ownedEntries = passwordEntryRepository.findByOwner(currentUser);
        List<PasswordEntry> sharedEntries = passwordEntryRepository.findEntriesSharedWithUser(currentUser);

        // Combine and deduplicate
        List<PasswordEntry> allEntries = new ArrayList<>(ownedEntries);
        sharedEntries.stream()
                .filter(entry -> !ownedEntries.contains(entry))
                .forEach(allEntries::add);

        // Filter by search query (case insensitive)
        String lowerQuery = query.toLowerCase().trim();
        List<PasswordEntry> filteredEntries = allEntries.stream()
                .filter(entry ->
                        entry.getSiteName().toLowerCase().contains(lowerQuery) ||
                                entry.getUsername().toLowerCase().contains(lowerQuery) ||
                                (entry.getSiteUrl() != null && entry.getSiteUrl().toLowerCase().contains(lowerQuery)) ||
                                (entry.getDescription() != null && entry.getDescription().toLowerCase().contains(lowerQuery))
                )
                .collect(Collectors.toList());

        return filteredEntries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}