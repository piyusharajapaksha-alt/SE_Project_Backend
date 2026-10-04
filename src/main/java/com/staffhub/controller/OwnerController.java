package com.staffhub.controller;

import com.staffhub.model.Owner;
import com.staffhub.repository.CompanyRepository;
import com.staffhub.repository.AuthRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final JdbcTemplate jdbcTemplate;

    private final CompanyRepository companyRepository;

    private final AuthRepository authRepository;

    public OwnerController(
            JdbcTemplate jdbcTemplate,
            CompanyRepository companyRepository,
            AuthRepository authRepository) {

        this.jdbcTemplate = jdbcTemplate;
        this.companyRepository = companyRepository;
        this.authRepository = authRepository;
    }

    // ==========================================================
    // CURRENT OWNER
    // ==========================================================

    @GetMapping("/profile")
    public ResponseEntity<?> profile(
            Authentication authentication) {

        AuthRepository.AuthUserRecord account =
                getOwnerAccount(authentication);

        if (account == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            new ErrorResponse(
                                    "Owner access required"));
        }

        return ResponseEntity.ok(
                toOwnerResponse(
                        account.owner()));
    }

    // ==========================================================
    // UPDATE CURRENT OWNER
    // ==========================================================

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody OwnerUpdateRequest request,
            Authentication authentication) {

        AuthRepository.AuthUserRecord account =
                getOwnerAccount(authentication);

        if (account == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            new ErrorResponse(
                                    "Owner access required"));
        }

        String firstName =
                required(
                        request.firstName(),
                        "First name");

        String lastName =
                required(
                        request.lastName(),
                        "Last name");

        String phone =
                request.phone() == null
                        ? null
                        : request.phone().trim();

        jdbcTemplate.update(
                """
                UPDATE company_owners
                SET
                    first_name = ?,
                    last_name = ?,
                    phone = ?,
                    updated_at = SYSDATETIME()
                WHERE id = ?
                """,
                firstName,
                lastName,
                phone,
                account.ownerId());

        AuthRepository.AuthUserRecord updated =
                authRepository.findByEmail(
                        authentication.getName());

        return ResponseEntity.ok(
                toOwnerResponse(
                        updated.owner()));
    }

    // ==========================================================
    // OWNER COMPANY
    // ==========================================================

    @GetMapping("/company")
    public ResponseEntity<?> company(
            Authentication authentication) {

        AuthRepository.AuthUserRecord account =
                getOwnerAccount(authentication);

        if (account == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            new ErrorResponse(
                                    "Owner access required"));
        }

        var company =
                companyRepository.findByOwnerEmail(
                        authentication.getName());

        if (company == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            new ErrorResponse(
                                    "Company not found"));
        }

        return ResponseEntity.ok(company);
    }

    private AuthRepository.AuthUserRecord
    getOwnerAccount(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return null;
        }

        AuthRepository.AuthUserRecord account =
                authRepository.findByEmail(
                        authentication.getName());

        if (account == null
                || account.owner() == null
                || !"Owner".equals(account.role())) {

            return null;
        }

        return account;
    }

    private String required(
            String value,
            String field) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    field + " is required");
        }

        return value.trim();
    }

    private OwnerResponse toOwnerResponse(
            Owner owner) {

        return new OwnerResponse(
                owner.getId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getEmail(),
                owner.getPhone(),
                owner.getStatus());
    }

    public record OwnerUpdateRequest(
            String firstName,
            String lastName,
            String phone) {
    }

    public record OwnerResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String status) {
    }

    public record ErrorResponse(
            String message) {
    }
}