package rs.ac.uns.ftn.pkisystem.security;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordStrengthValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9]");

    private static final String[] COMMON_PASSWORDS = {
            "password", "123456", "123456789", "qwerty", "abc123",
            "password123", "admin", "letmein", "welcome", "monkey"
    };

    public ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();
        int score = 0;

        if (password == null || password.isEmpty()) {
            errors.add("Password is required");
            return new ValidationResult(false, 0, errors);
        }

        // Length check
        if (password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        } else {
            score += 1;
        }

        // Character variety checks
        if (!LOWERCASE.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter");
        } else {
            score += 1;
        }

        if (!UPPERCASE.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter");
        } else {
            score += 1;
        }

        if (!DIGIT.matcher(password).find()) {
            errors.add("Password must contain at least one digit");
        } else {
            score += 1;
        }

        if (!SPECIAL.matcher(password).find()) {
            errors.add("Password must contain at least one special character");
        } else {
            score += 1;
        }

        // Common password check
        for (String common : COMMON_PASSWORDS) {
            if (password.toLowerCase().contains(common)) {
                errors.add("Password contains common patterns and is not secure");
                break;
            }
        }

        // Additional length bonus
        if (password.length() >= 12) {
            score += 1;
        }

        boolean isValid = errors.isEmpty() && score >= 4;
        return new ValidationResult(isValid, score, errors);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final int score;
        private final List<String> errors;

        public ValidationResult(boolean valid, int score, List<String> errors) {
            this.valid = valid;
            this.score = score;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public int getScore() { return score; }
        public List<String> getErrors() { return errors; }
    }
}