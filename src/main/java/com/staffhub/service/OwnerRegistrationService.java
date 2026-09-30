package com.staffhub.service;

import com.staffhub.model.Company;
import com.staffhub.model.Employee;
import com.staffhub.repository.CompanyRepository;
import com.staffhub.repository.OwnerRegistrationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class OwnerRegistrationService {
    private final OwnerRegistrationRepository registrationRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public OwnerRegistrationService(
            OwnerRegistrationRepository registrationRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder) {
        this.registrationRepository = registrationRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrationResult register(RegisterRequest request) {
        String companyName = required(request.companyName(), "Company name");
        String companyEmail = email(request.companyEmail(), "Company email");
        String ownerFirstName = required(request.ownerFirstName(), "Owner first name");
        String ownerLastName = required(request.ownerLastName(), "Owner last name");
        String ownerEmail = email(request.ownerEmail(), "Owner email");
        String password = required(request.password(), "Password");

        if (password.length() < 8)
            throw new RegistrationException("Password must contain at least 8 characters");
        if (!password.equals(request.confirmPassword()))
            throw new RegistrationException("Passwords do not match");
        if (registrationRepository.employeeEmailExists(ownerEmail))
            throw new RegistrationException("An account already exists with this owner email");
        if (companyRepository.existsByEmail(companyEmail))
            throw new RegistrationException("A company already exists with this company email");

        Employee owner = new Employee();
        owner.setEmployeeNumber("OWN-" + randomCode());
        owner.setFirstName(ownerFirstName);
        owner.setLastName(ownerLastName);
        owner.setEmail(ownerEmail);
        owner.setPhone(clean(request.ownerPhone()));
        owner.setDepartment("Management");
        owner.setPosition("Company Owner");
        owner.setRole("Owner");
        owner.setEmploymentStatus("Active");
        owner.setHireDate(LocalDate.now());

        Long employeeId = registrationRepository.insertOwnerEmployee(owner);

        Company company = new Company();
        company.setCompanyCode("COM-" + randomCode());
        company.setCompanyName(companyName);
        company.setEmail(companyEmail);
        company.setPhone(clean(request.companyPhone()));
        company.setAddress(clean(request.companyAddress()));
        company.setIndustry(clean(request.industry()));
        company.setStatus("Active");
        company.setOwnerEmployeeId(employeeId);

        Long companyId = companyRepository.insert(company);

        registrationRepository.insertAuthUser(
                employeeId, ownerEmail, passwordEncoder.encode(password));

        return new RegistrationResult(companyId, employeeId, ownerEmail);
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank())
            throw new RegistrationException(field + " is required");
        return value.trim();
    }

    private String email(String value, String field) {
        String result = required(value, field).toLowerCase();
        if (!result.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new RegistrationException("Please enter a valid " + field.toLowerCase());
        return result;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String randomCode() {
        return UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
    }

    public record RegisterRequest(
            String companyName, String companyEmail, String companyPhone,
            String companyAddress, String industry,
            String ownerFirstName, String ownerLastName, String ownerEmail,
            String ownerPhone, String password, String confirmPassword) {}

    public record RegistrationResult(Long companyId, Long employeeId, String ownerEmail) {}

    public static class RegistrationException extends RuntimeException {
        public RegistrationException(String message) { super(message); }
    }
}
