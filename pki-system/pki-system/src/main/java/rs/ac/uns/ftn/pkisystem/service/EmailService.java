package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:https://localhost:3000}")
    private String frontendUrl;

    public void sendActivationEmail(String toEmail, String activationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Context context = new Context();
            context.setVariable("activationLink", frontendUrl + "/activate/" + activationToken);
            context.setVariable("email", toEmail);

            String htmlContent = templateEngine.process("activation-email", context);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PKI System - Account Activation");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Fallback to simple email
            sendSimpleActivationEmail(toEmail, activationToken);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Context context = new Context();
            context.setVariable("resetLink", frontendUrl + "/password-reset/" + resetToken);
            context.setVariable("email", toEmail);

            String htmlContent = templateEngine.process("password-reset-email", context);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PKI System - Password Reset");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Fallback to simple email
            sendSimplePasswordResetEmail(toEmail, resetToken);
        }
    }

    public void sendCAUserWelcomeEmail(String toEmail, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Context context = new Context();
            context.setVariable("loginLink", frontendUrl + "/login");
            context.setVariable("email", toEmail);
            context.setVariable("temporaryPassword", temporaryPassword);

            String htmlContent = templateEngine.process("ca-user-welcome-email", context);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PKI System - CA User Account Created");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Fallback to simple email
            sendSimpleCAUserWelcomeEmail(toEmail, temporaryPassword);
        }
    }

    private void sendSimpleActivationEmail(String toEmail, String activationToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("PKI System - Account Activation");
        message.setText("Please activate your account by clicking the following link:\n" +
                frontendUrl + "/activate/" + activationToken);
        mailSender.send(message);
    }

    private void sendSimplePasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("PKI System - Password Reset");
        message.setText("Please reset your password by clicking the following link:\n" +
                frontendUrl + "/password-reset/" + resetToken);
        mailSender.send(message);
    }

    private void sendSimpleCAUserWelcomeEmail(String toEmail, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("PKI System - CA User Account Created");
        message.setText("Your CA user account has been created.\n" +
                "Email: " + toEmail + "\n" +
                "Temporary Password: " + temporaryPassword + "\n" +
                "Please log in and change your password immediately.\n" +
                "Login URL: " + frontendUrl + "/login");
        mailSender.send(message);
    }
}