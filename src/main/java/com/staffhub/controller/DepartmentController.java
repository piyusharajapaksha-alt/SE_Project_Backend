package com.staffhub.controller;

import com.staffhub.model.Department;
import com.staffhub.service.DepartmentService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(
            DepartmentService departmentService
    ) {

        this.departmentService =
                departmentService;
    }

    // ==========================================================
    // GET
    // ==========================================================

    @GetMapping
    public List<Department> getDepartments() {

        return departmentService
                .getDepartments();
    }

    // ==========================================================
    // CREATE
    // ==========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Department createDepartment(
            @RequestBody Map<String, String> request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Request body is required"
            );
        }

        return departmentService
                .createDepartment(
                        request.get("name")
                );
    }

    // ==========================================================
    // DELETE
    // ==========================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartment(
            @PathVariable Long id
    ) {

        departmentService
                .deleteDepartment(id);
    }
}