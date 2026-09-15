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
    // GET AVAILABLE EMPLOYEES FOR REVIEW
    //
    // Returns employees who:
    // 1. Match the selected department, if supplied.
    // 2. Do NOT already have a review for the selected month.
    // ============================================================

    public List<com.staffhub.model.Employee> findEmployeesAvailableForReview(
            String reviewPeriod,
            String department
    ) {

        String sql = """
                SELECT
                    e.id,
                    e.employee_number,
                    e.first_name,
                    e.last_name,
                    e.email,
                    e.phone,
                    e.department,
                    e.position,
                    e.role,
                    e.employment_status,
                    e.hire_date,
                    e.address,
                    e.emergency_contact,
                    e.salary,
                    e.gender
                FROM employees e
                WHERE
                    (
                        ? IS NULL
                        OR ? = ''
                        OR e.department = ?
                    )
                    AND NOT EXISTS (
                        SELECT 1
                        FROM performance_reviews pr
                        WHERE pr.employee_id = e.employee_number
                        AND pr.review_period = ?
                    )
                ORDER BY
                    e.department ASC,
                    e.first_name ASC,
                    e.last_name ASC,
                    e.employee_number ASC
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {

                    com.staffhub.model.Employee employee =
                            new com.staffhub.model.Employee();

                    employee.setId(
                            resultSet.getLong("id")
                    );

                    employee.setEmployeeNumber(
                            resultSet.getString("employee_number")
                    );

                    employee.setFirstName(
                            resultSet.getString("first_name")
                    );

                    employee.setLastName(
                            resultSet.getString("last_name")
                    );

                    employee.setEmail(
                            resultSet.getString("email")
                    );

                    employee.setPhone(
                            resultSet.getString("phone")
                    );

                    employee.setDepartment(
                            resultSet.getString("department")
                    );

                    employee.setPosition(
                            resultSet.getString("position")
                    );

                    employee.setRole(
                            resultSet.getString("role")
                    );

                    employee.setEmploymentStatus(
                            resultSet.getString("employment_status")
                    );

                    if (resultSet.getDate("hire_date") != null) {
                        employee.setHireDate(
                                resultSet
                                        .getDate("hire_date")
                                        .toLocalDate()
                        );
                    }

                    employee.setAddress(
                            resultSet.getString("address")
                    );

                    employee.setEmergencyContact(
                            resultSet.getString("emergency_contact")
                    );

                    employee.setSalary(
                            resultSet.getBigDecimal("salary")
                    );

                    employee.setGender(
                            resultSet.getString("gender")
                    );

                    return employee;
                },
                department,
                department,
                department,
                reviewPeriod
        );
    }

    // ============================================================
    // CHECK EMPLOYEE EXISTS
    // ============================================================

    public boolean employeeExists(String employeeId) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM employees
                    WHERE employee_number = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                employeeId
        );

        return Boolean.TRUE.equals(exists);
    }

    // ============================================================
    // CHECK DUPLICATE REVIEW
    // ============================================================

    public boolean reviewExists(
            String employeeId,
            String reviewPeriod
    ) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM performance_reviews
                    WHERE employee_id = ?
                    AND review_period = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                employeeId,
                reviewPeriod
        );

        return Boolean.TRUE.equals(exists);
    }

    // ============================================================
    // CHECK DUPLICATE REVIEW EXCEPT CURRENT ID
    // ============================================================

    public boolean reviewExistsExceptId(
            String employeeId,
            String reviewPeriod,
            Long id
    ) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM performance_reviews
                    WHERE employee_id = ?
                    AND review_period = ?
                    AND id <> ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                employeeId,
                reviewPeriod,
                id
        );

        return Boolean.TRUE.equals(exists);
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

    public Performance update(
            Long id,
            Performance performance
    ) {

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
                    status = ?,
                    updated_at = CURRENT_TIMESTAMP
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

        int rowsDeleted = jdbcTemplate.update(
                sql,
                id
        );

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

