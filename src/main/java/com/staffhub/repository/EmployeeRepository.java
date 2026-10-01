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
    // GET ALL EMPLOYEES
    // ==========================================================

    public List<Employee> findAll() {

        String sql = "SELECT * FROM employees ORDER BY id";

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> {

            Employee employee = new Employee();

            employee.setId(resultSet.getLong("id"));
            employee.setEmployeeNumber(
                    resultSet.getString("employee_number")
            );
            employee.setFirstName(
                    resultSet.getString("first_name")
            );
            employee.setLastName(
                    resultSet.getString("last_name")
            );
            employee.setEmail(
                    resultSet.getString("email")
            );
            employee.setPhone(
                    resultSet.getString("phone")
            );
            employee.setDepartment(
                    resultSet.getString("department")
            );
            employee.setPosition(
                    resultSet.getString("position")
            );
            employee.setRole(
                    resultSet.getString("role")
            );
            employee.setEmploymentStatus(
                    resultSet.getString("employment_status")
            );

            if (resultSet.getDate("hire_date") != null) {
                employee.setHireDate(
                        resultSet.getDate("hire_date").toLocalDate()
                );
            }

            employee.setAddress(
                    resultSet.getString("address")
            );

            employee.setEmergencyContact(
                    resultSet.getString("emergency_contact")
            );

            employee.setSalary(
                    resultSet.getBigDecimal("salary")
            );

            employee.setGender(
                    resultSet.getString("gender")
            );

            return employee;
        });
    }

    // ==========================================================
    // GET EMPLOYEE BY ID
    // ==========================================================

    public Employee findById(Long id) {

        String sql = "SELECT * FROM employees WHERE id = ?";

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, rowNumber) -> {

                    Employee employee = new Employee();

                    employee.setId(resultSet.getLong("id"));
                    employee.setEmployeeNumber(
                            resultSet.getString("employee_number")
                    );
                    employee.setFirstName(
                            resultSet.getString("first_name")
                    );
                    employee.setLastName(
                            resultSet.getString("last_name")
                    );
                    employee.setEmail(
                            resultSet.getString("email")
                    );
                    employee.setPhone(
                            resultSet.getString("phone")
                    );
                    employee.setDepartment(
                            resultSet.getString("department")
                    );
                    employee.setPosition(
                            resultSet.getString("position")
                    );
                    employee.setRole(
                            resultSet.getString("role")
                    );
                    employee.setEmploymentStatus(
                            resultSet.getString("employment_status")
                    );

                    if (resultSet.getDate("hire_date") != null) {
                        employee.setHireDate(
                                resultSet.getDate("hire_date").toLocalDate()
                        );
                    }

                    employee.setAddress(
                            resultSet.getString("address")
                    );

                    employee.setEmergencyContact(
                            resultSet.getString("emergency_contact")
                    );

                    employee.setSalary(
                            resultSet.getBigDecimal("salary")
                    );

                    employee.setGender(
                            resultSet.getString("gender")
                    );

                    return employee;
                },
                id
        );
    }

    // ==========================================================
    // CREATE EMPLOYEE
    // ==========================================================

    public Long save(Employee employee) {

        String sql = """
                INSERT INTO employees (
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
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder =
                new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps =
                    connection.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            ps.setString(
                    1,
                    employee.getEmployeeNumber()
            );

            ps.setString(
                    2,
                    employee.getFirstName()
            );

            ps.setString(
                    3,
                    employee.getLastName()
            );

            ps.setString(
                    4,
                    employee.getEmail()
            );

            ps.setString(
                    5,
                    employee.getPhone()
            );

            ps.setString(
                    6,
                    employee.getDepartment()
            );

            ps.setString(
                    7,
                    employee.getPosition()
            );

            ps.setString(
                    8,
                    employee.getRole()
            );

            ps.setString(
                    9,
                    employee.getEmploymentStatus()
            );

            ps.setObject(
                    10,
                    employee.getHireDate()
            );

            ps.setString(
                    11,
                    employee.getAddress()
            );

            ps.setString(
                    12,
                    employee.getEmergencyContact()
            );

            ps.setBigDecimal(
                    13,
                    employee.getSalary()
            );

            ps.setString(
                    14,
                    employee.getGender()
            );

            return ps;

        }, keyHolder);

        if (keyHolder.getKey() == null) {
            throw new IllegalStateException(
                    "Unable to create employee"
            );
        }

        return keyHolder.getKey().longValue();
    }

    // ==========================================================
    // UPDATE EMPLOYEE
    // ==========================================================

    public int update(
            Long id,
            Employee employee
    ) {

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
                id
        );
    }

    // ==========================================================
    // DELETE EMPLOYEE
    // ==========================================================

    public int delete(Long id) {

        String sql =
                "DELETE FROM employees WHERE id = ?";

        return jdbcTemplate.update(
                sql,
                id
        );
    }
}