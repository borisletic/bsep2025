package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.pkisystem.dto.CaptchaResponse;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.util.UUID;

@Service
public class CaptchaService {

    private final ConcurrentHashMap<String, CaptchaSession> captchaSessions = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public CaptchaResponse generateCaptcha() {
        String sessionId = UUID.randomUUID().toString();

        // Generate simple math problem
        int num1 = random.nextInt(20) + 1;
        int num2 = random.nextInt(20) + 1;
        String operation = random.nextBoolean() ? "+" : "-";

        String challenge;
        int answer;

        if (operation.equals("+")) {
            challenge = num1 + " + " + num2 + " = ?";
            answer = num1 + num2;
        } else {
            // Ensure positive result for subtraction
            if (num1 < num2) {
                int temp = num1;
                num1 = num2;
                num2 = temp;
            }
            challenge = num1 + " - " + num2 + " = ?";
            answer = num1 - num2;
        }

        CaptchaSession session = new CaptchaSession(
                challenge,
                String.valueOf(answer),
                LocalDateTime.now().plusMinutes(5)
        );

        captchaSessions.put(sessionId, session);

        // Clean up expired sessions
        cleanupExpiredSessions();

        return new CaptchaResponse(sessionId, challenge);
    }

    public boolean verifyCaptcha(String sessionId, String userAnswer) {
        CaptchaSession session = captchaSessions.get(sessionId);

        if (session == null) {
            return false;
        }

        if (session.isExpired()) {
            captchaSessions.remove(sessionId);
            return false;
        }

        boolean isCorrect = session.getAnswer().equals(userAnswer.trim());

        // Remove session after verification (one-time use)
        captchaSessions.remove(sessionId);

        return isCorrect;
    }

    private void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        captchaSessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private static class CaptchaSession {
        private final String challenge;
        private final String answer;
        private final LocalDateTime expiresAt;

        public CaptchaSession(String challenge, String answer, LocalDateTime expiresAt) {
            this.challenge = challenge;
            this.answer = answer;
            this.expiresAt = expiresAt;
        }

        public String getChallenge() { return challenge; }
        public String getAnswer() { return answer; }
        public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    }
}