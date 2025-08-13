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
            auditLog.setDescription(description);
            auditLog.setUserEmail(userEmail);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);

            // Get request information if available
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Don't let audit failures break the main operation
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}