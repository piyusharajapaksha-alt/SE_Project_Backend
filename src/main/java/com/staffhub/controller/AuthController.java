package com.staffhub.controller;

import com.staffhub.model.Employee;
import com.staffhub.repository.AuthRepository;
import com.staffhub.service.OwnerRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.security.web.csrf.CsrfToken;

import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final AuthRepository authRepository;

    private final OwnerRegistrationService ownerRegistrationService;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(
            AuthenticationManager authenticationManager,
            AuthRepository authRepository,
            OwnerRegistrationService ownerRegistrationService,
            PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

        this.authenticationManager = authenticationManager;

        this.authRepository = authRepository;

        this.ownerRegistrationService = ownerRegistrationService;
    }

    // ==========================================================
    // CSRF TOKEN
    // ==========================================================

    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(
            CsrfToken token) {

        return ResponseEntity.ok(
                new CsrfResponse(
                        token.getToken()));
    }

    // ==========================================================
    // LOGIN
    // ==========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest req,
            HttpServletResponse res) {

        if (request.email() == null
                || request.email().isBlank()
                || request.password() == null
                || request.password().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    "Email and password are required"));
        }

        try {

            Authentication authentication = authenticate(
                    request.email(),
                    request.password());

            saveAuthentication(
                    authentication,
                    req,
                    res);

            AuthRepository.AuthUserRecord account = authRepository.findByEmail(
                    request.email()
                            .trim()
                            .toLowerCase());

            if (account == null) {

                return unauthorized();
            }

            return ResponseEntity.ok(
                    toResponse(account));

        } catch (Exception ex) {

            return unauthorized();
        }
    }

    // ==========================================================
    // OWNER REGISTRATION
    // ==========================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody OwnerRegistrationService.RegisterRequest request,
            HttpServletRequest req,
            HttpServletResponse res) {

        try {

            OwnerRegistrationService.RegistrationResult result = ownerRegistrationService.register(
                    request);

            Authentication authentication = authenticate(
                    result.ownerEmail(),
                    request.password());

            saveAuthentication(
                    authentication,
                    req,
                    res);

            AuthRepository.AuthUserRecord account = authRepository.findByEmail(
                    result.ownerEmail());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            toResponse(account));

        } catch (OwnerRegistrationService.RegistrationException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    ex.getMessage()));

        } catch (Exception ex) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ErrorResponse(
                                    "Registration failed. Please try again."));
        }
    }

    // ==========================================================
    // CURRENT USER
    // ==========================================================

    @GetMapping("/me")
    public ResponseEntity<?> me(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new ErrorResponse(
                                    "Not authenticated"));
        }

        AuthRepository.AuthUserRecord account = authRepository.findByEmail(
                authentication.getName());

        if (account == null
                || !account.enabled()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new ErrorResponse(
                                    "Authenticated user no longer exists"));
        }

        return ResponseEntity.ok(
                toResponse(account));
    }

    // ==========================================================
    // CHANGE PASSWORD
    // ==========================================================

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        // ------------------------------------------------------
        // Check authentication
        // ------------------------------------------------------

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new ErrorResponse(
                                    "You must be logged in to change your password"));
        }

        // ------------------------------------------------------
        // Validate request
        // ------------------------------------------------------

        if (request.currentPassword() == null
                || request.currentPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    "Current password is required"));
        }

        if (request.newPassword() == null
                || request.newPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    "New password is required"));
        }

        if (request.confirmPassword() == null
                || request.confirmPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    "Confirm password is required"));
        }

        // ------------------------------------------------------
        // Password length
        // ------------------------------------------------------

        if (request.newPassword().length() < 8) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    "New password must contain at least 8 characters"));
        }

        // ------------------------------------------------------
        // Password confirmation
        // ------------------------------------------------------

        if (!request.newPassword()
                .equals(request.confirmPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    "New passwords do not match"));
        }

        // ------------------------------------------------------
        // Find current account
        // ------------------------------------------------------

        AuthRepository.AuthUserRecord account = authRepository.findByEmail(
                authentication.getName());

        if (account == null
                || !account.enabled()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new ErrorResponse(
                                    "User account was not found"));
        }

        // ------------------------------------------------------
        // Verify current password
        // ------------------------------------------------------

        boolean currentPasswordCorrect = passwordEncoder.matches(
                request.currentPassword(),
                account.passwordHash());

        if (!currentPasswordCorrect) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    "Current password is incorrect"));
        }

        // ------------------------------------------------------
        // Prevent same password
        // ------------------------------------------------------

        if (passwordEncoder.matches(
                request.newPassword(),
                account.passwordHash())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    "New password must be different from the current password"));
        }

        // ------------------------------------------------------
        // Encode new password
        // ------------------------------------------------------

        String newPasswordHash = passwordEncoder.encode(
                request.newPassword());

        // ------------------------------------------------------
        // Update database
        // ------------------------------------------------------

        // Add this method to AuthRepository below.

        authRepository.updatePassword(
                account.authId(),
                newPasswordHash);

        return ResponseEntity.ok(
                new MessageResponse(
                        "Password changed successfully"));
    }

    // ==========================================================
    // LOGOUT
    // ==========================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        var session = request.getSession(false);

        if (session != null) {

            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }

    // ==========================================================
    // AUTHENTICATE
    // ==========================================================

    private Authentication authenticate(
            String email,
            String password) {

        return authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(
                        email.trim().toLowerCase(),
                        password));
    }

    // ==========================================================
    // SAVE AUTHENTICATION
    // ==========================================================

    private void saveAuthentication(
            Authentication authentication,
            HttpServletRequest req,
            HttpServletResponse res) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(
                authentication);

        SecurityContextHolder.setContext(
                context);

        securityContextRepository.saveContext(
                context,
                req,
                res);
    }

    // ==========================================================
    // MAP USER RESPONSE
    // ==========================================================

    private AuthUserResponse toResponse(
            AuthRepository.AuthUserRecord account) {

        Employee e = account.employee();

        return new AuthUserResponse(
                account.authId(),
                e.getId(),
                e.getEmail(),
                e.getRole(),
                e.getEmployeeNumber(),
                e.getFirstName(),
                e.getLastName(),
                e.getDepartment(),
                e.getPosition(),
                e.getPhone(),
                e.getEmploymentStatus());
    }

    // ==========================================================
    // UNAUTHORIZED
    // ==========================================================

    private ResponseEntity<ErrorResponse> unauthorized() {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ErrorResponse(
                                "Invalid email or password"));
    }

    // ==========================================================
    // REQUEST / RESPONSE RECORDS
    // ==========================================================

    public record LoginRequest(
            String email,
            String password) {
    }

    public record ErrorResponse(
            String message) {
    }

    public record CsrfResponse(
            String token) {
    }

    public record AuthUserResponse(
            Long id,
            Long employeeId,
            String email,
            String role,
            String employeeNumber,
            String firstName,
            String lastName,
            String department,
            String position,
            String phone,
            String status) {
    }

    public record ChangePasswordRequest(
            String currentPassword,
            String newPassword,
            String confirmPassword) {
    }

    public record MessageResponse(
            String message) {
    }

}