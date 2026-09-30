package com.staffhub.controller;

import com.staffhub.model.Company;
import com.staffhub.repository.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {
    private final CompanyRepository companyRepository;

    public OwnerController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/company")
    public ResponseEntity<?> company(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Not authenticated"));

        Company company = companyRepository.findByOwnerEmail(authentication.getName());
        if (company == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Company information was not found"));

        return ResponseEntity.ok(company);
    }

    public record ErrorResponse(String message) {}
}
