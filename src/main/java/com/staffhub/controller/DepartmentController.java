package com.staffhub.controller;

import com.staffhub.model.Department;
import com.staffhub.service.DepartmentService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    // GET DEPARTMENTS
    //
    // Only departments belonging to the authenticated user's
    // current company are returned.
    // ==========================================================

    @GetMapping
    public List<Department> getDepartments() {

        return departmentService
                .getDepartments();
    }
}