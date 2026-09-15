package com.staffhub.repository;

import com.staffhub.model.Leave;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LeaveRepository {

    private final JdbcTemplate jdbcTemplate;

    public LeaveRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // ============================================================
    // GET ALL / FILTER
    // ============================================================

    public List<Leave> findAll(
            String employeeId,
            String search,
            String department,
            String status
    ) {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    l.id,
                    l.employee_id,
                    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
                    e.department,
                    l.leave_type,
                    l.start_date,
                    l.end_date,
                    l.reason,
                    l.approver_id,
                    l.status,
                    l.comment
                FROM leave_requests l
                INNER JOIN employees e
                    ON e.employee_number = l.employee_id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();


        // Employee-specific records
        if (employeeId != null && !employeeId.isBlank()) {
            sql.append(" AND l.employee_id = ?");
            params.add(employeeId);
        }


        // Search employee
        if (search != null && !search.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(e.first_name) LIKE LOWER(?)
                        OR LOWER(e.last_name) LIKE LOWER(?)
                        OR LOWER(CONCAT(e.first_name, ' ', e.last_name)) LIKE LOWER(?)
                        OR LOWER(l.employee_id) LIKE LOWER(?)
                    )
                    """);

            String searchValue = "%" + search.trim() + "%";

            params.add(searchValue);
            params.add(searchValue);
            params.add(searchValue);
            params.add(searchValue);
        }


        // Department filter
        if (department != null
                && !department.isBlank()
                && !department.equalsIgnoreCase("All")) {

            sql.append(" AND e.department = ?");
            params.add(department);
        }


        // Status filter
        if (status != null
                && !status.isBlank()
                && !status.equalsIgnoreCase("All")) {

            sql.append(" AND l.status = ?");
            params.add(status);
        }


        sql.append("""
                ORDER BY
                    CASE
                        WHEN l.status = 'Pending' THEN 1
                        WHEN l.status = 'Approved' THEN 2
                        WHEN l.status = 'Rejected' THEN 3
                        ELSE 4
                    END,
                    l.start_date DESC,
                    l.id DESC
                """);


        return jdbcTemplate.query(
                sql.toString(),
                (resultSet, rowNumber) -> mapRow(resultSet),
                params.toArray()
        );
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    public Leave findById(Long id) {

        String sql = """
                SELECT
                    l.id,
                    l.employee_id,
                    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
                    e.department,
                    l.leave_type,
                    l.start_date,
                    l.end_date,
                    l.reason,
                    l.approver_id,
                    l.status,
                    l.comment
                FROM leave_requests l
                INNER JOIN employees e
                    ON e.employee_number = l.employee_id
                WHERE l.id = ?
                """;

        List<Leave> results = jdbcTemplate.query(
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
    // CREATE
    // ============================================================

    public Leave create(Leave leave) {

        String sql = """
                INSERT INTO leave_requests (
                    employee_id,
                    leave_type,
                    start_date,
                    end_date,
                    reason,
                    approver_id,
                    status,
                    comment
                )
                VALUES (?, ?, ?, ?, ?, ?, 'Pending', ?)
                RETURNING id
                """;

        Long generatedId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                leave.getEmployeeId(),
                leave.getType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                nullIfBlank(leave.getApproverId()),
                nullIfBlank(leave.getComment())
        );

        leave.setId(generatedId);
        leave.setStatus("Pending");

        return leave;
    }


    // ============================================================
    // APPROVE
    // ============================================================

    public Leave approve(
            Long id,
            String approverId,
            String comment
    ) {

        String sql = """
                UPDATE leave_requests
                SET
                    status = 'Approved',
                    approver_id = ?,
                    comment = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND status = 'Pending'
                """;

        int updated = jdbcTemplate.update(
                sql,
                nullIfBlank(approverId),
                nullIfBlank(comment),
                id
        );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Pending leave request not found"
            );
        }

        return findById(id);
    }


    // ============================================================
    // REJECT
    // ============================================================

    public Leave reject(
            Long id,
            String approverId,
            String comment
    ) {

        String sql = """
                UPDATE leave_requests
                SET
                    status = 'Rejected',
                    approver_id = ?,
                    comment = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND status = 'Pending'
                """;

        int updated = jdbcTemplate.update(
                sql,
                nullIfBlank(approverId),
                nullIfBlank(comment),
                id
        );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Pending leave request not found"
            );
        }

        return findById(id);
    }


    // ============================================================
    // CANCEL
    // ============================================================

    public Leave cancel(Long id) {

        String sql = """
                UPDATE leave_requests
                SET
                    status = 'Cancelled',
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND status = 'Pending'
                """;

        int updated = jdbcTemplate.update(sql, id);

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Pending leave request not found"
            );
        }

        return findById(id);
    }


    // ============================================================
    // LEAVE BALANCE
    // ============================================================

    public long getApprovedLeaveDays(
            String employeeId,
            String leaveType,
            int year
    ) {

        String sql = """
                SELECT COALESCE(
                    SUM(
                        (end_date - start_date) + 1
                    ),
                    0
                )
                FROM leave_requests
                WHERE employee_id = ?
                  AND leave_type = ?
                  AND status = 'Approved'
                  AND EXTRACT(YEAR FROM start_date) = ?
                """;

        Number result = jdbcTemplate.queryForObject(
                sql,
                Number.class,
                employeeId,
                leaveType,
                year
        );

        return result == null ? 0 : result.longValue();
    }


    // ============================================================
    // RESULT MAPPER
    // ============================================================

    private Leave mapRow(
            java.sql.ResultSet resultSet
    ) throws java.sql.SQLException {

        Leave leave = new Leave();

        leave.setId(
                resultSet.getLong("id")
        );

        leave.setEmployeeId(
                resultSet.getString("employee_id")
        );

        leave.setEmployeeName(
                resultSet.getString("employee_name")
        );

        leave.setDepartment(
                resultSet.getString("department")
        );

        leave.setType(
                resultSet.getString("leave_type")
        );

        LocalDate startDate =
                resultSet.getDate("start_date") != null
                        ? resultSet.getDate("start_date").toLocalDate()
                        : null;

        LocalDate endDate =
                resultSet.getDate("end_date") != null
                        ? resultSet.getDate("end_date").toLocalDate()
                        : null;

        leave.setStartDate(startDate);
        leave.setEndDate(endDate);

        leave.setReason(
                resultSet.getString("reason")
        );

        leave.setApproverId(
                resultSet.getString("approver_id")
        );

        leave.setStatus(
                resultSet.getString("status")
        );

        leave.setComment(
                resultSet.getString("comment")
        );

        return leave;
    }


    // ============================================================
    // HELPER
    // ============================================================

    private String nullIfBlank(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }
}