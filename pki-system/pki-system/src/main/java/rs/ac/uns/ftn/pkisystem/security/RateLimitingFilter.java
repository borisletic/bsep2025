package rs.ac.uns.ftn.pkisystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastRequestTimes = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final long TIME_WINDOW = 60000; // 1 minute in milliseconds

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIpAddress(request);
        String key = clientIp + ":" + request.getRequestURI();

        // Skip rate limiting for certain endpoints
        if (shouldSkipRateLimit(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Clean up old entries
        cleanupOldEntries(currentTime);

        // Check rate limit
        AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        lastRequestTimes.put(key, currentTime);

        if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean shouldSkipRateLimit(String uri) {
        return uri.startsWith("/actuator/health") ||
                uri.startsWith("/swagger-ui") ||
                uri.startsWith("/v3/api-docs");
    }

    private void cleanupOldEntries(long currentTime) {
        lastRequestTimes.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > TIME_WINDOW) {
                requestCounts.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}