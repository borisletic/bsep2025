package rs.ac.uns.ftn.pkisystem.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.pkisystem.service.UserTokenService;

@Component
public class ScheduledTasks {

    @Autowired
    private UserTokenService userTokenService;

    // Clean up expired tokens every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        userTokenService.cleanupExpiredTokens();
    }
}