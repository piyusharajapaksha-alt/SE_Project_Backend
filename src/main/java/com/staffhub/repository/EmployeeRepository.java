package com.staffhub.repository;

import com.staffhub.model.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Get all employees
    public List<Employee> findAll() {

        String sql = "SELECT * FROM employees";

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> {

            Employee employee = new Employee();

            employee.setId(resultSet.getLong("id"));
            employee.setEmployeeNumber(resultSet.getString("employee_number"));
            employee.setFirstName(resultSet.getString("first_name"));
            employee.setLastName(resultSet.getString("last_name"));
            employee.setEmail(resultSet.getString("email"));
            employee.setPhone(resultSet.getString("phone"));
            employee.setDepartment(resultSet.getString("department"));
            employee.setPosition(resultSet.getString("position"));
            employee.setRole(resultSet.getString("role"));
            employee.setEmploymentStatus(resultSet.getString("employment_status"));

            return employee;
        });
    }


// Get one employee by ID
public Employee findById(Long id) {

    String sql = "SELECT * FROM employees WHERE id = ?";

    return jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) -> {

        Employee employee = new Employee();

        employee.setId(resultSet.getLong("id"));
        employee.setEmployeeNumber(resultSet.getString("employee_number"));
        employee.setFirstName(resultSet.getString("first_name"));
        employee.setLastName(resultSet.getString("last_name"));
        employee.setEmail(resultSet.getString("email"));
        employee.setPhone(resultSet.getString("phone"));
        employee.setDepartment(resultSet.getString("department"));
        employee.setPosition(resultSet.getString("position"));
        employee.setRole(resultSet.getString("role"));
        employee.setEmploymentStatus(resultSet.getString("employment_status"));

        return employee;
    }, id);
 }

 // Create a new employee
public int save(Employee employee) {

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
                employment_status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            employee.getEmploymentStatus()
    );
}

// Update an existing employee
public int update(Long id, Employee employee) {

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
                employment_status = ?
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
            id
    );
}




}
