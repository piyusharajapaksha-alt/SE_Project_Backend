package com.staffhub.controller;

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
    // GET /api/performance
    // ============================================================

    @GetMapping
    public List<Performance> getAllPerformanceReviews() {

        return performanceService.getAllPerformanceReviews();
    }

    // ============================================================
    // GET /api/performance/{id}
    // ============================================================

    @GetMapping("/{id}")
    public Performance getPerformanceReviewById(
            @PathVariable Long id
    ) {

        return performanceService.getPerformanceReviewById(id);
    }

    // ============================================================
    // GET /api/performance/employee/{employeeId}
    // ============================================================

    @GetMapping("/employee/{employeeId}")
    public List<Performance> getPerformanceByEmployee(
            @PathVariable String employeeId
    ) {

        return performanceService.getPerformanceByEmployee(
                employeeId
        );
    }

    // ============================================================
    // POST /api/performance
    // ============================================================

    @PostMapping
    public Performance createPerformance(
            @RequestBody Performance performance
    ) {

        return performanceService.createPerformance(
                performance
        );
    }

    // ============================================================
    // PUT /api/performance/{id}
    // ============================================================

    @PutMapping("/{id}")
    public Performance updatePerformance(
            @PathVariable Long id,
            @RequestBody Performance performance
    ) {

        return performanceService.updatePerformance(
                id,
                performance
        );
    }

    // ============================================================
    // DELETE /api/performance/{id}
    // ============================================================

    @DeleteMapping("/{id}")
    public void deletePerformance(
            @PathVariable Long id
    ) {

        performanceService.deletePerformance(id);
    }
}