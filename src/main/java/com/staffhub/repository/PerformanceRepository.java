package com.staffhub.repository;

import com.staffhub.model.Performance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PerformanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public PerformanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    // GET ALL
    // ============================================================

    public List<Performance> findAll() {

        String sql = """
                SELECT
                    id,
                    employee_id,
                    review_period,
                    quality_of_work,
                    productivity,
                    teamwork,
                    communication,
                    responsibility,
                    problem_solving,
                    overall_rating,
                    manager_feedback,
                    areas_for_improvement,
                    status
                FROM performance_reviews
                ORDER BY review_period DESC, id DESC
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> mapRow(resultSet)
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    public Performance findById(Long id) {

        String sql = """
                SELECT
                    id,
                    employee_id,
                    review_period,
                    quality_of_work,
                    productivity,
                    teamwork,
                    communication,
                    responsibility,
                    problem_solving,
                    overall_rating,
                    manager_feedback,
                    areas_for_improvement,
                    status
                FROM performance_reviews
                WHERE id = ?
                """;

        List<Performance> results = jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> mapRow(resultSet),
                id
        );

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    // ============================================================
    // GET BY EMPLOYEE
    // ============================================================

    public List<Performance> findByEmployeeId(String employeeId) {

        String sql = """
                SELECT
                    id,
                    employee_id,
                    review_period,
                    quality_of_work,
                    productivity,
                    teamwork,
                    communication,
                    responsibility,
                    problem_solving,
                    overall_rating,
                    manager_feedback,
                    areas_for_improvement,
                    status
                FROM performance_reviews
                WHERE employee_id = ?
                ORDER BY review_period DESC, id DESC
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> mapRow(resultSet),
                employeeId
        );
    }

    // ============================================================
    // CREATE
    // ============================================================

    public Performance create(Performance performance) {

        String sql = """
                INSERT INTO performance_reviews (
                    employee_id,
                    review_period,
                    quality_of_work,
                    productivity,
                    teamwork,
                    communication,
                    responsibility,
                    problem_solving,
                    overall_rating,
                    manager_feedback,
                    areas_for_improvement,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        Long generatedId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                performance.getEmployeeId(),
                performance.getReviewPeriod(),
                performance.getQualityOfWork(),
                performance.getProductivity(),
                performance.getTeamwork(),
                performance.getCommunication(),
                performance.getResponsibility(),
                performance.getProblemSolving(),
                performance.getOverallRating(),
                performance.getManagerFeedback(),
                performance.getAreasForImprovement(),
                performance.getStatus()
        );

        performance.setId(generatedId);

        return performance;
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public Performance update(Long id, Performance performance) {

        String sql = """
                UPDATE performance_reviews
                SET
                    employee_id = ?,
                    review_period = ?,
                    quality_of_work = ?,
                    productivity = ?,
                    teamwork = ?,
                    communication = ?,
                    responsibility = ?,
                    problem_solving = ?,
                    overall_rating = ?,
                    manager_feedback = ?,
                    areas_for_improvement = ?,
                    status = ?
                WHERE id = ?
                """;

        int rowsUpdated = jdbcTemplate.update(
                sql,
                performance.getEmployeeId(),
                performance.getReviewPeriod(),
                performance.getQualityOfWork(),
                performance.getProductivity(),
                performance.getTeamwork(),
                performance.getCommunication(),
                performance.getResponsibility(),
                performance.getProblemSolving(),
                performance.getOverallRating(),
                performance.getManagerFeedback(),
                performance.getAreasForImprovement(),
                performance.getStatus(),
                id
        );

        if (rowsUpdated == 0) {
            throw new IllegalArgumentException(
                    "Performance review not found"
            );
        }

        performance.setId(id);

        return performance;
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void delete(Long id) {

        String sql = """
                DELETE FROM performance_reviews
                WHERE id = ?
                """;

        int rowsDeleted = jdbcTemplate.update(sql, id);

        if (rowsDeleted == 0) {
            throw new IllegalArgumentException(
                    "Performance review not found"
            );
        }
    }

    // ============================================================
    // RESULT SET MAPPER
    // ============================================================

    private Performance mapRow(
            java.sql.ResultSet resultSet
    ) throws java.sql.SQLException {

        Performance performance = new Performance();

        performance.setId(
                resultSet.getLong("id")
        );

        performance.setEmployeeId(
                resultSet.getString("employee_id")
        );

        performance.setReviewPeriod(
                resultSet.getString("review_period")
        );

        performance.setQualityOfWork(
                resultSet.getInt("quality_of_work")
        );

        performance.setProductivity(
                resultSet.getInt("productivity")
        );

        performance.setTeamwork(
                resultSet.getInt("teamwork")
        );

        performance.setCommunication(
                resultSet.getInt("communication")
        );

        performance.setResponsibility(
                resultSet.getInt("responsibility")
        );

        performance.setProblemSolving(
                resultSet.getInt("problem_solving")
        );

        performance.setOverallRating(
                resultSet.getDouble("overall_rating")
        );

        performance.setManagerFeedback(
                resultSet.getString("manager_feedback")
        );

        performance.setAreasForImprovement(
                resultSet.getString("areas_for_improvement")
        );

        performance.setStatus(
                resultSet.getString("status")
        );

        return performance;
    }
}