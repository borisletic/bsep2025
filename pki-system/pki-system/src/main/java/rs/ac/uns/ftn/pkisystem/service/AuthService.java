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

    @Autowired
    private UserService userService;

    public AuthenticationResponse login(AuthenticationRequest request) {
        try {
            // Verify CAPTCHA
            if (!captchaService.verifyCaptcha(request.getCaptchaToken(), request.getCaptchaAnswer())) {
                auditService.logEvent("LOGIN_FAILED", "Invalid CAPTCHA", null, null, request.getEmail());
                throw new BadCredentialsException("Invalid CAPTCHA");
            }

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();

            if (!user.isActivated()) {
                auditService.logEvent("LOGIN_FAILED", "Account not activated", null, null, user.getEmail());
                throw new BadCredentialsException("Account not activated. Please check your email.");
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user);
            String jti = jwtUtil.getJtiFromToken(token);

            // Save token for session management
            userTokenService.saveToken(user, jti, token);

            auditService.logEvent("LOGIN_SUCCESS", "User logged in successfully", null, null, user.getEmail());

            UserDTO userDTO = userService.convertToDTO(user);
            return new AuthenticationResponse(token, userDTO);

        } catch (BadCredentialsException e) {
            auditService.logEvent("LOGIN_FAILED", "Invalid credentials", null, null, request.getEmail());
            throw e;
        } catch (Exception e) {
            auditService.logEvent("LOGIN_FAILED", "Login error: " + e.getMessage(), null, null, request.getEmail());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public ApiResponse<String> register(RegistrationRequest request) {
        // Validate input
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Validate password strength
        PasswordStrengthValidator.ValidationResult validation = passwordValidator.validate(request.getPassword());
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Password is not strong enough: " + String.join(", ", validation.getErrors()));
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setOrganization(request.getOrganization());
        user.setRole(Role.END_USER);
        user.setActivated(false);

        // Generate activation token
        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);
        user.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

        user = userRepository.save(user);

        // Send activation email
        emailService.sendActivationEmail(user.getEmail(), activationToken);

        auditService.logEvent("USER_REGISTERED", "New user registered", "USER", user.getId(), user.getEmail());

        return ApiResponse.success("Registration successful. Please check your email to activate your account.");
    }

    public ApiResponse<String> activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid activation token"));

        if (user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Activation token has expired");
        }

        if (user.isActivated()) {
            throw new IllegalArgumentException("Account is already activated");
        }

        user.setActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        userRepository.save(user);

        auditService.logEvent("ACCOUNT_ACTIVATED", "Account activated", "USER", user.getId(), user.getEmail());

        return ApiResponse.success("Account activated successfully. You can now log in.");
    }

    public ApiResponse<String> requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        auditService.logEvent("PASSWORD_RESET_REQUESTED", "Password reset requested", "USER", user.getId(), user.getEmail());

        return ApiResponse.success("Password reset link sent to your email.");
    }

    public ApiResponse<String> changePassword(PasswordChangeRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Validate password strength
        PasswordStrengthValidator.ValidationResult validation = passwordValidator.validate(request.getNewPassword());
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Password is not strong enough: " + String.join(", ", validation.getErrors()));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        auditService.logEvent("PASSWORD_CHANGED", "Password changed", "USER", user.getId(), user.getEmail());

        return ApiResponse.success("Password changed successfully.");
    }

    public User createCAUser(CreateCAUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setOrganization(request.getOrganization());
        user.setRole(Role.CA_USER);
        user.setActivated(true); // CA users are pre-activated

        user = userRepository.save(user);

        // Send temporary password via email
        emailService.sendTemporaryPasswordEmail(user.getEmail(), temporaryPassword);

        auditService.logEvent("CA_USER_CREATED", "CA user created", "USER", user.getId(), user.getEmail());

        return user;
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }
}