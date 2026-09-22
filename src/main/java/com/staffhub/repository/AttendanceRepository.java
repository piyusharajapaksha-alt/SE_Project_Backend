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

    public AttendanceRecord findTodayByEmployee(Long employeeId) {

        String sql = """
            SELECT
                a.*,
                e.employee_number,
                e.first_name,
                e.last_name,
                e.department
            FROM attendance_records a
            INNER JOIN employees e ON e.id = a.employee_id
            WHERE a.employee_id = ?
              AND a.attendance_date = CAST(GETDATE() AS DATE)
            """;

        List<AttendanceRecord> records = jdbc.query(
                sql,
                this::map,
                employeeId
        );

        return records.isEmpty() ? null : records.get(0);
    }

    public List<AttendanceRecord> findByEmployee(Long employeeId) {

        String sql = """
            SELECT
                a.*,
                e.employee_number,
                e.first_name,
                e.last_name,
                e.department
            FROM attendance_records a
            INNER JOIN employees e ON e.id = a.employee_id
            WHERE a.employee_id = ?
            ORDER BY a.attendance_date DESC
            """;

        return jdbc.query(sql, this::map, employeeId);
    }

    public List<AttendanceRecord> findByDate(LocalDate date) {

        String sql = """
            SELECT
                a.*,
                e.employee_number,
                e.first_name,
                e.last_name,
                e.department
            FROM attendance_records a
            INNER JOIN employees e ON e.id = a.employee_id
            WHERE a.attendance_date = ?
            ORDER BY
                CASE
                    WHEN a.check_in IS NULL THEN 1
                    ELSE 0
                END,
                a.check_in
            """;

        return jdbc.query(
                sql,
                this::map,
                date
        );
    }

    public AttendanceRecord findByEmployeeAndDate(
            Long employeeId,
            LocalDate date
    ) {

        String sql = """
            SELECT
                a.*,
                e.employee_number,
                e.first_name,
                e.last_name,
                e.department
            FROM attendance_records a
            INNER JOIN employees e ON e.id = a.employee_id
            WHERE a.employee_id = ?
              AND a.attendance_date = ?
            """;

        List<AttendanceRecord> records =
                jdbc.query(sql, this::map, employeeId, date);

        return records.isEmpty() ? null : records.get(0);
    }

    public Long create(
            Long employeeId,
            LocalDate date,
            LocalDateTime checkIn,
            String method,
            Long sessionId
    ) {

        String sql = """
            INSERT INTO attendance_records
            (
                employee_id,
                attendance_date,
                check_in,
                status,
                check_in_method,
                qr_session_id
            )
            VALUES (?, ?, ?, 'PRESENT', ?, ?)
            """;

        jdbc.update(
                sql,
                employeeId,
                date,
                Timestamp.valueOf(checkIn),
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
                    status = 'PRESENT',
                    updated_at = SYSDATETIME()
                WHERE id = ?
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
                checkIn == null ? null : Timestamp.valueOf(checkIn),
                checkOut == null ? null : Timestamp.valueOf(checkOut),
                status,
                reason,
                id
        );
    }

    private AttendanceRecord map(
            java.sql.ResultSet rs,
            int row
    ) throws java.sql.SQLException {

        AttendanceRecord a = new AttendanceRecord();

        a.setId(rs.getLong("id"));
        a.setEmployeeId(rs.getLong("employee_id"));

        a.setEmployeeNumber(
                rs.getString("employee_number")
        );

        a.setEmployeeName(
                rs.getString("first_name")
                        + " "
                        + rs.getString("last_name")
        );

        a.setDepartment(
                rs.getString("department")
        );

        a.setAttendanceDate(
                rs.getDate("attendance_date").toLocalDate()
        );

        Timestamp in = rs.getTimestamp("check_in");
        Timestamp out = rs.getTimestamp("check_out");

        if (in != null) {
            a.setCheckIn(in.toLocalDateTime());
        }

        if (out != null) {
            a.setCheckOut(out.toLocalDateTime());
        }

        a.setStatus(rs.getString("status"));
        a.setCheckInMethod(rs.getString("check_in_method"));
        a.setCheckOutMethod(rs.getString("check_out_method"));

        long sessionId = rs.getLong("qr_session_id");

        if (!rs.wasNull()) {
            a.setQrSessionId(sessionId);
        }

        a.setManualCorrection(
                rs.getBoolean("manual_correction")
        );

        a.setCorrectionReason(
                rs.getString("correction_reason")
        );

        return a;
    }
}