package com.staffhub.repository;

import com.staffhub.model.AttendanceEvent;
import com.staffhub.model.AttendanceMonitor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AttendanceMonitorRepository {

    private final JdbcTemplate jdbc;

    public AttendanceMonitorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AttendanceMonitor getMonitor() {

        List<AttendanceMonitor> result =
                jdbc.query(
                        """
                        SELECT TOP 1 *
                        FROM attendance_monitor
                        ORDER BY id DESC
                        """,
                        this::mapMonitor
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public AttendanceMonitor getActiveMonitor() {

        List<AttendanceMonitor> result =
                jdbc.query(
                        """
                        SELECT TOP 1 *
                        FROM attendance_monitor
                        WHERE active = 1
                        ORDER BY id DESC
                        """,
                        this::mapMonitor
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public Long create(String activationCode) {

        jdbc.update(
                """
                INSERT INTO attendance_monitor
                (
                    activation_code,
                    active,
                    activation_type,
                    qr_sequence
                )
                VALUES (?, 0, 'MANUAL', 0)
                """,
                activationCode
        );

        return jdbc.queryForObject(
                "SELECT CAST(SCOPE_IDENTITY() AS BIGINT)",
                Long.class
        );
    }

    public void activate(
            Long id,
            String activatedBy,
            String activationType,
            String token,
            int sequence,
            LocalDateTime created,
            LocalDateTime expires
    ) {

        jdbc.update(
                """
                UPDATE attendance_monitor
                SET
                    active = 1,
                    activation_type = ?,
                    activated_by = ?,
                    activated_at = SYSDATETIME(),
                    deactivated_at = NULL,
                    current_qr_token = ?,
                    qr_sequence = ?,
                    qr_created_at = ?,
                    qr_expires_at = ?
                WHERE id = ?
                """,
                activationType,
                activatedBy,
                token,
                sequence,
                Timestamp.valueOf(created),
                Timestamp.valueOf(expires),
                id
        );
    }

    public void deactivate(
            Long id,
            String newActivationCode
    ) {

        jdbc.update(
                """
                UPDATE attendance_monitor
                SET
                    active = 0,
                    deactivated_at = SYSDATETIME(),
                    current_qr_token = NULL,
                    qr_created_at = NULL,
                    qr_expires_at = NULL,
                    activation_code = ?
                WHERE id = ?
                """,
                newActivationCode,
                id
        );
    }

    public void rotate(
            Long id,
            String token,
            int sequence,
            LocalDateTime created,
            LocalDateTime expires
    ) {

        jdbc.update(
                """
                UPDATE attendance_monitor
                SET
                    current_qr_token = ?,
                    qr_sequence = ?,
                    qr_created_at = ?,
                    qr_expires_at = ?
                WHERE id = ?
                  AND active = 1
                """,
                token,
                sequence,
                Timestamp.valueOf(created),
                Timestamp.valueOf(expires),
                id
        );
    }

    public Long createSession(
            Long monitorId,
            String activationType,
            String activatedBy
    ) {

        jdbc.update(
                """
                INSERT INTO attendance_monitor_sessions
                (
                    monitor_id,
                    activation_type,
                    activated_by
                )
                VALUES (?, ?, ?)
                """,
                monitorId,
                activationType,
                activatedBy
        );

        return jdbc.queryForObject(
                "SELECT CAST(SCOPE_IDENTITY() AS BIGINT)",
                Long.class
        );
    }

    public Long getOpenSessionId(Long monitorId) {

        List<Long> result =
                jdbc.query(
                        """
                        SELECT TOP 1 id
                        FROM attendance_monitor_sessions
                        WHERE monitor_id = ?
                          AND deactivated_at IS NULL
                        ORDER BY id DESC
                        """,
                        (rs, row) ->
                                rs.getLong("id"),
                        monitorId
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public void closeSession(
            Long sessionId,
            String deactivatedBy,
            String deactivationType
    ) {

        jdbc.update(
                """
                UPDATE attendance_monitor_sessions
                SET
                    deactivated_by = ?,
                    deactivated_at = SYSDATETIME(),
                    deactivation_type = ?
                WHERE id = ?
                  AND deactivated_at IS NULL
                """,
                deactivatedBy,
                deactivationType,
                sessionId
        );
    }

    public void logEvent(
            Long monitorId,
            Long attendanceRecordId,
            Long employeeId,
            String action,
            Integer qrSequence,
            String performedBy,
            String details
    ) {

        jdbc.update(
                """
                INSERT INTO attendance_events
                (
                    monitor_id,
                    attendance_record_id,
                    employee_id,
                    action,
                    event_time,
                    qr_sequence,
                    performed_by,
                    details
                )
                VALUES (?, ?, ?, ?, SYSDATETIME(), ?, ?, ?)
                """,
                monitorId,
                attendanceRecordId,
                employeeId,
                action,
                qrSequence,
                performedBy,
                details
        );
    }

    public List<AttendanceEvent> findEvents(int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 200));

        String sql =
                """
                SELECT TOP %d
                    ae.id,
                    ae.monitor_id,
                    ae.attendance_record_id,
                    ae.employee_id,
                    ae.action,
                    ae.event_time,
                    ae.qr_sequence,
                    ae.performed_by,
                    ae.details,
                    e.employee_number,
                    CONCAT(e.first_name, ' ', e.last_name) AS employee_name
                FROM attendance_events ae
                LEFT JOIN employees e
                    ON e.id = ae.employee_id
                ORDER BY ae.event_time DESC, ae.id DESC
                """.formatted(safeLimit);

        return jdbc.query(
                sql,
                this::mapEvent
        );
    }

    private AttendanceMonitor mapMonitor(
            java.sql.ResultSet rs,
            int row
    ) throws java.sql.SQLException {

        AttendanceMonitor monitor =
                new AttendanceMonitor();

        monitor.setId(
                rs.getLong("id")
        );

        monitor.setActivationCode(
                rs.getString("activation_code")
        );

        monitor.setActive(
                rs.getBoolean("active")
        );

        monitor.setActivationType(
                rs.getString("activation_type")
        );

        monitor.setActivatedBy(
                rs.getString("activated_by")
        );

        Timestamp activated =
                rs.getTimestamp("activated_at");

        Timestamp deactivated =
                rs.getTimestamp("deactivated_at");

        Timestamp created =
                rs.getTimestamp("qr_created_at");

        Timestamp expires =
                rs.getTimestamp("qr_expires_at");

        if (activated != null) {
            monitor.setActivatedAt(
                    activated.toLocalDateTime()
            );
        }

        if (deactivated != null) {
            monitor.setDeactivatedAt(
                    deactivated.toLocalDateTime()
            );
        }

        monitor.setCurrentQrToken(
                rs.getString("current_qr_token")
        );

        monitor.setQrSequence(
                rs.getInt("qr_sequence")
        );

        if (created != null) {
            monitor.setQrCreatedAt(
                    created.toLocalDateTime()
            );
        }

        if (expires != null) {
            monitor.setQrExpiresAt(
                    expires.toLocalDateTime()
            );
        }

        return monitor;
    }

    private AttendanceEvent mapEvent(
            java.sql.ResultSet rs,
            int row
    ) throws java.sql.SQLException {

        AttendanceEvent event =
                new AttendanceEvent();

        event.setId(
                rs.getLong("id")
        );

        long monitorId =
                rs.getLong("monitor_id");

        if (!rs.wasNull()) {
            event.setMonitorId(monitorId);
        }

        long recordId =
                rs.getLong("attendance_record_id");

        if (!rs.wasNull()) {
            event.setAttendanceRecordId(recordId);
        }

        long employeeId =
                rs.getLong("employee_id");

        if (!rs.wasNull()) {
            event.setEmployeeId(employeeId);
        }

        event.setAction(
                rs.getString("action")
        );

        Timestamp eventTime =
                rs.getTimestamp("event_time");

        if (eventTime != null) {
            event.setEventTime(
                    eventTime.toLocalDateTime()
            );
        }

        int sequence =
                rs.getInt("qr_sequence");

        if (!rs.wasNull()) {
            event.setQrSequence(sequence);
        }

        event.setPerformedBy(
                rs.getString("performed_by")
        );

        event.setDetails(
                rs.getString("details")
        );

        event.setEmployeeNumber(
                rs.getString("employee_number")
        );

        event.setEmployeeName(
                rs.getString("employee_name")
        );

        return event;
    }
}