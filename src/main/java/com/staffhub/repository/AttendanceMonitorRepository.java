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

    /*
     * ============================================================
     * PUBLIC MONITOR
     *
     * Used by /qrmonitor.
     *
     * monitorId identifies the physical/browser monitor.
     * ============================================================
     */

    public AttendanceMonitor findById(Long monitorId) {

        if (monitorId == null) {
            return null;
        }

        List<AttendanceMonitor> result =
                jdbc.query(
                        """
                        SELECT
                            m.*,
                            c.company_name
                        FROM dbo.attendance_monitor m
                        LEFT JOIN dbo.companies c
                            ON c.id = m.company_id
                        WHERE m.id = ?
                        """,
                        this::mapMonitor,
                        monitorId
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    /*
     * Find an unassigned monitor by its OTP.
     */
    public AttendanceMonitor findUnassignedByCode(
            String activationCode
    ) {

        List<AttendanceMonitor> result =
                jdbc.query(
                        """
                        SELECT
                            m.*,
                            c.company_name
                        FROM dbo.attendance_monitor m
                        LEFT JOIN dbo.companies c
                            ON c.id = m.company_id
                        WHERE m.activation_code = ?
                          AND m.company_id IS NULL
                          AND m.authorized = 0
                        ORDER BY m.id DESC
                        """,
                        this::mapMonitor,
                        activationCode
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    /*
     * Create a completely new physical monitor.
     */
    public Long create(
            String activationCode
    ) {

        jdbc.update(
                """
                INSERT INTO dbo.attendance_monitor
                (
                    company_id,
                    authorized,
                    activation_code,
                    active,
                    activation_type,
                    qr_sequence
                )
                VALUES
                (
                    NULL,
                    0,
                    ?,
                    0,
                    'MANUAL',
                    0
                )
                """,
                activationCode
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

    /*
     * Accept monitor permanently for a company.
     */
    public void authorize(
            Long monitorId,
            Long companyId,
            String authorizedBy
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE dbo.attendance_monitor
                        SET
                            company_id = ?,
                            authorized = 1,
                            authorized_by = ?,
                            authorized_at = SYSDATETIME()
                        WHERE id = ?
                          AND company_id IS NULL
                          AND authorized = 0
                        """,
                        companyId,
                        authorizedBy,
                        monitorId
                );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Monitor is already accepted or does not exist"
            );
        }
    }

    /*
     * Reject/unassign monitor.
     *
     * It cannot be rejected by another company.
     */
    public void reject(
            Long monitorId,
            Long companyId
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE dbo.attendance_monitor
                        SET
                            company_id = NULL,
                            authorized = 0,
                            authorized_by = NULL,
                            authorized_at = NULL,
                            active = 0,
                            activation_type = 'MANUAL',
                            activated_by = NULL,
                            activated_at = NULL,
                            deactivated_at = SYSDATETIME(),
                            current_qr_token = NULL,
                            qr_sequence = 0,
                            qr_created_at = NULL,
                            qr_expires_at = NULL,
                            activation_code = ?
                        WHERE id = ?
                          AND company_id = ?
                          AND authorized = 1
                        """,
                        generateActivationCode(),
                        monitorId,
                        companyId
                );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Monitor does not belong to your company"
            );
        }
    }

    /*
     * All accepted monitors for current company.
     */
    public List<AttendanceMonitor> findAcceptedByCompany(
            Long companyId
    ) {

        return jdbc.query(
                """
                SELECT
                    m.*,
                    c.company_name
                FROM dbo.attendance_monitor m
                INNER JOIN dbo.companies c
                    ON c.id = m.company_id
                WHERE m.company_id = ?
                  AND m.authorized = 1
                ORDER BY m.id
                """,
                this::mapMonitor,
                companyId
        );
    }

    /*
     * Active monitor for one company only.
     */
    public AttendanceMonitor findActiveByCompany(
            Long companyId
    ) {

        List<AttendanceMonitor> result =
                jdbc.query(
                        """
                        SELECT
                            m.*,
                            c.company_name
                        FROM dbo.attendance_monitor m
                        INNER JOIN dbo.companies c
                            ON c.id = m.company_id
                        WHERE m.company_id = ?
                          AND m.authorized = 1
                          AND m.active = 1
                        ORDER BY m.id DESC
                        """,
                        this::mapMonitor,
                        companyId
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    /*
     * Specific monitor belonging to company.
     */
    public AttendanceMonitor findByCompany(
            Long monitorId,
            Long companyId
    ) {

        List<AttendanceMonitor> result =
                jdbc.query(
                        """
                        SELECT
                            m.*,
                            c.company_name
                        FROM dbo.attendance_monitor m
                        INNER JOIN dbo.companies c
                            ON c.id = m.company_id
                        WHERE m.id = ?
                          AND m.company_id = ?
                          AND m.authorized = 1
                        """,
                        this::mapMonitor,
                        monitorId,
                        companyId
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public void activate(
            Long monitorId,
            Long companyId,
            String activatedBy,
            String activationType,
            String token,
            int sequence,
            LocalDateTime created,
            LocalDateTime expires
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE dbo.attendance_monitor
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
                          AND company_id = ?
                          AND authorized = 1
                        """,
                        activationType,
                        activatedBy,
                        token,
                        sequence,
                        Timestamp.valueOf(created),
                        Timestamp.valueOf(expires),
                        monitorId,
                        companyId
                );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Monitor does not belong to your company"
            );
        }
    }

    public void deactivate(
            Long monitorId,
            Long companyId,
            String newActivationCode
    ) {

        jdbc.update(
                """
                UPDATE dbo.attendance_monitor
                SET
                    active = 0,
                    deactivated_at = SYSDATETIME(),
                    current_qr_token = NULL,
                    qr_created_at = NULL,
                    qr_expires_at = NULL,
                    activation_code = ?
                WHERE id = ?
                  AND company_id = ?
                  AND authorized = 1
                """,
                newActivationCode,
                monitorId,
                companyId
        );
    }

    public void rotate(
            Long monitorId,
            Long companyId,
            String token,
            int sequence,
            LocalDateTime created,
            LocalDateTime expires
    ) {

        int updated =
                jdbc.update(
                        """
                        UPDATE dbo.attendance_monitor
                        SET
                            current_qr_token = ?,
                            qr_sequence = ?,
                            qr_created_at = ?,
                            qr_expires_at = ?
                        WHERE id = ?
                          AND company_id = ?
                          AND authorized = 1
                          AND active = 1
                        """,
                        token,
                        sequence,
                        Timestamp.valueOf(created),
                        Timestamp.valueOf(expires),
                        monitorId,
                        companyId
                );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Monitor is not active for this company"
            );
        }
    }

    public Long createSession(
            Long monitorId,
            Long companyId,
            String activationType,
            String activatedBy
    ) {

        jdbc.update(
                """
                INSERT INTO dbo.attendance_monitor_sessions
                (
                    monitor_id,
                    company_id,
                    activation_type,
                    activated_by
                )
                VALUES (?, ?, ?, ?)
                """,
                monitorId,
                companyId,
                activationType,
                activatedBy
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

    public Long getOpenSessionId(
            Long monitorId,
            Long companyId
    ) {

        List<Long> result =
                jdbc.query(
                        """
                        SELECT TOP 1 id
                        FROM dbo.attendance_monitor_sessions
                        WHERE monitor_id = ?
                          AND company_id = ?
                          AND deactivated_at IS NULL
                        ORDER BY id DESC
                        """,
                        (rs, row) ->
                                rs.getLong("id"),
                        monitorId,
                        companyId
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public void closeSession(
            Long sessionId,
            Long companyId,
            String deactivatedBy,
            String deactivationType
    ) {

        jdbc.update(
                """
                UPDATE dbo.attendance_monitor_sessions
                SET
                    deactivated_by = ?,
                    deactivated_at = SYSDATETIME(),
                    deactivation_type = ?
                WHERE id = ?
                  AND company_id = ?
                  AND deactivated_at IS NULL
                """,
                deactivatedBy,
                deactivationType,
                sessionId,
                companyId
        );
    }

    public void logEvent(
            Long monitorId,
            Long companyId,
            Long attendanceRecordId,
            Long employeeId,
            String action,
            Integer qrSequence,
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
                    ?, ?, ?, ?, ?,
                    SYSDATETIME(),
                    ?, ?, ?
                )
                """,
                companyId,
                monitorId,
                attendanceRecordId,
                employeeId,
                action,
                qrSequence,
                performedBy,
                details
        );
    }

    public List<AttendanceEvent> findEvents(
            Long companyId,
            int limit
    ) {

        int safeLimit =
                Math.max(
                        1,
                        Math.min(limit, 200)
                );

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
                    CONCAT(
                        e.first_name,
                        ' ',
                        e.last_name
                    ) AS employee_name
                FROM dbo.attendance_events ae
                LEFT JOIN dbo.employees e
                    ON e.id = ae.employee_id
                WHERE ae.company_id = ?
                ORDER BY
                    ae.event_time DESC,
                    ae.id DESC
                """.formatted(safeLimit);

        return jdbc.query(
                sql,
                this::mapEvent,
                companyId
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

        long companyId =
                rs.getLong("company_id");

        if (!rs.wasNull()) {
            monitor.setCompanyId(companyId);
        }

        monitor.setCompanyName(
                rs.getString("company_name")
        );

        monitor.setAuthorized(
                rs.getBoolean("authorized")
        );

        monitor.setAuthorizedBy(
                rs.getString("authorized_by")
        );

        Timestamp authorizedAt =
                rs.getTimestamp("authorized_at");

        if (authorizedAt != null) {
            monitor.setAuthorizedAt(
                    authorizedAt.toLocalDateTime()
            );
        }

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

        Timestamp created =
                rs.getTimestamp("qr_created_at");

        Timestamp expires =
                rs.getTimestamp("qr_expires_at");

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

        event.setId(rs.getLong("id"));

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

    private String generateActivationCode() {

        return String.format(
                "%06d",
                new java.security.SecureRandom()
                        .nextInt(1_000_000)
        );
    }
}