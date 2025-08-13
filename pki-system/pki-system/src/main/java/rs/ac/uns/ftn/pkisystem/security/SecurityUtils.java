package rs.ac.uns.ftn.pkisystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rs.ac.uns.ftn.pkisystem.entity.User;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }

        return Optional.empty();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().map(User::getEmail).orElse(null);
    }

    public static boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(user -> user.getRole().name().equals("ADMIN"))
                .orElse(false);
    }
}