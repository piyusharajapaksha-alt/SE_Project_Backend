package com.staffhub.repository;

import com.staffhub.model.Employee;
import com.staffhub.model.Owner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuthRepository(
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthUserRecord findByEmail(
            String email) {

        String sql = """
            SELECT
                a.id AS auth_id,
                a.employee_id,
                a.owner_id,
                a.email,
                a.password_hash,
                a.enabled,

                e.employee_number,
                e.first_name AS employee_first_name,
                e.last_name AS employee_last_name,
                e.department,
                e.position,
                e.role,
                e.phone AS employee_phone,
                e.employment_status,

                o.first_name AS owner_first_name,
                o.last_name AS owner_last_name,
                o.phone AS owner_phone,
                o.status AS owner_status

            FROM staffhub_auth_users a

            LEFT JOIN employees e
                ON e.id = a.employee_id

            LEFT JOIN company_owners o
                ON o.id = a.owner_id

            WHERE LOWER(a.email) = LOWER(?)
            """;

        List<AuthUserRecord> results =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> {

                            Long employeeId =
                                    rs.getObject(
                                            "employee_id",
                                            Long.class);

                            Long ownerId =
                                    rs.getObject(
                                            "owner_id",
                                            Long.class);

                            Employee employee = null;

                            if (employeeId != null) {

                                employee =
                                        new Employee();

                                employee.setId(
                                        employeeId);

                                employee.setEmployeeNumber(
                                        rs.getString(
                                                "employee_number"));

                                employee.setFirstName(
                                        rs.getString(
                                                "employee_first_name"));

                                employee.setLastName(
                                        rs.getString(
                                                "employee_last_name"));

                                employee.setEmail(
                                        rs.getString("email"));

                                employee.setDepartment(
                                        rs.getString(
                                                "department"));

                                employee.setPosition(
                                        rs.getString(
                                                "position"));

                                employee.setRole(
                                        rs.getString(
                                                "role"));

                                employee.setPhone(
                                        rs.getString(
                                                "employee_phone"));

                                employee.setEmploymentStatus(
                                        rs.getString(
                                                "employment_status"));
                            }

                            Owner owner = null;

                            if (ownerId != null) {

                                owner =
                                        new Owner();

                                owner.setId(
                                        ownerId);

                                owner.setFirstName(
                                        rs.getString(
                                                "owner_first_name"));

                                owner.setLastName(
                                        rs.getString(
                                                "owner_last_name"));

                                owner.setEmail(
                                        rs.getString(
                                                "email"));

                                owner.setPhone(
                                        rs.getString(
                                                "owner_phone"));

                                owner.setStatus(
                                        rs.getString(
                                                "owner_status"));
                            }

                            String role;

                            if (owner != null) {
                                role = "Owner";
                            } else if (employee != null) {
                                role = employee.getRole();
                            } else {
                                role = "Employee";
                            }

                            return new AuthUserRecord(
                                    rs.getLong("auth_id"),
                                    employeeId,
                                    ownerId,
                                    rs.getString("email"),
                                    rs.getString("password_hash"),
                                    rs.getBoolean("enabled"),
                                    role,
                                    employee,
                                    owner);
                        },
                        email.trim());

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    public void updatePassword(
            Long authId,
            String passwordHash) {

        jdbcTemplate.update(
                """
                UPDATE staffhub_auth_users
                SET password_hash = ?
                WHERE id = ?
                """,
                passwordHash,
                authId);
    }

    public record AuthUserRecord(
            Long authId,
            Long employeeId,
            Long ownerId,
            String email,
            String passwordHash,
            boolean enabled,
            String role,
            Employee employee,
            Owner owner) {
    }
}