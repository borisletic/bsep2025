package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private final Map<String, CaptchaChallenge> challenges = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public CaptchaChallenge generateChallenge() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        String operation = random.nextBoolean() ? "+" : "-";

        int answer;
        String question;

        if ("+".equals(operation)) {
            answer = a + b;
            question = a + " + " + b + " = ?";
        } else {
            if (a < b) {
                int temp = a;
                a = b;
                b = temp;
            }
            answer = a - b;
            question = a + " - " + b + " = ?";
        }

        String token = generateToken();
        CaptchaChallenge challenge = new CaptchaChallenge(token, question, answer);
        challenges.put(token, challenge);

        return challenge;
    }

    public boolean verifyCaptcha(String token, String userAnswer) {
        if (token == null || userAnswer == null) {
            return false;
        }

        CaptchaChallenge challenge = challenges.remove(token);
        if (challenge == null) {
            return false;
        }

        if (challenge.isExpired()) {
            return false;
        }

        try {
            int answer = Integer.parseInt(userAnswer.trim());
            return answer == challenge.getAnswer();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String generateToken() {
        return Long.toHexString(random.nextLong());
    }

    public static class CaptchaChallenge {
        private final String token;
        private final String question;
        private final int answer;
        private final long createdAt;

        public CaptchaChallenge(String token, String question, int answer) {
            this.token = token;
            this.question = question;
            this.answer = answer;
            this.createdAt = System.currentTimeMillis();
        }

        public String getToken() { return token; }
        public String getQuestion() { return question; }
        public int getAnswer() { return answer; }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 300000; // 5 minutes
        }
    }
}