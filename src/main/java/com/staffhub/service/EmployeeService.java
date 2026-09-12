package com.staffhub.service;

import com.staffhub.model.Employee;
import com.staffhub.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // Get all employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Get one employee by ID
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    // Create a new employee
    public int createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

}