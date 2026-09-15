package com.staffhub.repository;

import com.staffhub.model.Leave;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LeaveRepository {

    private final JdbcTemplate jdbcTemplate;

    public LeaveRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    // GET ALL LEAVE REQUESTS
    // ============================================================

    public List<Leave> findAll(
            String employeeId,
            String search,
            String department,
            String status
    ) {

        StringBuilder sql = new StringBuilder(
                """
                SELECT
                    l.id,
                    l.employee_id,
                    CONCAT(
                        COALESCE(e.first_name, ''),
                        ' ',
                        COALESCE(e.last_name, '')
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
                """
        );

        List<Object> parameters = new ArrayList<>();

        // --------------------------------------------------------
        // Employee filter
        // --------------------------------------------------------

        if (employeeId != null && !employeeId.isBlank()) {

            sql.append(
                    " AND l.employee_id = ? "
            );

            parameters.add(
                    employeeId.trim()
            );
        }

        // --------------------------------------------------------
        // Search
        // --------------------------------------------------------

        if (search != null && !search.isBlank()) {

            sql.append(
                    """
                    AND (
                        LOWER(COALESCE(e.first_name, ''))
                            LIKE LOWER(?)

                        OR LOWER(COALESCE(e.last_name, ''))
                            LIKE LOWER(?)

                        OR LOWER(COALESCE(l.employee_id, ''))
                            LIKE LOWER(?)
                    )
                    """
            );

            String searchValue =
                    "%" + search.trim() + "%";

            parameters.add(searchValue);
            parameters.add(searchValue);
            parameters.add(searchValue);
        }

        // --------------------------------------------------------
        // Department
        // --------------------------------------------------------

        if (
                department != null
                        && !department.isBlank()
                        && !department.equalsIgnoreCase("All")
        ) {

            sql.append(
                    " AND e.department = ? "
            );

            parameters.add(
                    department.trim()
            );
        }

        // --------------------------------------------------------
        // Status
        // --------------------------------------------------------

        if (
                status != null
                        && !status.isBlank()
                        && !status.equalsIgnoreCase("All")
        ) {

            sql.append(
                    " AND l.status = ? "
            );

            parameters.add(
                    status.trim()
            );
        }

        // --------------------------------------------------------
        // Ordering
        // --------------------------------------------------------

        sql.append(
                """
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
                """
        );

        return jdbcTemplate.query(
                sql.toString(),
                (resultSet, rowNumber) ->
                        mapRow(resultSet),
                parameters.toArray()
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    public Leave findById(Long id) {

        String sql =
                """
                SELECT
                    l.id,
                    l.employee_id,
                    CONCAT(
                        COALESCE(e.first_name, ''),
                        ' ',
                        COALESCE(e.last_name, '')
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

        List<Leave> results =
                jdbcTemplate.query(
                        sql,
                        (resultSet, rowNumber) ->
                                mapRow(resultSet),
                        id
                );

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    // ============================================================
    // CHECK EMPLOYEE EXISTS
    // ============================================================

    public boolean employeeExists(
            String employeeId
    ) {

        String sql =
                """
                SELECT
                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM employees
                            WHERE employee_number = ?
                        )
                        THEN 1
                        ELSE 0
                    END
                """;

        Integer exists =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        employeeId
                );

        return exists != null && exists == 1;
    }

    // ============================================================
    // CREATE
    // ============================================================

    public Leave create(
            Leave leave
    ) {

        String sql =
                """
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
                OUTPUT INSERTED.id
                VALUES (?, ?, ?, ?, ?, ?, 'Pending', ?)
                """;

        Long id =
                jdbcTemplate.queryForObject(
                        sql,
                        Long.class,
                        leave.getEmployeeId(),
                        leave.getType(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getReason(),
                        nullIfBlank(
                                leave.getApproverId()
                        ),
                        nullIfBlank(
                                leave.getComment()
                        )
                );

        if (id == null) {

            throw new IllegalStateException(
                    "Failed to create leave request"
            );
        }

        return findById(id);
    }

    // ============================================================
    // APPROVE
    // ============================================================

    public Leave approve(
            Long id,
            String approverId,
            String comment
    ) {

        String sql =
                """
                UPDATE leave_requests
                SET
                    status = 'Approved',
                    approver_id = ?,
                    comment = ?,
                    updated_at = SYSDATETIME()
                WHERE id = ?
                  AND status = 'Pending'
                """;

        int updated =
                jdbcTemplate.update(
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

        String sql =
                """
                UPDATE leave_requests
                SET
                    status = 'Rejected',
                    approver_id = ?,
                    comment = ?,
                    updated_at = SYSDATETIME()
                WHERE id = ?
                  AND status = 'Pending'
                """;

        int updated =
                jdbcTemplate.update(
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

    public Leave cancel(
            Long id
    ) {

        String sql =
                """
                UPDATE leave_requests
                SET
                    status = 'Cancelled',
                    updated_at = SYSDATETIME()
                WHERE id = ?
                  AND status = 'Pending'
                """;

        int updated =
                jdbcTemplate.update(
                        sql,
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
    // APPROVED LEAVE DAYS
    //
    // SQL Server version
    //
    // Counts weekdays only:
    // Monday-Friday = counted
    // Saturday-Sunday = ignored
    //
    // The date range is generated using a recursive CTE.
    // ============================================================

    public long getApprovedLeaveDays(
            String employeeId,
            String leaveType,
            int year
    ) {

        String sql =
                """
                WITH LeaveDays AS
                (
                    SELECT
                        l.start_date AS leave_date,
                        l.end_date
                    FROM leave_requests l
                    WHERE l.employee_id = ?
                      AND l.leave_type = ?
                      AND l.status = 'Approved'
                      AND YEAR(l.start_date) = ?

                    UNION ALL

                    SELECT
                        DATEADD(
                            DAY,
                            1,
                            leave_date
                        ),
                        end_date
                    FROM LeaveDays
                    WHERE leave_date < end_date
                )
                SELECT
                    COUNT_BIG(*)
                FROM LeaveDays
                WHERE DATEDIFF(
                    DAY,
                    '19000101',
                    leave_date
                ) % 7 NOT IN (5, 6)
                OPTION (MAXRECURSION 0)
                """;

        Long result =
                jdbcTemplate.queryForObject(
                        sql,
                        Long.class,
                        employeeId,
                        leaveType,
                        year
                );

        if (result == null) {
            return 0;
        }

        return result;
    }

    // ============================================================
    // MAP DATABASE ROW
    // ============================================================

    private Leave mapRow(
            ResultSet resultSet
    ) throws SQLException {

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

        if (
                resultSet.getDate("start_date")
                        != null
        ) {

            leave.setStartDate(
                    resultSet
                            .getDate("start_date")
                            .toLocalDate()
            );
        }

        if (
                resultSet.getDate("end_date")
                        != null
        ) {

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

    private String nullIfBlank(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}