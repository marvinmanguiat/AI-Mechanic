package com.marvin.AI_Mechanic.service;

import com.marvin.AI_Mechanic.dto.AuthRequest;
import com.marvin.AI_Mechanic.dto.AuthResponse;
import com.marvin.AI_Mechanic.dto.RegisterRequest;
import com.marvin.AI_Mechanic.model.AppUser;
import com.marvin.AI_Mechanic.model.EmailVerificationToken;
import com.marvin.AI_Mechanic.model.Role;
import com.marvin.AI_Mechanic.repository.AppUserRepository;
import com.marvin.AI_Mechanic.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AppUserRepository appUserRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final String appBaseUrl;

    private static final long VERIFICATION_TOKEN_TTL_SECONDS = 24 * 60 * 60;

    public AuthService(AppUserRepository appUserRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       EmailService emailService,
                       @Value("${app.base-url:http://localhost:8080}") String appBaseUrl) {
        this.appUserRepository = appUserRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.appBaseUrl = appBaseUrl != null ? appBaseUrl.replaceAll("/+$", "") : "";
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = parseRole(request.getRole());
        AppUser user = new AppUser(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            role,
            false
        );

        AppUser savedUser = appUserRepository.save(user);
        String verificationToken = createVerificationToken(savedUser);
        sendVerificationEmail(savedUser, verificationToken);

        return new AuthResponse(
            "PENDING_EMAIL_VERIFICATION",
            savedUser.getUsername(),
            savedUser.getRole().name()
        );
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        AppUser user = (AppUser) authentication.getPrincipal();
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Account is not verified. Please verify your email first.");
        }
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public void verifyEmailToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Verification token is required");
        }

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Verification token has already been used");
        }

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        AppUser user = verificationToken.getUser();
        user.setEnabled(true);
        appUserRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }

    public AppUser createSeedUserIfMissing(String username, String email, String rawPassword, Role role) {
        return appUserRepository.findByUsername(username)
            .orElseGet(() -> appUserRepository.save(
                new AppUser(username, email, passwordEncoder.encode(rawPassword), role, true)
            ));
    }

    public AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private Role parseRole(String value) {
        if (value == null || value.isBlank()) {
            return Role.USER;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role. Allowed: ADMIN, USER, MECHANIC");
        }
    }

    private String createVerificationToken(AppUser user) {
        emailVerificationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(
            token,
            user,
            Instant.now().plusSeconds(VERIFICATION_TOKEN_TTL_SECONDS),
            false
        );
        emailVerificationTokenRepository.save(verificationToken);
        return token;
    }

    private void sendVerificationEmail(AppUser user, String token) {
        String verifyLink = appBaseUrl + "/api/auth/verify?token=" + token;
        String subject = "Verify your AI Mechanic account";
        String body = "Hi " + user.getUsername() + ",\n\n"
            + "Please verify your account by opening this link:\n"
            + verifyLink + "\n\n"
            + "This link will expire in 24 hours.";
        log.info("Verification link for {}: {}", user.getUsername(), verifyLink);
        emailService.sendEmailOtp(user.getEmail(), null, subject, body);
    }
}
