package com.staffhub.repository;

import com.staffhub.model.AttendanceRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AttendanceManagementRepository {

    private final JdbcTemplate jdbc;

    public AttendanceManagementRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // ============================================================
    // FIND RECORD
    // ============================================================

    public AttendanceRecord findById(
            Long id,
            Long companyId
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
                        FROM dbo.attendance_records a
                        INNER JOIN dbo.employees e
                            ON e.id = a.employee_id
                        WHERE a.id = ?
                          AND a.company_id = ?
                          AND e.company_id = ?
                        """,
                        this::map,
                        id,
                        companyId,
                        companyId
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    // ============================================================
    // FIND EMPLOYEE
    // ============================================================

    public Long findActiveEmployeeByNumber(
            String employeeNumber,
            Long companyId
    ) {

        if (
                employeeNumber == null
                        || employeeNumber.isBlank()
        ) {
            return null;
        }

        List<Long> result =
                jdbc.query(
                        """
                        SELECT TOP 1 id
                        FROM dbo.employees
                        WHERE company_id = ?
                          AND employee_number = ?
                          AND employment_status = 'Active'
                        """,
                        (rs, rowNum) ->
                                rs.getLong("id"),
                        companyId,
                        employeeNumber.trim()
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    // ============================================================
    // CHECK EXISTING RECORD
    // ============================================================

    public boolean existsForEmployeeAndDate(
            Long employeeId,
            Long companyId,
            LocalDate date
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.attendance_records
                        WHERE employee_id = ?
                          AND company_id = ?
                          AND attendance_date = ?
                        """,
                        Integer.class,
                        employeeId,
                        companyId,
                        date
                );

        return count != null
                && count > 0;
    }

    // ============================================================
    // CREATE MANUAL ATTENDANCE
    // ============================================================

    public Long createManual(
            Long employeeId,
            Long companyId,
            LocalDate date,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            String status,
            String reason
    ) {

        jdbc.update(
                """
                INSERT INTO dbo.attendance_records
                (
                    company_id,
                    employee_id,
                    attendance_date,
                    check_in,
                    check_out,
                    status,
                    check_in_method,
                    check_out_method,
                    qr_session_id,
                    manual_correction,
                    correction_reason,
                    updated_at
                )
                VALUES
                (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    NULL,
                    1,
                    ?,
                    SYSDATETIME()
                )
                """,

                companyId,

                employeeId,

                date,

                checkIn == null
                        ? null
                        : Timestamp.valueOf(checkIn),

                checkOut == null
                        ? null
                        : Timestamp.valueOf(checkOut),

                status,

                checkIn == null
                        ? null
                        : "MANUAL",

                checkOut == null
                        ? null
                        : "MANUAL",

                reason
        );

        Long id =
                jdbc.queryForObject(
                        """
                        SELECT CAST(
                            SCOPE_IDENTITY()
                            AS BIGINT
                        )
                        """,
                        Long.class
                );

        if (id == null) {
            throw new IllegalStateException(
                    "Manual attendance was created but its ID could not be retrieved"
            );
        }

        return id;
    }

    // ============================================================
    // DELETE RECORD
    // ============================================================

    public void deleteRecord(
            Long id,
            Long companyId
    ) {

        /*
         * Attendance events may reference this record.
         *
         * Detach the event first so the actual attendance row
         * can be deleted without breaking the audit/event table.
         */
        jdbc.update(
                """
                UPDATE dbo.attendance_events
                SET attendance_record_id = NULL
                WHERE attendance_record_id = ?
                  AND company_id = ?
                """,
                id,
                companyId
        );

        int deleted =
                jdbc.update(
                        """
                        DELETE FROM dbo.attendance_records
                        WHERE id = ?
                          AND company_id = ?
                        """,
                        id,
                        companyId
                );

        if (deleted == 0) {
            throw new IllegalArgumentException(
                    "Attendance record was not found in the current company"
            );
        }
    }

    // ============================================================
    // AUDIT EVENT
    // ============================================================

    public void createManagementEvent(
            Long companyId,
            Long attendanceRecordId,
            Long employeeId,
            String action,
            String performedBy,
            String details
    ) {

        jdbc.update(
                """
                INSERT INTO dbo.attendance_events
                (
                    company_id,
                    monitor_id,
                    attendance_record_id,
                    employee_id,
                    action,
                    event_time,
                    qr_sequence,
                    performed_by,
                    details
                )
                VALUES
                (
                    ?,
                    NULL,
                    ?,
                    ?,
                    ?,
                    SYSDATETIME(),
                    NULL,
                    ?,
                    ?
                )
                """,

                companyId,

                attendanceRecordId,

                employeeId,

                action,

                performedBy,

                details
        );
    }

    // ============================================================
    // FIND CURRENT USER ROLE
    // ============================================================

    public String findCurrentUserRole(
            String email
    ) {

        List<String> result =
                jdbc.query(
                        """
                        SELECT TOP 1 e.role
                        FROM dbo.staffhub_auth_users a
                        INNER JOIN dbo.employees e
                            ON e.id = a.employee_id
                        WHERE LOWER(a.email) = LOWER(?)
                          AND a.enabled = 1
                        """,
                        (rs, rowNum) ->
                                rs.getString("role"),
                        email
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    // ============================================================
    // MAP
    // ============================================================

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

        String firstName =
                rs.getString("first_name");

        String lastName =
                rs.getString("last_name");

        String employeeName =
                (
                        firstName == null
                                ? ""
                                : firstName
                )
                + " "
                +
                (
                        lastName == null
                                ? ""
                                : lastName
                );

        record.setEmployeeName(
                employeeName.trim()
        );

        record.setDepartment(
                rs.getString("department")
        );

        if (
                rs.getDate("attendance_date")
                        != null
        ) {

            record.setAttendanceDate(
                    rs.getDate(
                            "attendance_date"
                    ).toLocalDate()
            );
        }

        Timestamp checkIn =
                rs.getTimestamp("check_in");

        Timestamp checkOut =
                rs.getTimestamp("check_out");

        if (checkIn != null) {

            record.setCheckIn(
                    checkIn.toLocalDateTime()
            );
        }

        if (checkOut != null) {

            record.setCheckOut(
                    checkOut.toLocalDateTime()
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

        long sessionId =
                rs.getLong("qr_session_id");

        if (!rs.wasNull()) {

            record.setQrSessionId(
                    sessionId
            );
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

