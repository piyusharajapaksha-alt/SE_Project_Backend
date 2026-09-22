package com.staffhub.repository;

import com.staffhub.model.AttendanceMonitor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class AttendanceMonitorRepository {

    private final JdbcTemplate jdbc;

    public AttendanceMonitorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AttendanceMonitor getMonitor() {

        List<AttendanceMonitor> list = jdbc.query(
                """
                SELECT TOP 1 *
                FROM attendance_monitor
                ORDER BY id DESC
                """,
                this::map
        );

        return list.isEmpty() ? null : list.get(0);
    }

    public AttendanceMonitor getActiveMonitor() {

        List<AttendanceMonitor> list = jdbc.query(
                """
                SELECT TOP 1 *
                FROM attendance_monitor
                WHERE active = 1
                ORDER BY id DESC
                """,
                this::map
        );

        return list.isEmpty() ? null : list.get(0);
    }

    public Long create(String code) {

        jdbc.update(
                """
                INSERT INTO attendance_monitor
                (
                    activation_code,
                    active,
                    activation_type
                )
                VALUES (?, 0, 'MANUAL')
                """,
                code
        );

        return jdbc.queryForObject(
                "SELECT CAST(SCOPE_IDENTITY() AS BIGINT)",
                Long.class
        );
    }

    public void activate(
            Long id,
            String activatedBy,
            String type,
            String token,
            int sequence,
            java.time.LocalDateTime created,
            java.time.LocalDateTime expires
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
                type,
                activatedBy,
                token,
                sequence,
                Timestamp.valueOf(created),
                Timestamp.valueOf(expires),
                id
        );
    }

    public void deactivate(Long id) {

        jdbc.update(
                """
                UPDATE attendance_monitor
                SET
                    active = 0,
                    deactivated_at = SYSDATETIME(),
                    current_qr_token = NULL,
                    qr_created_at = NULL,
                    qr_expires_at = NULL
                WHERE id = ?
                """,
                id
        );
    }

    public void rotate(
            Long id,
            String token,
            int sequence,
            java.time.LocalDateTime created,
            java.time.LocalDateTime expires
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

    public String getActivationCode() {

        List<String> codes = jdbc.query(
                """
                SELECT TOP 1 activation_code
                FROM attendance_monitor
                ORDER BY id DESC
                """,
                (rs, row) -> rs.getString(1)
        );

        return codes.isEmpty() ? null : codes.get(0);
    }

    private AttendanceMonitor map(
            java.sql.ResultSet rs,
            int row
    ) throws java.sql.SQLException {

        AttendanceMonitor m = new AttendanceMonitor();

        m.setId(rs.getLong("id"));
        m.setActivationCode(rs.getString("activation_code"));
        m.setActive(rs.getBoolean("active"));
        m.setActivationType(rs.getString("activation_type"));
        m.setActivatedBy(rs.getString("activated_by"));

        Timestamp activated = rs.getTimestamp("activated_at");
        Timestamp deactivated = rs.getTimestamp("deactivated_at");
        Timestamp created = rs.getTimestamp("qr_created_at");
        Timestamp expires = rs.getTimestamp("qr_expires_at");

        if (activated != null)
            m.setActivatedAt(activated.toLocalDateTime());

        if (deactivated != null)
            m.setDeactivatedAt(deactivated.toLocalDateTime());

        m.setCurrentQrToken(rs.getString("current_qr_token"));
        m.setQrSequence(rs.getInt("qr_sequence"));

        if (created != null)
            m.setQrCreatedAt(created.toLocalDateTime());

        if (expires != null)
            m.setQrExpiresAt(expires.toLocalDateTime());

        return m;
    }
}