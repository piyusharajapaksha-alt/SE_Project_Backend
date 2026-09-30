package com.staffhub.repository;

import com.staffhub.model.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class OwnerRegistrationRepository {
    private final JdbcTemplate jdbcTemplate;

    public OwnerRegistrationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean employeeEmailExists(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees WHERE LOWER(email) = LOWER(?)",
                Integer.class, email.trim());
        return count != null && count > 0;
    }

    public Long insertOwnerEmployee(Employee e) {
        String sql = """
            INSERT INTO employees
            (employee_number, first_name, last_name, email, phone, department,
             position, role, employment_status, hire_date, address,
             emergency_contact, salary, gender)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, e.getEmployeeNumber());
            ps.setString(2, e.getFirstName());
            ps.setString(3, e.getLastName());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getPhone());
            ps.setString(6, e.getDepartment());
            ps.setString(7, e.getPosition());
            ps.setString(8, e.getRole());
            ps.setString(9, e.getEmploymentStatus());
            ps.setObject(10, e.getHireDate());
            ps.setString(11, e.getAddress());
            ps.setString(12, e.getEmergencyContact());
            ps.setBigDecimal(13, e.getSalary());
            ps.setString(14, e.getGender());
            return ps;
        }, kh);

        if (kh.getKey() == null) throw new IllegalStateException("Unable to create owner");
        return kh.getKey().longValue();
    }

    public void insertAuthUser(Long employeeId, String email, String passwordHash) {
        jdbcTemplate.update("""
            INSERT INTO staffhub_auth_users (employee_id, email, password_hash, enabled)
            VALUES (?, ?, ?, 1)
            """, employeeId, email, passwordHash);
    }
}
