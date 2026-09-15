package com.staffhub.repository;

import com.staffhub.model.Leave;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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
                    COALESCE(
                        CONCAT(e.first_name, ' ', e.last_name),
                        l.employee_id
                    ) AS employee_name,
                    e.department,
                    l.leave_type,
                    l.start_date,
                    l.end_date,
                    l.reason,
                    l.approver_id,
                    l.status,
                    l.comment
                FROM leave_requests l
                LEFT JOIN employees e
                    ON e.employee_number = l.employee_id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        // Employee filter
        if (employeeId != null && !employeeId.isBlank()) {
            sql.append(" AND l.employee_id = ?");
            params.add(employeeId.trim());
        }

        // Search filter
        if (search != null && !search.isBlank()) {

            sql.append("""
                    AND (
                        LOWER(COALESCE(e.first_name, '')) LIKE LOWER(?)
                        OR LOWER(COALESCE(e.last_name, '')) LIKE LOWER(?)
                        OR LOWER(
                            COALESCE(
                                CONCAT(e.first_name, ' ', e.last_name),
                                ''
                            )
                        ) LIKE LOWER(?)
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
            params.add(department.trim());
        }

        // Status filter
        if (status != null
                && !status.isBlank()
                && !status.equalsIgnoreCase("All")) {

            sql.append(" AND l.status = ?");
            params.add(status.trim());
        }

        sql.append("""
                ORDER BY
                    CASE
                        WHEN l.status = 'Pending' THEN 1
                        WHEN l.status = 'Approved' THEN 2
                        WHEN l.status = 'Rejected' THEN 3
                        WHEN l.status = 'Cancelled' THEN 4
                        ELSE 5
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
                    COALESCE(
                        CONCAT(e.first_name, ' ', e.last_name),
                        l.employee_id
                    ) AS employee_name,
                    e.department,
                    l.leave_type,
                    l.start_date,
                    l.end_date,
                    l.reason,
                    l.approver_id,
                    l.status,
                    l.comment
                FROM leave_requests l
                LEFT JOIN employees e
                    ON e.employee_number = l.employee_id
                WHERE l.id = ?
                """;

        List<Leave> results = jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> mapRow(resultSet),
                id
        );

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    // ============================================================
    // CHECK EMPLOYEE
    // ============================================================

    public boolean employeeExists(String employeeId) {

        String sql = """
                SELECT COUNT(*)
                FROM employees
                WHERE employee_number = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                employeeId
        );

        return count != null && count > 0;
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

        return findById(generatedId);
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
                    "Leave request does not exist or is no longer pending"
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
                    "Leave request does not exist or is no longer pending"
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
                    "Leave request does not exist or is no longer pending"
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
                SELECT
                    COALESCE(
                        SUM(
                            CASE
                                WHEN EXTRACT(
                                    DOW FROM day
                                ) NOT IN (0, 6)
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    )
                FROM leave_requests,
                     generate_series(
                         start_date,
                         end_date,
                         INTERVAL '1 day'
                     ) AS day
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

        return result == null
                ? 0
                : result.longValue();
    }

    // ============================================================
    // RESULT MAPPER
    // ============================================================

    private Leave mapRow(ResultSet resultSet)
            throws SQLException {

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

        if (resultSet.getDate("start_date") != null) {
            leave.setStartDate(
                    resultSet
                            .getDate("start_date")
                            .toLocalDate()
            );
        }

        if (resultSet.getDate("end_date") != null) {
            leave.setEndDate(
                    resultSet
                            .getDate("end_date")
                            .toLocalDate()
            );
        }

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

        return value.trim();
    }
}

