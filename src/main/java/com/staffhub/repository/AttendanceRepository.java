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
            Long companyId,
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
                        FROM dbo.attendance_records a
                        INNER JOIN dbo.employees e
                            ON e.id = a.employee_id
                        WHERE a.employee_id = ?
                          AND a.company_id = ?
                          AND e.company_id = ?
                          AND a.attendance_date = ?
                        """,
                        this::map,
                        employeeId,
                        companyId,
                        companyId,
                        date
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public AttendanceRecord findTodayByEmployee(
            Long employeeId,
            Long companyId
    ) {
        return findByEmployeeAndDate(
                employeeId,
                companyId,
                LocalDate.now()
        );
    }

    public List<AttendanceRecord> findByEmployee(
            Long employeeId,
            Long companyId
    ) {

        return jdbc.query(
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
                WHERE a.employee_id = ?
                  AND a.company_id = ?
                  AND e.company_id = ?
                ORDER BY
                    a.attendance_date DESC,
                    a.check_in DESC
                """,
                this::map,
                employeeId,
                companyId,
                companyId
        );
    }

    public List<AttendanceRecord> findByDate(
            Long companyId,
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
                FROM dbo.attendance_records a
                INNER JOIN dbo.employees e
                    ON e.id = a.employee_id
                WHERE a.company_id = ?
                  AND e.company_id = ?
                  AND a.attendance_date = ?
                ORDER BY
                    CASE
                        WHEN a.check_in IS NULL
                        THEN 1
                        ELSE 0
                    END,
                    a.check_in
                """,
                this::map,
                companyId,
                companyId,
                date
        );
    }

    public Long create(
            Long employeeId,
            Long companyId,
            LocalDate date,
            LocalDateTime checkIn,
            String method,
            Long sessionId,
            String status
    ) {

        jdbc.update(
                """
                INSERT INTO dbo.attendance_records
                (
                    company_id,
                    employee_id,
                    attendance_date,
                    check_in,
                    status,
                    check_in_method,
                    qr_session_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                companyId,
                employeeId,
                date,
                Timestamp.valueOf(checkIn),
                status,
                method,
                sessionId
        );

        return jdbc.queryForObject(
                """
                SELECT CAST(
                    SCOPE_IDENTITY()
                    AS BIGINT
                )
                """,
                Long.class
        );
    }

    public void updateCheckOut(
            Long id,
            Long companyId,
            LocalDateTime checkOut,
            String method
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE dbo.attendance_records
                        SET
                            check_out = ?,
                            check_out_method = ?,
                            updated_at = SYSDATETIME()
                        WHERE id = ?
                          AND company_id = ?
                          AND check_out IS NULL
                        """,
                        Timestamp.valueOf(checkOut),
                        method,
                        id,
                        companyId
                );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Attendance record does not belong to your company"
            );
        }
    }

    public void updateCorrection(
            Long id,
            Long companyId,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            String status,
            String reason
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE dbo.attendance_records
                        SET
                            check_in = ?,
                            check_out = ?,
                            status = ?,
                            manual_correction = 1,
                            correction_reason = ?,
                            updated_at = SYSDATETIME()
                        WHERE id = ?
                          AND company_id = ?
                        """,
                        checkIn == null
                                ? null
                                : Timestamp.valueOf(checkIn),
                        checkOut == null
                                ? null
                                : Timestamp.valueOf(checkOut),
                        status,
                        reason,
                        id,
                        companyId
                );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Attendance record does not belong to your company"
            );
        }
    }

    public Long resolveEmployeeId(
            String employeeIdentifier,
            Long companyId
    ) {

        if (
                employeeIdentifier == null
                        || employeeIdentifier.isBlank()
        ) {
            return null;
        }

        String value =
                employeeIdentifier.trim();

        List<Long> result =
                jdbc.query(
                        """
                        SELECT TOP 1 e.id
                        FROM dbo.employees e
                        WHERE e.company_id = ?
                          AND e.employment_status = 'Active'
                          AND (
                              e.employee_number = ?
                              OR CAST(
                                  e.id AS VARCHAR(50)
                              ) = ?
                          )
                        """,
                        (rs, row) ->
                                rs.getLong("id"),
                        companyId,
                        value,
                        value
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public boolean employeeOnApprovedLeave(
            String employeeIdentifier,
            Long companyId,
            LocalDate date
    ) {

        if (
                employeeIdentifier == null
                        || employeeIdentifier.isBlank()
        ) {
            return false;
        }

        String value =
                employeeIdentifier.trim();

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.leave_requests l
                        INNER JOIN dbo.employees e
                            ON e.employee_number =
                               l.employee_id
                        WHERE e.company_id = ?
                          AND l.company_id = ?
                          AND (
                              e.employee_number = ?
                              OR CAST(
                                  e.id AS VARCHAR(50)
                              ) = ?
                          )
                          AND l.status = 'Approved'
                          AND ? BETWEEN
                              l.start_date
                              AND l.end_date
                        """,
                        Integer.class,
                        companyId,
                        companyId,
                        value,
                        value,
                        date
                );

        return count != null
                && count > 0;
    }

    public int countActiveEmployees(
            Long companyId
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.employees
                        WHERE company_id = ?
                          AND employment_status = 'Active'
                        """,
                        Integer.class,
                        companyId
                );

        return count == null
                ? 0
                : count;
    }

    public int countEmployeesOnApprovedLeave(
            Long companyId,
            LocalDate date
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(DISTINCT e.id)
                        FROM dbo.employees e
                        INNER JOIN dbo.leave_requests l
                            ON l.employee_id =
                               e.employee_number
                        WHERE e.company_id = ?
                          AND l.company_id = ?
                          AND e.employment_status = 'Active'
                          AND l.status = 'Approved'
                          AND ? BETWEEN
                              l.start_date
                              AND l.end_date
                        """,
                        Integer.class,
                        companyId,
                        companyId,
                        date
                );

        return count == null
                ? 0
                : count;
    }

    public int countAttended(
            Long companyId,
            LocalDate date
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.attendance_records
                        WHERE company_id = ?
                          AND attendance_date = ?
                          AND check_in IS NOT NULL
                        """,
                        Integer.class,
                        companyId,
                        date
                );

        return count == null
                ? 0
                : count;
    }

    public int countCheckedOut(
            Long companyId,
            LocalDate date
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.attendance_records
                        WHERE company_id = ?
                          AND attendance_date = ?
                          AND check_out IS NOT NULL
                        """,
                        Integer.class,
                        companyId,
                        date
                );

        return count == null
                ? 0
                : count;
    }

    public int countCurrentlyWorking(
            Long companyId,
            LocalDate date
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.attendance_records
                        WHERE company_id = ?
                          AND attendance_date = ?
                          AND check_in IS NOT NULL
                          AND check_out IS NULL
                        """,
                        Integer.class,
                        companyId,
                        date
                );

        return count == null
                ? 0
                : count;
    }

    public int countLate(
            Long companyId,
            LocalDate date
    ) {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.attendance_records
                        WHERE company_id = ?
                          AND attendance_date = ?
                          AND status = 'LATE'
                        """,
                        Integer.class,
                        companyId,
                        date
                );

        return count == null
                ? 0
                : count;
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

        String firstName =
                rs.getString("first_name");

        String lastName =
                rs.getString("last_name");

        record.setEmployeeName(
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
                )
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