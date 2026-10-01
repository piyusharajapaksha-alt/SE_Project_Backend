package com.staffhub.controller;

import com.staffhub.model.Employee;
import com.staffhub.service.EmployeeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(
            EmployeeService employeeService
    ) {
        this.employeeService =
                employeeService;
    }

    // ==========================================================
    // GET ALL
    // ==========================================================

    @GetMapping
    public List<Employee> getAllEmployees() {

        return employeeService.getAllEmployees();
    }

    // ==========================================================
    // GET BY ID
    // ==========================================================

    @GetMapping("/{id}")
    public Employee getEmployeeById(
            @PathVariable Long id
    ) {

        return employeeService.getEmployeeById(id);
    }

    // ==========================================================
    // CREATE
    // ==========================================================

    @PostMapping
    public ResponseEntity<?> createEmployee(
            @RequestBody Employee employee
    ) {

        try {

            Long employeeId =
                    employeeService.createEmployee(
                            employee
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            new EmployeeCreateResponse(
                                    employeeId,
                                    employee.getEmail(),
                                    "Employee created successfully. "
                                            + "Login account created with the temporary password."
                            )
                    );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    ex.getMessage()
                            )
                    );

        } catch (Exception ex) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            new ErrorResponse(
                                    "Employee creation failed."
                            )
                    );
        }
    }

    // ==========================================================
    // UPDATE
    // ==========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee employee
    ) {

        try {

            employeeService.updateEmployee(
                    id,
                    employee
            );

            return ResponseEntity.ok(
                    "Employee updated successfully"
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ErrorResponse(
                                    ex.getMessage()
                            )
                    );

        } catch (Exception ex) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            new ErrorResponse(
                                    "Employee update failed."
                            )
                    );
        }
    }

    // ==========================================================
    // DELETE
    // ==========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(
            @PathVariable Long id
    ) {

        try {

            employeeService.deleteEmployee(id);

            return ResponseEntity.ok(
                    "Employee deleted successfully"
            );

        } catch (Exception ex) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            new ErrorResponse(
                                    "Employee deletion failed."
                            )
                    );
        }
    }

    // ==========================================================
    // RESPONSE RECORDS
    // ==========================================================

    public record EmployeeCreateResponse(
            Long employeeId,
            String email,
            String message
    ) {}

    public record ErrorResponse(
            String message
    ) {}
}