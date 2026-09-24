package com.staffhub.repository;

import com.staffhub.model.AttendanceRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AttendanceRepository {

    private final JdbcTemplate jdbc;

    public AttendanceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AttendanceRecord findByEmployeeAndDate(
            Long employeeId,
            LocalDate date
    ) {

        List<AttendanceRecord> result =
                jdbc.query(
                        """
                        SELECT
                            a.*,
                            e.employee_number,
                            e.first_name,
                            e.last_name,
                            e.department
                        FROM attendance_records a
                        INNER JOIN employees e
                            ON e.id = a.employee_id
                        WHERE a.employee_id = ?
                          AND a.attendance_date = ?
                        """,
                        this::map,
                        employeeId,
                        date
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public AttendanceRecord findTodayByEmployee(
            Long employeeId
    ) {
        return findByEmployeeAndDate(
                employeeId,
                LocalDate.now()
        );
    }

    public List<AttendanceRecord> findByEmployee(
            Long employeeId
    ) {

        return jdbc.query(
                """
                SELECT
                    a.*,
                    e.employee_number,
                    e.first_name,
                    e.last_name,
                    e.department
                FROM attendance_records a
                INNER JOIN employees e
                    ON e.id = a.employee_id
                WHERE a.employee_id = ?
                ORDER BY
                    a.attendance_date DESC,
                    a.check_in DESC
                """,
                this::map,
                employeeId
        );
    }

    public List<AttendanceRecord> findByDate(
            LocalDate date
    ) {

        return jdbc.query(
                """
                SELECT
                    a.*,
                    e.employee_number,
                    e.first_name,
                    e.last_name,
                    e.department
                FROM attendance_records a
                INNER JOIN employees e
                    ON e.id = a.employee_id
                WHERE a.attendance_date = ?
                ORDER BY
                    CASE
                        WHEN a.check_in IS NULL THEN 1
                        ELSE 0
                    END,
                    a.check_in
                """,
                this::map,
                date
        );
    }

    public Long create(
            Long employeeId,
            LocalDate date,
            LocalDateTime checkIn,
            String method,
            Long sessionId,
            String status
    ) {

        jdbc.update(
                """
                INSERT INTO attendance_records
                (
                    employee_id,
                    attendance_date,
                    check_in,
                    status,
                    check_in_method,
                    qr_session_id
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                employeeId,
                date,
                Timestamp.valueOf(checkIn),
                status,
                method,
                sessionId
        );

        return jdbc.queryForObject(
                "SELECT CAST(SCOPE_IDENTITY() AS BIGINT)",
                Long.class
        );
    }

    public void updateCheckOut(
            Long id,
            LocalDateTime checkOut,
            String method
    ) {

        jdbc.update(
                """
                UPDATE attendance_records
                SET
                    check_out = ?,
                    check_out_method = ?,
                    updated_at = SYSDATETIME()
                WHERE id = ?
                  AND check_out IS NULL
                """,
                Timestamp.valueOf(checkOut),
                method,
                id
        );
    }

    public void updateCorrection(
            Long id,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            String status,
            String reason
    ) {

        jdbc.update(
                """
                UPDATE attendance_records
                SET
                    check_in = ?,
                    check_out = ?,
                    status = ?,
                    manual_correction = 1,
                    correction_reason = ?,
                    updated_at = SYSDATETIME()
                WHERE id = ?
                """,
                checkIn == null
                        ? null
                        : Timestamp.valueOf(checkIn),

                checkOut == null
                        ? null
                        : Timestamp.valueOf(checkOut),

                status,
                reason,
                id
        );
    }

    public boolean employeeExists(Long employeeId) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM employees
                        WHERE id = ?
                          AND employment_status = 'Active'
                        """,
                        Integer.class,
                        employeeId
                );

        return count != null && count > 0;
    }

    public boolean employeeOnApprovedLeave(
            Long employeeId,
            LocalDate date
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM leave_requests
                        WHERE employee_id =
                            (
                                SELECT employee_number
                                FROM employees
                                WHERE id = ?
                            )
                          AND status = 'Approved'
                          AND ? BETWEEN start_date AND end_date
                        """,
                        Integer.class,
                        employeeId,
                        date
                );

        return count != null && count > 0;
    }

    public int countActiveEmployees() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM employees
                        WHERE employment_status = 'Active'
                        """,
                        Integer.class
                );

        return count == null ? 0 : count;
    }

    public int countEmployeesOnApprovedLeave(
            LocalDate date
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(DISTINCT e.id)
                        FROM employees e
                        INNER JOIN leave_requests l
                            ON l.employee_id = e.employee_number
                        WHERE e.employment_status = 'Active'
                          AND l.status = 'Approved'
                          AND ? BETWEEN l.start_date AND l.end_date
                        """,
                        Integer.class,
                        date
                );

        return count == null ? 0 : count;
    }

    public int countAttended(LocalDate date) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM attendance_records
                        WHERE attendance_date = ?
                          AND check_in IS NOT NULL
                        """,
                        Integer.class,
                        date
                );

        return count == null ? 0 : count;
    }

    public int countCheckedOut(LocalDate date) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM attendance_records
                        WHERE attendance_date = ?
                          AND check_out IS NOT NULL
                        """,
                        Integer.class,
                        date
                );

        return count == null ? 0 : count;
    }

    public int countCurrentlyWorking(LocalDate date) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM attendance_records
                        WHERE attendance_date = ?
                          AND check_in IS NOT NULL
                          AND check_out IS NULL
                        """,
                        Integer.class,
                        date
                );

        return count == null ? 0 : count;
    }

    public int countLate(LocalDate date) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM attendance_records
                        WHERE attendance_date = ?
                          AND status = 'LATE'
                        """,
                        Integer.class,
                        date
                );

        return count == null ? 0 : count;
    }

    private AttendanceRecord map(
            java.sql.ResultSet rs,
            int row
    ) throws java.sql.SQLException {

        AttendanceRecord record =
                new AttendanceRecord();

        record.setId(
                rs.getLong("id")
        );

        record.setEmployeeId(
                rs.getLong("employee_id")
        );

        record.setEmployeeNumber(
                rs.getString("employee_number")
        );

        record.setEmployeeName(
                (
                        rs.getString("first_name") == null
                                ? ""
                                : rs.getString("first_name")
                )
                + " "
                +
                (
                        rs.getString("last_name") == null
                                ? ""
                                : rs.getString("last_name")
                )
        );

        record.setDepartment(
                rs.getString("department")
        );

        record.setAttendanceDate(
                rs.getDate("attendance_date")
                        .toLocalDate()
        );

        Timestamp in =
                rs.getTimestamp("check_in");

        Timestamp out =
                rs.getTimestamp("check_out");

        if (in != null) {
            record.setCheckIn(
                    in.toLocalDateTime()
            );
        }

        if (out != null) {
            record.setCheckOut(
                    out.toLocalDateTime()
            );
        }

        record.setStatus(
                rs.getString("status")
        );

        record.setCheckInMethod(
                rs.getString("check_in_method")
        );

        record.setCheckOutMethod(
                rs.getString("check_out_method")
        );

        long session =
                rs.getLong("qr_session_id");

        if (!rs.wasNull()) {
            record.setQrSessionId(session);
        }

        record.setManualCorrection(
                rs.getBoolean("manual_correction")
        );

        record.setCorrectionReason(
                rs.getString("correction_reason")
        );

        return record;
    }
}