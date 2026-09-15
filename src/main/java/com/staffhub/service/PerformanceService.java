package com.staffhub.service;

import com.staffhub.model.Performance;
import com.staffhub.repository.PerformanceRepository;
import org.springframework.dao.DataIntegrityViolationException;
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

    public Performance getPerformanceReviewById(
            Long id
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Performance review ID is required"
            );
        }

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

        if (employeeId == null ||
                employeeId.isBlank()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        return performanceRepository.findByEmployeeId(
                employeeId.trim()
        );
    }


    // ============================================================
    // CREATE
    // ============================================================

    public Performance createPerformance(
            Performance performance
    ) {

        validatePerformance(performance);

        /*
         * Make sure the employee actually exists.
         */
        if (!performanceRepository.employeeExists(
                performance.getEmployeeId()
        )) {

            throw new IllegalArgumentException(
                    "Employee not found: "
                            + performance.getEmployeeId()
            );
        }

        /*
         * Prevent duplicate review for the same
         * employee and review period.
         */
        if (performanceRepository.reviewExists(
                performance.getEmployeeId(),
                performance.getReviewPeriod()
        )) {

            throw new IllegalArgumentException(
                    "A performance review already exists for "
                            + performance.getEmployeeId()
                            + " for "
                            + performance.getReviewPeriod()
            );
        }

        /*
         * Always calculate overall rating on backend.
         *
         * This prevents incorrect values coming
         * from the frontend.
         */
        performance.setOverallRating(
                calculateOverallRating(performance)
        );

        try {

            return performanceRepository.create(
                    performance
            );

        } catch (DataIntegrityViolationException exception) {

            /*
             * Handles race-condition duplicates as well.
             */
            throw new IllegalArgumentException(
                    "A performance review already exists for "
                            + performance.getEmployeeId()
                            + " for "
                            + performance.getReviewPeriod()
            );
        }
    }


    // ============================================================
    // UPDATE
    // ============================================================

    public Performance updatePerformance(
            Long id,
            Performance performance
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Performance review ID is required"
            );
        }

        validatePerformance(performance);

        /*
         * Make sure the review being edited exists.
         */
        Performance existing =
                performanceRepository.findById(id);

        if (existing == null) {

            throw new IllegalArgumentException(
                    "Performance review not found"
            );
        }

        /*
         * Make sure the selected employee exists.
         */
        if (!performanceRepository.employeeExists(
                performance.getEmployeeId()
        )) {

            throw new IllegalArgumentException(
                    "Employee not found: "
                            + performance.getEmployeeId()
            );
        }

        /*
         * If employee or review period was changed,
         * make sure another review doesn't already
         * use that combination.
         */
        boolean changedEmployee =
                !existing.getEmployeeId()
                        .equals(performance.getEmployeeId());

        boolean changedPeriod =
                !existing.getReviewPeriod()
                        .equals(performance.getReviewPeriod());

        if (changedEmployee || changedPeriod) {

            if (performanceRepository.reviewExistsExceptId(
                    performance.getEmployeeId(),
                    performance.getReviewPeriod(),
                    id
            )) {

                throw new IllegalArgumentException(
                        "A performance review already exists for "
                                + performance.getEmployeeId()
                                + " for "
                                + performance.getReviewPeriod()
                );
            }
        }

        /*
         * Always recalculate the overall rating.
         */
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

    public void deletePerformance(
            Long id
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Performance review ID is required"
            );
        }

        /*
         * Check first so the error is clear.
         */
        Performance existing =
                performanceRepository.findById(id);

        if (existing == null) {

            throw new IllegalArgumentException(
                    "Performance review not found"
            );
        }

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

        double average =
                total / 6.0;

        return Math.round(
                average * 100.0
        ) / 100.0;
    }


    // ============================================================
    // VALIDATION
    // ============================================================

    private void validatePerformance(
            Performance performance
    ) {

        if (performance == null) {

            throw new IllegalArgumentException(
                    "Performance review data is required"
            );
        }


        if (performance.getEmployeeId() == null
                || performance.getEmployeeId().isBlank()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        /*
         * Normalize employee ID.
         */
        performance.setEmployeeId(
                performance.getEmployeeId().trim()
        );


        if (performance.getReviewPeriod() == null
                || performance.getReviewPeriod().isBlank()) {

            throw new IllegalArgumentException(
                    "Review period is required"
            );
        }

        /*
         * Expected format:
         * YYYY-MM
         */
        String reviewPeriod =
                performance.getReviewPeriod().trim();

        if (!reviewPeriod.matches(
                "^\\d{4}-(0[1-9]|1[0-2])$"
        )) {

            throw new IllegalArgumentException(
                    "Review period must use YYYY-MM format"
            );
        }

        performance.setReviewPeriod(
                reviewPeriod
        );


        // ========================================================
        // RATINGS
        // ========================================================

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


        // ========================================================
        // STATUS
        // ========================================================

        if (performance.getStatus() == null
                || performance.getStatus().isBlank()) {

            performance.setStatus(
                    "Pending Review"
            );
        }

        String status =
                performance.getStatus().trim();

        if (!status.equals("Pending Review")
                && !status.equals("Completed")) {

            throw new IllegalArgumentException(
                    "Invalid performance review status"
            );
        }

        performance.setStatus(status);
    }


    // ============================================================
    // VALIDATE RATING
    // ============================================================

    private void validateRating(
            Integer rating,
            String fieldName
    ) {

        if (rating == null ||
                rating < 1 ||
                rating > 5) {

            throw new IllegalArgumentException(
                    fieldName
                            + " rating must be between 1 and 5"
            );
        }
    }
}