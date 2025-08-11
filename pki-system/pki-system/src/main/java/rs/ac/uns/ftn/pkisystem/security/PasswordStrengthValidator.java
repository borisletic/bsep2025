package rs.ac.uns.ftn.pkisystem.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordStrengthValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    public PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return new PasswordStrength(StrengthLevel.WEAK, "Password must be at least " + MIN_LENGTH + " characters long");
        }

        int score = 0;
        String feedback = "";

        if (password.length() >= MIN_LENGTH) score++;
        if (UPPERCASE_PATTERN.matcher(password).matches()) {
            score++;
        } else {
            feedback += "Add uppercase letters. ";
        }

        if (LOWERCASE_PATTERN.matcher(password).matches()) {
            score++;
        } else {
            feedback += "Add lowercase letters. ";
        }

        if (DIGIT_PATTERN.matcher(password).matches()) {
            score++;
        } else {
            feedback += "Add numbers. ";
        }

        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            score++;
        } else {
            feedback += "Add special characters. ";
        }

        StrengthLevel level;
        if (score <= 2) {
            level = StrengthLevel.WEAK;
        } else if (score <= 4) {
            level = StrengthLevel.MEDIUM;
        } else {
            level = StrengthLevel.STRONG;
            feedback = "Strong password!";
        }

        return new PasswordStrength(level, feedback.trim());
    }

    public boolean isPasswordValid(String password) {
        return checkPasswordStrength(password).getLevel() != StrengthLevel.WEAK;
    }

    public static class PasswordStrength {
        private final StrengthLevel level;
        private final String feedback;

        public PasswordStrength(StrengthLevel level, String feedback) {
            this.level = level;
            this.feedback = feedback;
        }

        public StrengthLevel getLevel() { return level; }
        public String getFeedback() { return feedback; }
    }

    public enum StrengthLevel {
        WEAK, MEDIUM, STRONG
    }
}