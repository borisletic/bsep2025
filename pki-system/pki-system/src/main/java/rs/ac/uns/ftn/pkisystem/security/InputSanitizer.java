package rs.ac.uns.ftn.pkisystem.security;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

@Component
public class InputSanitizer {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror|alert)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|</script|javascript:|vbscript:|onload=|onerror=|onclick=|onmouseover=)",
            Pattern.CASE_INSENSITIVE
    );

    public String sanitizeInput(String input) {
        if (input == null) return null;

        // HTML escape
        String sanitized = HtmlUtils.htmlEscape(input);

        // Remove potential SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(sanitized).find()) {
            throw new SecurityException("Potential SQL injection detected");
        }

        // Remove potential XSS patterns
        if (XSS_PATTERN.matcher(sanitized).find()) {
            throw new SecurityException("Potential XSS attack detected");
        }

        return sanitized.trim();
    }

    public boolean containsSqlInjection(String input) {
        return input != null && SQL_INJECTION_PATTERN.matcher(input).find();
    }

    public boolean containsXss(String input) {
        return input != null && XSS_PATTERN.matcher(input).find();
    }

    public String sanitizeForDisplay(String input) {
        if (input == null) return null;
        return HtmlUtils.htmlEscape(input);
    }
}