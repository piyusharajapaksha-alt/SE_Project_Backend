package com.staffhub.service;

import com.staffhub.model.Employee;
import com.staffhub.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Service
public class EmployeeService {

    private static final String DEFAULT_PASSWORD =
            "Abcd1234";

    private final EmployeeRepository employeeRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder
    ) {
        this.employeeRepository =
                employeeRepository;

        this.jdbcTemplate =
                jdbcTemplate;

        this.passwordEncoder =
                passwordEncoder;
    }

    // ==========================================================
    // GET ALL EMPLOYEES
    // ==========================================================

    public List<Employee> getAllEmployees() {

        return employeeRepository.findAll();
    }

    // ==========================================================
    // GET EMPLOYEE BY ID
    // ==========================================================

    public Employee getEmployeeById(Long id) {

        return employeeRepository.findById(id);
    }

    // ==========================================================
    // CREATE EMPLOYEE + LOGIN ACCOUNT
    // ==========================================================

    @Transactional
    public Long createEmployee(Employee employee) {

        if (employee == null) {
            throw new IllegalArgumentException(
                    "Employee data is required"
            );
        }

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
        // Check employee email
        // ------------------------------------------------------

        Integer employeeCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM employees
                        WHERE LOWER(email) = LOWER(?)
                        """,
                        Integer.class,
                        email
                );

        if (employeeCount != null
                && employeeCount > 0) {

            throw new IllegalArgumentException(
                    "An employee already exists with this email"
            );
        }

        // ------------------------------------------------------
        // Check login account email
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
        // Create employee
        // ------------------------------------------------------

        Long employeeId =
                employeeRepository.save(employee);

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
                    email,
                    password_hash,
                    enabled
                )
                VALUES (?, ?, ?, 1)
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

        if (employee == null) {
            throw new IllegalArgumentException(
                    "Employee data is required"
            );
        }

        String oldEmail =
                employeeRepository
                        .findById(id)
                        .getEmail();

        String newEmail =
                employee.getEmail();

        if (newEmail == null
                || newEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "Employee email is required"
            );
        }

        newEmail =
                newEmail.trim().toLowerCase();

        employee.setEmail(newEmail);

        int result =
                employeeRepository.update(
                        id,
                        employee
                );

        // ------------------------------------------------------
        // Keep login email synchronized
        // ------------------------------------------------------

        if (oldEmail != null
                && !oldEmail.equalsIgnoreCase(newEmail)) {

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
    public int deleteEmployee(Long id) {

        // Delete login account first because
        // it references the employee.

        jdbcTemplate.update(
                """
                DELETE FROM staffhub_auth_users
                WHERE employee_id = ?
                """,
                id
        );

        return employeeRepository.delete(id);
    }
}