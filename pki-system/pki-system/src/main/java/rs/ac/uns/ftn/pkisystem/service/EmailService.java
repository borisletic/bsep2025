package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendActivationEmail(String to, String token) {
        String subject = "PKI System - Account Activation";
        String message = "Please click the following link to activate your account:\n" +
                "https://localhost:8080/api/auth/activate/" + token + "\n\n" +
                "This link will expire in 24 hours.";

        sendEmail(to, subject, message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "PKI System - Password Reset";
        String message = "Please click the following link to reset your password:\n" +
                "https://localhost:3000/reset-password?token=" + token + "\n\n" +
                "This link will expire in 1 hour.";

        sendEmail(to, subject, message);
    }

    public void sendTemporaryPasswordEmail(String to, String temporaryPassword) {
        String subject = "PKI System - Temporary Password";
        String message = "Your temporary password is: " + temporaryPassword + "\n\n" +
                "Please change this password upon first login.";

        sendEmail(to, subject, message);
    }

    private void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            mailSender.send(mailMessage);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}