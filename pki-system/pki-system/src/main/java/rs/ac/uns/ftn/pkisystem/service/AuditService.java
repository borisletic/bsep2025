package rs.ac.uns.ftn.pkisystem.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import rs.ac.uns.ftn.pkisystem.entity.AuditLog;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.repository.AuditLogRepository;
import rs.ac.uns.ftn.pkisystem.security.SecurityUtils;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logEvent(String eventType, String description, String resourceType, Long resourceId) {
        User currentUser = SecurityUtils.getCurrentUser().orElse(null);
        String userEmail = currentUser != null ? currentUser.getEmail() : "anonymous";
        logEvent(eventType, description, resourceType, resourceId, userEmail);
    }

    public void logEvent(String eventType, String description, String resourceType, Long resourceId, String userEmail) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEventType(eventType);
            auditLog.setEventDescription(description);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setTimestamp(LocalDateTime.now());

            // Get current user if available
            User currentUser = SecurityUtils.getCurrentUser().orElse(null);
            if (currentUser != null) {
                auditLog.setUser(currentUser);
            }

            // Get request information if available
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log to system logger as fallback
            System.err.println("Failed to save audit log: " + e.getMessage());
        }
    }

    public void logSecurityEvent(String eventType, String description, boolean success, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEventType(eventType);
            auditLog.setEventDescription(description);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setTimestamp(LocalDateTime.now());

            User currentUser = SecurityUtils.getCurrentUser().orElse(null);
            if (currentUser != null) {
                auditLog.setUser(currentUser);
            }

            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Failed to save security audit log: " + e.getMessage());
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}