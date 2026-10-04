package com.staffhub.repository;

import com.staffhub.model.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==========================================================
    // MAPPER
    // ==========================================================

    private Employee mapEmployee(
            java.sql.ResultSet rs) throws java.sql.SQLException {

        Employee employee = new Employee();

        employee.setId(
                rs.getLong("id")
        );

        employee.setCompanyId(
                rs.getLong("company_id")
        );

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

        employee.setPhone(
                rs.getString("phone")
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

        employee.setEmploymentStatus(
                rs.getString("employment_status")
        );

        if (rs.getDate("hire_date") != null) {

            employee.setHireDate(
                    rs.getDate("hire_date")
                            .toLocalDate()
            );
        }

        employee.setAddress(
                rs.getString("address")
        );

        employee.setEmergencyContact(
                rs.getString("emergency_contact")
        );

        employee.setSalary(
                rs.getBigDecimal("salary")
        );

        employee.setGender(
                rs.getString("gender")
        );

        return employee;
    }

    // ==========================================================
    // GET ALL EMPLOYEES FOR COMPANY
    // ==========================================================

    public List<Employee> findAll(Long companyId) {

        String sql = """
                SELECT *
                FROM employees
                WHERE company_id = ?
                ORDER BY id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapEmployee(rs),
                companyId
        );
    }

    // ==========================================================
    // GET EMPLOYEE BY ID + COMPANY
    // ==========================================================

    public Employee findById(
            Long id,
            Long companyId) {

        String sql = """
                SELECT *
                FROM employees
                WHERE id = ?
                  AND company_id = ?
                """;

        List<Employee> results =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> mapEmployee(rs),
                        id,
                        companyId
                );

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    // ==========================================================
    // CREATE EMPLOYEE
    // ==========================================================

    public Long save(
            Employee employee,
            Long companyId) {

        String sql = """
                INSERT INTO employees
                (
                    company_id,
                    employee_number,
                    first_name,
                    last_name,
                    email,
                    phone,
                    department,
                    position,
                    role,
                    employment_status,
                    hire_date,
                    address,
                    emergency_contact,
                    salary,
                    gender
                )
                VALUES
                (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
                """;

        KeyHolder keyHolder =
                new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {

                    PreparedStatement ps =
                            connection.prepareStatement(
                                    sql,
                                    Statement.RETURN_GENERATED_KEYS
                            );

                    ps.setLong(1, companyId);

                    ps.setString(
                            2,
                            employee.getEmployeeNumber()
                    );

                    ps.setString(
                            3,
                            employee.getFirstName()
                    );

                    ps.setString(
                            4,
                            employee.getLastName()
                    );

                    ps.setString(
                            5,
                            employee.getEmail()
                    );

                    ps.setString(
                            6,
                            employee.getPhone()
                    );

                    ps.setString(
                            7,
                            employee.getDepartment()
                    );

                    ps.setString(
                            8,
                            employee.getPosition()
                    );

                    ps.setString(
                            9,
                            employee.getRole()
                    );

                    ps.setString(
                            10,
                            employee.getEmploymentStatus()
                    );

                    ps.setObject(
                            11,
                            employee.getHireDate()
                    );

                    ps.setString(
                            12,
                            employee.getAddress()
                    );

                    ps.setString(
                            13,
                            employee.getEmergencyContact()
                    );

                    ps.setBigDecimal(
                            14,
                            employee.getSalary()
                    );

                    ps.setString(
                            15,
                            employee.getGender()
                    );

                    return ps;

                },
                keyHolder
        );

        if (keyHolder.getKey() == null) {

            throw new IllegalStateException(
                    "Unable to create employee"
            );
        }

        return keyHolder
                .getKey()
                .longValue();
    }

    // ==========================================================
    // UPDATE
    // ==========================================================

    public int update(
            Long id,
            Employee employee,
            Long companyId) {

        String sql = """
                UPDATE employees
                SET
                    employee_number = ?,
                    first_name = ?,
                    last_name = ?,
                    email = ?,
                    phone = ?,
                    department = ?,
                    position = ?,
                    role = ?,
                    employment_status = ?,
                    hire_date = ?,
                    address = ?,
                    emergency_contact = ?,
                    salary = ?,
                    gender = ?
                WHERE id = ?
                  AND company_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDepartment(),
                employee.getPosition(),
                employee.getRole(),
                employee.getEmploymentStatus(),
                employee.getHireDate(),
                employee.getAddress(),
                employee.getEmergencyContact(),
                employee.getSalary(),
                employee.getGender(),
                id,
                companyId
        );
    }

    // ==========================================================
    // DELETE
    // ==========================================================

    public int delete(
            Long id,
            Long companyId) {

        return jdbcTemplate.update(
                """
                DELETE FROM employees
                WHERE id = ?
                  AND company_id = ?
                """,
                id,
                companyId
        );
    }
}