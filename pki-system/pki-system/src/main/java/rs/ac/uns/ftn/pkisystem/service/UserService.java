package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.pkisystem.dto.UserDTO;
import rs.ac.uns.ftn.pkisystem.entity.Role;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.pkisystem.repository.UserRepository;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    public UserDTO getCurrentUserProfile() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
        return convertToDTO(currentUser);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getEndEntityUsers() {
        List<User> users = userRepository.findByRole(Role.END_USER);
        return users.stream()
                .filter(User::isActivated)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Cannot delete self
        if (user.equals(currentUser)) {
            throw new IllegalArgumentException("Cannot delete your own account");
        }

        // Cannot delete other admins unless you're an admin
        if (user.getRole() == Role.ADMIN && currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Cannot delete admin user");
        }

        userRepository.delete(user);
        auditService.logEvent("USER_DELETED", "User deleted: " + user.getEmail(), "USER", id);
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setOrganization(user.getOrganization());
        dto.setRole(user.getRole());
        dto.setActivated(user.isActivated());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}