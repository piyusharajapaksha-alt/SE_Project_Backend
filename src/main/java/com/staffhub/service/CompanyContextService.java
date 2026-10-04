package com.staffhub.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CompanyContextService {

    private final JdbcTemplate jdbcTemplate;

    public CompanyContextService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns the company ID belonging to the currently
     * authenticated owner or employee.
     */
    public Long getCurrentCompanyId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

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
         * Owner:
         * auth.owner_id -> companies.owner_id
         */
        Long ownerCompanyId = null;

        try {

            ownerCompanyId =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT c.id
                            FROM companies c
                            INNER JOIN company_owners o
                                ON o.id = c.owner_id
                            INNER JOIN staffhub_auth_users a
                                ON a.owner_id = o.id
                            WHERE LOWER(a.email) = LOWER(?)
                            """,
                            Long.class,
                            email
                    );

        } catch (Exception ignored) {
        }

        if (ownerCompanyId != null) {
            return ownerCompanyId;
        }

        /*
         * Employee:
         * auth.employee_id -> employees.company_id
         */
        Long employeeCompanyId = null;

        try {

            employeeCompanyId =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT e.company_id
                            FROM employees e
                            INNER JOIN staffhub_auth_users a
                                ON a.employee_id = e.id
                            WHERE LOWER(a.email) = LOWER(?)
                            """,
                            Long.class,
                            email
                    );

        } catch (Exception ignored) {
        }

        if (employeeCompanyId != null) {
            return employeeCompanyId;
        }

        throw new IllegalStateException(
                "Authenticated user is not linked to a company"
        );
    }
}