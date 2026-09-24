package com.staffhub.repository;

import com.staffhub.model.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthUserRecord findByEmail(String email) {

        String sql = """
                SELECT
                    a.id AS auth_id,
                    a.employee_id,
                    a.email,
                    a.password_hash,
                    a.enabled,
                    e.employee_number,
                    e.first_name,
                    e.last_name,
                    e.department,
                    e.position,
                    e.role,
                    e.phone,
                    e.employment_status
                FROM staffhub_auth_users a
                INNER JOIN employees e
                    ON e.id = a.employee_id
                WHERE LOWER(a.email) = LOWER(?)
                """;

        List<AuthUserRecord> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Employee employee = new Employee();

                    employee.setId(rs.getLong("employee_id"));
                    employee.setEmployeeNumber(
                            rs.getString("employee_number")
                    );
                    employee.setFirstName(
                            rs.getString("first_name")
                    );
                    employee.setLastName(
                            rs.getString("last_name")
                    );
                    employee.setEmail(
                            rs.getString("email")
                    );
                    employee.setDepartment(
                            rs.getString("department")
                    );
                    employee.setPosition(
                            rs.getString("position")
                    );
                    employee.setRole(
                            rs.getString("role")
                    );
                    employee.setPhone(
                            rs.getString("phone")
                    );
                    employee.setEmploymentStatus(
                            rs.getString("employment_status")
                    );

                    return new AuthUserRecord(
                            rs.getLong("auth_id"),
                            rs.getLong("employee_id"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getBoolean("enabled"),
                            employee
                    );
                },
                email.trim()
        );

        return results.isEmpty() ? null : results.get(0);
    }

    public record AuthUserRecord(
            Long authId,
            Long employeeId,
            String email,
            String passwordHash,
            boolean enabled,
            Employee employee
    ) {
    }
}