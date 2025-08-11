package rs.ac.uns.ftn.pkisystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.pkisystem.dto.*;
import rs.ac.uns.ftn.pkisystem.entity.Role;
import rs.ac.uns.ftn.pkisystem.entity.User;
import rs.ac.uns.ftn.pkisystem.exception.InvalidTokenException;
import rs.ac.uns.ftn.pkisystem.exception.UserAlreadyExistsException;
import rs.ac.uns.ftn.pkisystem.exception.UserNotFoundException;
import rs.ac.uns.ftn.pkisystem.repository.UserRepository;
import rs.ac.uns.ftn.pkisystem.security.JwtUtil;
import rs.ac.uns.ftn.pkisystem.security.PasswordStrengthValidator;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private PasswordStrengthValidator passwordValidator;

    public AuthenticationResponse login(AuthenticationRequest request) {
        try {
            // Verify CAPTCHA
            if (!captchaService.verifyCaptcha(request.getCaptchaToken(), request.getCaptchaAnswer())) {
                auditService.logEvent("LOGIN_FAILED", "Invalid CAPTCHA", null, null, request.getEmail());
                throw new BadCredentialsException("Invalid CAPTCHA");
            }

            // Authenticate user
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = (User) auth.getPrincipal();

            if (!user.isActivated()) {
                auditService.logEvent("LOGIN_FAILED", "Account not activated", "USER", user.getId(), user.getEmail());
                throw new BadCredentialsException("Account not activated");
            }

            // Generate JWT with JTI
            String jti = jwtUtil.generateJti();
            String token = jwtUtil.generateTokenWithJti(user, jti);

            // Save token information
            userTokenService.saveToken(user, jti, token);

            auditService.logEvent("LOGIN_SUCCESS", "User logged in successfully", "USER", user.getId(), user.getEmail());

            return new AuthenticationResponse(
                    token, user.getId(), user.getEmail(), user.getFirstName(),
                    user.getLastName(), user.getOrganization(), user.getRole(), user.isActivated()
            );

        } catch (BadCredentialsException e) {
            auditService.logEvent("LOGIN_FAILED", "Invalid credentials", null, null, request.getEmail());
            throw e;
        }
    }

    public ApiResponse<String> register(RegistrationRequest request) {
        // Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Validate password strength
        if (!passwordValidator.isPasswordValid(request.getPassword())) {
            PasswordStrengthValidator.PasswordStrength strength = passwordValidator.checkPasswordStrength(request.getPassword());
            throw new IllegalArgumentException("Password is too weak: " + strength.getFeedback());
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getOrganization()
        );

        // Generate activation token
        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);
        user.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // Send activation email
        emailService.sendActivationEmail(user.getEmail(), activationToken);

        auditService.logEvent("USER_CREATED", "New user registered", "USER", user.getId(), user.getEmail());

        return ApiResponse.success("Registration successful. Please check your email to activate your account.");
    }

    public ApiResponse<String> activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid activation token"));

        if (user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Activation token has expired");
        }

        if (user.isActivated()) {
            throw new IllegalStateException("Account is already activated");
        }

        user.setActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        userRepository.save(user);

        auditService.logEvent("ACCOUNT_ACTIVATED", "Account activated successfully", "USER", user.getId(), user.getEmail());

        return ApiResponse.success("Account activated successfully. You can now log in.");
    }

    public ApiResponse<String> requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Generate password reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(2));
        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        auditService.logEvent("PASSWORD_RESET_REQUESTED", "Password reset requested", "USER", user.getId(), user.getEmail());

        return ApiResponse.success("Password reset email sent. Please check your inbox.");
    }

    public ApiResponse<String> changePassword(PasswordChangeRequest request) {
        // Check if passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Validate password strength
        if (!passwordValidator.isPasswordValid(request.getNewPassword())) {
            PasswordStrengthValidator.PasswordStrength strength = passwordValidator.checkPasswordStrength(request.getNewPassword());
            throw new IllegalArgumentException("Password is too weak: " + strength.getFeedback());
        }

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Password reset token has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        // Revoke all existing tokens for security
        userTokenService.revokeAllTokensForUser(user);

        auditService.logEvent("PASSWORD_RESET_COMPLETED", "Password reset completed", "USER", user.getId(), user.getEmail());

        return ApiResponse.success("Password changed successfully. Please log in with your new password.");
    }

    public User createCAUser(CreateCAUserRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Generate random password
        String randomPassword = generateRandomPassword();

        // Create CA user
        User caUser = new User(
                request.getEmail(),
                passwordEncoder.encode(randomPassword),
                request.getFirstName(),
                request.getLastName(),
                request.getOrganization()
        );
        caUser.setRole(Role.CA_USER);
        caUser.setActivated(true); // CA users are activated immediately

        userRepository.save(caUser);

        // Send welcome email with password
        emailService.sendCAUserWelcomeEmail(caUser.getEmail(), randomPassword);

        auditService.logEvent("CA_USER_CREATED", "CA user created", "USER", caUser.getId(), caUser.getEmail());

        return caUser;
    }

    private String generateRandomPassword() {
        // Generate a secure random password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }
}