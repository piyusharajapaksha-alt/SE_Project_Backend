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
}