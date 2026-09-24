package com.staffhub.controller;

import com.staffhub.model.Employee;
import com.staffhub.repository.AuthRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthRepository authRepository;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(
            AuthenticationManager authenticationManager,
            AuthRepository authRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.authRepository = authRepository;
    }

    // ============================================================
    // CSRF
    // ============================================================

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken token) {

        // Calling getToken() forces Spring Security to create
        // the CSRF token and send the XSRF-TOKEN cookie.
        token.getToken();

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {

        if (request.email() == null ||
                request.email().isBlank() ||
                request.password() == null ||
                request.password().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(
                            "Email and password are required"
                    ));
        }

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.email().trim().toLowerCase(),
                                    request.password()
                            )
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(
                    context,
                    httpRequest,
                    httpResponse
            );

            AuthRepository.AuthUserRecord account =
                    authRepository.findByEmail(
                            request.email().trim().toLowerCase()
                    );

            if (account == null) {

                SecurityContextHolder.clearContext();

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(
                                "Invalid email or password"
                        ));
            }

            Employee employee = account.employee();

            AuthUserResponse response =
                    new AuthUserResponse(
                            account.authId(),
                            employee.getId(),
                            employee.getEmail(),
                            employee.getRole(),
                            employee.getEmployeeNumber(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            employee.getDepartment(),
                            employee.getPosition(),
                            employee.getPhone(),
                            employee.getEmploymentStatus()
                    );

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            "Invalid email or password"
                    ));
        }
    }

    // ============================================================
    // CURRENT USER
    // ============================================================

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            "Not authenticated"
                    ));
        }

        AuthRepository.AuthUserRecord account =
                authRepository.findByEmail(
                        authentication.getName()
                );

        if (account == null || !account.enabled()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            "Authenticated user no longer exists"
                    ));
        }

        Employee employee = account.employee();

        return ResponseEntity.ok(
                new AuthUserResponse(
                        account.authId(),
                        employee.getId(),
                        employee.getEmail(),
                        employee.getRole(),
                        employee.getEmployeeNumber(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getDepartment(),
                        employee.getPosition(),
                        employee.getPhone(),
                        employee.getEmploymentStatus()
                )
        );
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request
    ) {

        request.getSession(false);

        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // REQUEST / RESPONSE RECORDS
    // ============================================================

    public record LoginRequest(
            String email,
            String password
    ) {
    }

    public record ErrorResponse(
            String message
    ) {
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
            String status
    ) {
    }
}