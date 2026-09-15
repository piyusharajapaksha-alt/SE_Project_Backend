package com.staffhub.controller;

import com.staffhub.model.Employee;
import com.staffhub.model.Performance;
import com.staffhub.service.PerformanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
@CrossOrigin
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(
            PerformanceService performanceService
    ) {
        this.performanceService = performanceService;
    }

    // ============================================================
    // GET ALL
    // ============================================================

    @GetMapping
    public List<Performance> getAllPerformanceReviews() {

        return performanceService
                .getAllPerformanceReviews();
    }

    // ============================================================
    // GET AVAILABLE EMPLOYEES
    //
    // Example:
    // /api/performance/available-employees
    //      ?reviewPeriod=2026-09
    //
    // Or:
    // /api/performance/available-employees
    //      ?reviewPeriod=2026-09
    //      &department=IT
    // ============================================================

    @GetMapping("/available-employees")
    public List<Employee> getEmployeesAvailableForReview(
            @RequestParam String reviewPeriod,
            @RequestParam(required = false) String department
    ) {

        return performanceService
                .getEmployeesAvailableForReview(
                        reviewPeriod,
                        department
                );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/{id}")
    public Performance getPerformanceReviewById(
            @PathVariable Long id
    ) {

        return performanceService
                .getPerformanceReviewById(id);
    }

    // ============================================================
    // GET BY EMPLOYEE
    // ============================================================

    @GetMapping("/employee/{employeeId}")
    public List<Performance> getPerformanceByEmployee(
            @PathVariable String employeeId
    ) {

        return performanceService
                .getPerformanceByEmployee(employeeId);
    }

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping
    public Performance createPerformance(
            @RequestBody Performance performance
    ) {

        return performanceService
                .createPerformance(performance);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @PutMapping("/{id}")
    public Performance updatePerformance(
            @PathVariable Long id,
            @RequestBody Performance performance
    ) {

        return performanceService
                .updatePerformance(
                        id,
                        performance
                );
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/{id}")
    public void deletePerformance(
            @PathVariable Long id
    ) {

        performanceService
                .deletePerformance(id);
    }
}

