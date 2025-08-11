package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.pkisystem.dto.PasswordEntryDTO;
import rs.ac.uns.ftn.pkisystem.dto.PasswordEntryRequest;
import rs.ac.uns.ftn.pkisystem.dto.SharePasswordRequest;
import rs.ac.uns.ftn.pkisystem.dto.UserDTO;
import rs.ac.uns.ftn.pkisystem.entity.PasswordEntry;
import rs.ac.uns.ftn.pkisystem.entity.PasswordShare;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.pkisystem.exception.UserNotFoundException;
import rs.ac.uns.ftn.pkisystem.repository.PasswordEntryRepository;
import rs.ac.uns.ftn.pkisystem.repository.PasswordShareRepository;
import rs.ac.uns.ftn.pkisystem.repository.UserRepository;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;

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

        auditService.logEvent("PASSWORD_ENTRY_CREATED",
                "Password entry created for site: " + request.getSiteName(),
                "PASSWORD_ENTRY", passwordEntry.getId());

        return convertToDTO(passwordEntry, currentUser);
    }

    public List<PasswordEntryDTO> getPasswordEntriesForCurrentUser() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        List<PasswordEntry> passwordEntries = passwordEntryRepository.findAllAccessibleByUser(currentUser);

        return passwordEntries.stream()
                .map(entry -> convertToDTO(entry, currentUser))
                .collect(Collectors.toList());
    }

    public PasswordEntryDTO getPasswordEntryById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        // Check if user has access to this entry
        if (!hasAccessToEntry(passwordEntry, currentUser)) {
            throw new SecurityException("Access denied to password entry");
        }

        auditService.logEvent("PASSWORD_ENTRY_VIEWED",
                "Password entry viewed: " + passwordEntry.getSiteName(),
                "PASSWORD_ENTRY", passwordEntry.getId());

        return convertToDTO(passwordEntry, currentUser);
    }

    public PasswordEntryDTO updatePasswordEntry(Long id, PasswordEntryRequest request) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        // Only owner can update
        if (!passwordEntry.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only owner can update password entry");
        }

        passwordEntry.setSiteName(request.getSiteName());
        passwordEntry.setSiteUrl(request.getSiteUrl());
        passwordEntry.setUsername(request.getUsername());
        passwordEntry.setDescription(request.getDescription());

        passwordEntry = passwordEntryRepository.save(passwordEntry);

        // Update owner's encrypted password
        PasswordShare ownerShare = passwordShareRepository.findByPasswordEntryAndUser(passwordEntry, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Password share not found"));
        ownerShare.setEncryptedPassword(request.getEncryptedPassword());
        passwordShareRepository.save(ownerShare);

        auditService.logEvent("PASSWORD_ENTRY_UPDATED",
                "Password entry updated: " + passwordEntry.getSiteName(),
                "PASSWORD_ENTRY", passwordEntry.getId());

        return convertToDTO(passwordEntry, currentUser);
    }

    public void deletePasswordEntry(Long id) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        // Only owner can delete
        if (!passwordEntry.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only owner can delete password entry");
        }

        passwordEntryRepository.delete(passwordEntry);

        auditService.logEvent("PASSWORD_ENTRY_DELETED",
                "Password entry deleted: " + passwordEntry.getSiteName(),
                "PASSWORD_ENTRY", passwordEntry.getId());
    }

    public void sharePassword(Long passwordEntryId, SharePasswordRequest request) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        PasswordEntry passwordEntry = passwordEntryRepository.findById(passwordEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        // Only owner can share
        if (!passwordEntry.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only owner can share password entry");
        }

        User targetUser = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getUserEmail()));

        // Check if already shared
        if (passwordShareRepository.existsByPasswordEntryAndUser(passwordEntry, targetUser)) {
            throw new IllegalStateException("Password is already shared with this user");
        }

        PasswordShare passwordShare = new PasswordShare(passwordEntry, targetUser, request.getEncryptedPassword());
        passwordShareRepository.save(passwordShare);

        auditService.logEvent("PASSWORD_SHARED",
                "Password shared with user: " + targetUser.getEmail() + " for site: " + passwordEntry.getSiteName(),
                "PASSWORD_ENTRY", passwordEntry.getId());
    }

    public void removePasswordShare(Long passwordEntryId, String userEmail) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        PasswordEntry passwordEntry = passwordEntryRepository.findById(passwordEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Password entry not found"));

        // Only owner can remove shares
        if (!passwordEntry.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only owner can remove password shares");
        }

        User targetUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        passwordShareRepository.deleteByPasswordEntryAndUser(passwordEntry, targetUser);

        auditService.logEvent("PASSWORD_SHARE_REVOKED",
                "Password share revoked for user: " + userEmail + " for site: " + passwordEntry.getSiteName(),
                "PASSWORD_ENTRY", passwordEntry.getId());
    }

    public List<PasswordEntryDTO> searchPasswordEntries(String query) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        List<PasswordEntry> passwordEntries = passwordEntryRepository.searchAccessibleByUser(currentUser, query);

        return passwordEntries.stream()
                .map(entry -> convertToDTO(entry, currentUser))
                .collect(Collectors.toList());
    }

    private boolean hasAccessToEntry(PasswordEntry passwordEntry, User user) {
        return passwordEntry.getOwner().getId().equals(user.getId()) ||
                passwordShareRepository.existsByPasswordEntryAndUser(passwordEntry, user);
    }

    private PasswordEntryDTO convertToDTO(PasswordEntry passwordEntry, User currentUser) {
        PasswordEntryDTO dto = new PasswordEntryDTO();
        dto.setId(passwordEntry.getId());
        dto.setSiteName(passwordEntry.getSiteName());
        dto.setSiteUrl(passwordEntry.getSiteUrl());
        dto.setUsername(passwordEntry.getUsername());
        dto.setDescription(passwordEntry.getDescription());
        dto.setCreatedAt(passwordEntry.getCreatedAt());
        dto.setUpdatedAt(passwordEntry.getUpdatedAt());
        dto.setOwner(passwordEntry.getOwner().getId().equals(currentUser.getId()));
        dto.setOwnerEmail(passwordEntry.getOwner().getEmail());

        // Get encrypted password for current user
        passwordShareRepository.findByPasswordEntryAndUser(passwordEntry, currentUser)
                .ifPresent(share -> dto.setEncryptedPassword(share.getEncryptedPassword()));

        // Get list of users this entry is shared with (only for owner)
        if (dto.isOwner()) {
            List<User> sharedUsers = passwordShareRepository.findUsersWithAccessToEntry(passwordEntry);
            List<UserDTO> sharedUserDTOs = sharedUsers.stream()
                    .filter(user -> !user.getId().equals(currentUser.getId())) // Exclude owner
                    .map(userService::convertToDTO)
                    .collect(Collectors.toList());
            dto.setSharedWith(sharedUserDTOs);
        }

        return dto;
    }
}

