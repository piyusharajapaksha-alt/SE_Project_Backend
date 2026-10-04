package com.staffhub.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CompanyContextService {

    private final JdbcTemplate jdbcTemplate;

    public CompanyContextService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns the company belonging to the
     * currently authenticated user.
     *
     * NEVER accepts companyId from the frontend.
     */
    public Long getCurrentCompanyId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || authentication.getName() == null
                        || authentication.getName().isBlank()
        ) {
            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }

        String email =
                authentication
                        .getName()
                        .trim()
                        .toLowerCase();

        /*
         * OWNER
         *
         * auth user
         *     -> owner
         *     -> company
         */
        Long ownerCompanyId = findOwnerCompany(email);

        if (ownerCompanyId != null) {
            return ownerCompanyId;
        }

        /*
         * EMPLOYEE
         *
         * auth user
         *     -> employee
         *     -> company
         */
        Long employeeCompanyId =
                findEmployeeCompany(email);

        if (employeeCompanyId != null) {
            return employeeCompanyId;
        }

        throw new IllegalStateException(
                "Authenticated user is not linked to a company"
        );
    }

    private Long findOwnerCompany(
            String email
    ) {

        try {

            return jdbcTemplate.queryForObject(
                    """
                    SELECT TOP 1 c.id
                    FROM dbo.companies c
                    INNER JOIN dbo.company_owners o
                        ON o.id = c.owner_id
                    INNER JOIN dbo.staffhub_auth_users a
                        ON a.owner_id = o.id
                    WHERE LOWER(a.email) = LOWER(?)
                      AND a.enabled = 1
                    """,
                    Long.class,
                    email
            );

        } catch (Exception ignored) {

            return null;
        }
    }

    private Long findEmployeeCompany(
            String email
    ) {

        try {

            return jdbcTemplate.queryForObject(
                    """
                    SELECT TOP 1 e.company_id
                    FROM dbo.employees e
                    INNER JOIN dbo.staffhub_auth_users a
                        ON a.employee_id = e.id
                    WHERE LOWER(a.email) = LOWER(?)
                      AND a.enabled = 1
                      AND e.company_id IS NOT NULL
                    """,
                    Long.class,
                    email
            );

        } catch (Exception ignored) {

            return null;
        }
    }

    /**
     * Checks whether an employee belongs to
     * the current authenticated company.
     */
    public boolean employeeBelongsToCurrentCompany(
            Long employeeId
    ) {

        if (employeeId == null) {
            return false;
        }

        Long companyId =
                getCurrentCompanyId();

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.employees
                        WHERE id = ?
                          AND company_id = ?
                        """,
                        Integer.class,
                        employeeId,
                        companyId
                );

        return count != null && count > 0;
    }

    /**
     * Employee number version.
     */
    public boolean employeeNumberBelongsToCurrentCompany(
            String employeeNumber
    ) {

        if (
                employeeNumber == null
                        || employeeNumber.isBlank()
        ) {
            return false;
        }

        Long companyId =
                getCurrentCompanyId();

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.employees
                        WHERE employee_number = ?
                          AND company_id = ?
                        """,
                        Integer.class,
                        employeeNumber.trim(),
                        companyId
                );

        return count != null && count > 0;
    }

    /**
     * Throws an exception if an employee does
     * not belong to the current company.
     */
    public void requireEmployeeInCurrentCompany(
            Long employeeId
    ) {

        if (!employeeBelongsToCurrentCompany(employeeId)) {

            throw new IllegalArgumentException(
                    "Employee does not belong to the current company"
            );
        }
    }

    public void requireEmployeeNumberInCurrentCompany(
            String employeeNumber
    ) {

        if (
                !employeeNumberBelongsToCurrentCompany(
                        employeeNumber
                )
        ) {

            throw new IllegalArgumentException(
                    "Employee does not belong to the current company"
            );
        }
    }
}