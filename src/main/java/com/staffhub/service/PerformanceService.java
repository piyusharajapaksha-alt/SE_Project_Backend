package com.staffhub.service;

import com.staffhub.model.Performance;
import com.staffhub.repository.PerformanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

    public PerformanceService(
            PerformanceRepository performanceRepository
    ) {
        this.performanceRepository = performanceRepository;
    }

    // ============================================================
    // GET ALL
    // ============================================================

    public List<Performance> getAllPerformanceReviews() {

        return performanceRepository.findAll();
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    public Performance getPerformanceReviewById(Long id) {

        Performance performance =
                performanceRepository.findById(id);

        if (performance == null) {
            throw new IllegalArgumentException(
                    "Performance review not found"
            );
        }

        return performance;
    }

    // ============================================================
    // GET BY EMPLOYEE
    // ============================================================

    public List<Performance> getPerformanceByEmployee(
            String employeeId
    ) {

        return performanceRepository.findByEmployeeId(
                employeeId
        );
    }

    // ============================================================
    // CREATE
    // ============================================================

    public Performance createPerformance(
            Performance performance
    ) {

        validatePerformance(performance);

        performance.setOverallRating(
                calculateOverallRating(performance)
        );

        return performanceRepository.create(
                performance
        );
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public Performance updatePerformance(
            Long id,
            Performance performance
    ) {

        validatePerformance(performance);

        performance.setOverallRating(
                calculateOverallRating(performance)
        );

        return performanceRepository.update(
                id,
                performance
        );
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void deletePerformance(Long id) {

        performanceRepository.delete(id);
    }

    // ============================================================
    // CALCULATE OVERALL RATING
    // ============================================================

    private double calculateOverallRating(
            Performance performance
    ) {

        double total =
                performance.getQualityOfWork()
                + performance.getProductivity()
                + performance.getTeamwork()
                + performance.getCommunication()
                + performance.getResponsibility()
                + performance.getProblemSolving();

        double average = total / 6.0;

        return Math.round(average * 100.0) / 100.0;
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void validatePerformance(
            Performance performance
    ) {

        if (performance.getEmployeeId() == null
                || performance.getEmployeeId().isBlank()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (performance.getReviewPeriod() == null
                || performance.getReviewPeriod().isBlank()) {

            throw new IllegalArgumentException(
                    "Review period is required"
            );
        }

        validateRating(
                performance.getQualityOfWork(),
                "Quality of Work"
        );

        validateRating(
                performance.getProductivity(),
                "Productivity"
        );

        validateRating(
                performance.getTeamwork(),
                "Teamwork"
        );

        validateRating(
                performance.getCommunication(),
                "Communication"
        );

        validateRating(
                performance.getResponsibility(),
                "Responsibility"
        );

        validateRating(
                performance.getProblemSolving(),
                "Problem Solving"
        );

        if (performance.getStatus() == null
                || performance.getStatus().isBlank()) {

            performance.setStatus("Pending Review");
        }
    }

    private void validateRating(
            Integer rating,
            String fieldName
    ) {

        if (rating == null || rating < 1 || rating > 5) {

            throw new IllegalArgumentException(
                    fieldName + " rating must be between 1 and 5"
            );
        }
    }
}