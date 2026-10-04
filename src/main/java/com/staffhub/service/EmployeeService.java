package com.staffhub.service;

import com.staffhub.model.Employee;
import com.staffhub.repository.EmployeeRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private static final String DEFAULT_PASSWORD =
            "Abcd1234";

    private final EmployeeRepository employeeRepository;

    private final JdbcTemplate jdbcTemplate;

    private final PasswordEncoder passwordEncoder;

    private final CompanyContextService companyContextService;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            CompanyContextService companyContextService
    ) {

        this.employeeRepository =
                employeeRepository;

        this.jdbcTemplate =
                jdbcTemplate;

        this.passwordEncoder =
                passwordEncoder;

        this.companyContextService =
                companyContextService;
    }

    // ==========================================================
    // GET ALL EMPLOYEES
    // ==========================================================

    public List<Employee> getAllEmployees() {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        return employeeRepository
                .findAll(companyId);
    }

    // ==========================================================
    // GET EMPLOYEE BY ID
    // ==========================================================

    public Employee getEmployeeById(
            Long id
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        Employee employee =
                employeeRepository
                        .findById(
                                id,
                                companyId
                        );

        if (employee == null) {

            throw new IllegalArgumentException(
                    "Employee not found"
            );
        }

        return employee;
    }

    // ==========================================================
    // GET NEXT EMPLOYEE NUMBER
    //
    // Used by the frontend to DISPLAY the recommendation.
    //
    // Example:
    //
    // EMP001
    // EMP002
    // EMP003
    // EMP004 <- next
    // ==========================================================

    public String getNextEmployeeNumber() {

        return employeeRepository
                .generateNextEmployeeNumber();
    }

    // ==========================================================
    // CREATE EMPLOYEE + LOGIN ACCOUNT
    // ==========================================================

    @Transactional
    public Long createEmployee(
            Employee employee
    ) {

        if (employee == null) {

            throw new IllegalArgumentException(
                    "Employee data is required"
            );
        }

        // ------------------------------------------------------
        // Validate email
        // ------------------------------------------------------

        if (employee.getEmail() == null
                || employee.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Employee email is required"
            );
        }

        String email =
                employee.getEmail()
                        .trim()
                        .toLowerCase();

        employee.setEmail(email);

        // ------------------------------------------------------
        // Get current company
        // ------------------------------------------------------

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        // ------------------------------------------------------
        // Check employee email in current company
        // ------------------------------------------------------

        Integer employeeCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM employees
                        WHERE LOWER(email) = LOWER(?)
                          AND company_id = ?
                        """,
                        Integer.class,
                        email,
                        companyId
                );

        if (employeeCount != null
                && employeeCount > 0) {

            throw new IllegalArgumentException(
                    "An employee already exists with this email"
            );
        }

        // ------------------------------------------------------
        // Check login email globally
        // ------------------------------------------------------

        Integer accountCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM staffhub_auth_users
                        WHERE LOWER(email) = LOWER(?)
                        """,
                        Integer.class,
                        email
                );

        if (accountCount != null
                && accountCount > 0) {

            throw new IllegalArgumentException(
                    "A login account already exists with this email"
            );
        }

        // ------------------------------------------------------
        // IMPORTANT
        //
        // Do NOT trust employeeNumber from React.
        //
        // The backend generates the real employee number.
        // ------------------------------------------------------

        String employeeNumber =
                employeeRepository
                        .generateNextEmployeeNumber();

        employee.setEmployeeNumber(
                employeeNumber
        );

        // Company is also controlled by backend.
        employee.setCompanyId(
                companyId
        );

        // ------------------------------------------------------
        // Create employee
        // ------------------------------------------------------

        Long employeeId =
                employeeRepository.save(
                        employee,
                        companyId
                );

        // ------------------------------------------------------
        // Create login account
        // ------------------------------------------------------

        String passwordHash =
                passwordEncoder.encode(
                        DEFAULT_PASSWORD
                );

        jdbcTemplate.update(
                """
                INSERT INTO staffhub_auth_users
                (
                    employee_id,
                    owner_id,
                    email,
                    password_hash,
                    enabled
                )
                VALUES
                (
                    ?,
                    NULL,
                    ?,
                    ?,
                    1
                )
                """,
                employeeId,
                email,
                passwordHash
        );

        return employeeId;
    }

    // ==========================================================
    // UPDATE EMPLOYEE
    // ==========================================================

    @Transactional
    public int updateEmployee(
            Long id,
            Employee employee
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        // ------------------------------------------------------
        // Verify employee belongs to current company
        // ------------------------------------------------------

        Employee existing =
                employeeRepository.findById(
                        id,
                        companyId
                );

        if (existing == null) {

            throw new IllegalArgumentException(
                    "Employee not found"
            );
        }

        // ------------------------------------------------------
        // Validate email
        // ------------------------------------------------------

        if (employee.getEmail() == null
                || employee.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Employee email is required"
            );
        }

        String newEmail =
                employee.getEmail()
                        .trim()
                        .toLowerCase();

        employee.setEmail(newEmail);

        // ------------------------------------------------------
        // Employee number must NEVER change during edit
        // ------------------------------------------------------

        employee.setEmployeeNumber(
                existing.getEmployeeNumber()
        );

        employee.setCompanyId(
                companyId
        );

        // ------------------------------------------------------
        // Check email is not used by another login account
        // ------------------------------------------------------

        Integer existingAccount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM staffhub_auth_users
                        WHERE LOWER(email) = LOWER(?)
                          AND employee_id <> ?
                        """,
                        Integer.class,
                        newEmail,
                        id
                );

        if (existingAccount != null
                && existingAccount > 0) {

            throw new IllegalArgumentException(
                    "Another login account already uses this email"
            );
        }

        // ------------------------------------------------------
        // Update employee
        // ------------------------------------------------------

        int result =
                employeeRepository.update(
                        id,
                        employee,
                        companyId
                );

        // ------------------------------------------------------
        // Update login email if changed
        // ------------------------------------------------------

        if (existing.getEmail() == null
                || !existing.getEmail()
                        .equalsIgnoreCase(newEmail)) {

            jdbcTemplate.update(
                    """
                    UPDATE staffhub_auth_users
                    SET email = ?
                    WHERE employee_id = ?
                    """,
                    newEmail,
                    id
            );
        }

        return result;
    }

    // ==========================================================
    // DELETE EMPLOYEE
    // ==========================================================

    @Transactional
    public int deleteEmployee(
            Long id
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        // ------------------------------------------------------
        // Verify employee belongs to current company
        // ------------------------------------------------------

        Employee employee =
                employeeRepository.findById(
                        id,
                        companyId
                );

        if (employee == null) {

            throw new IllegalArgumentException(
                    "Employee not found"
            );
        }

        // ------------------------------------------------------
        // Delete login account
        // ------------------------------------------------------

        jdbcTemplate.update(
                """
                DELETE FROM staffhub_auth_users
                WHERE employee_id = ?
                """,
                id
        );

        // ------------------------------------------------------
        // Delete employee
        // ------------------------------------------------------

        return employeeRepository.delete(
                id,
                companyId
        );
    }
}