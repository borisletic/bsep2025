package rs.ac.uns.ftn.pkisystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rs.ac.uns.ftn.pkisystem.entity.Role;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public static boolean isCurrentUserCAUser() {
        return getCurrentUser()
                .map(user -> user.getRole() == Role.CA_USER || user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public static boolean isCurrentUserEndUser() {
        return getCurrentUser()
                .map(user -> user.getRole() == Role.END_USER)
                .orElse(false);
    }

    public static boolean canAccessCertificate(User certificateOwner) {
        return getCurrentUser()
                .map(currentUser -> {
                    if (currentUser.getRole() == Role.ADMIN) {
                        return true;
                    }
                    if (currentUser.getRole() == Role.CA_USER) {
                        return currentUser.getId().equals(certificateOwner.getId());
                    }
                    if (currentUser.getRole() == Role.END_USER) {
                        return currentUser.getId().equals(certificateOwner.getId());
                    }
                    return false;
                })
                .orElse(false);
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser()
                .map(User::getEmail)
                .orElse("anonymous");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser()
                .map(User::getId)
                .orElse(null);
    }
}