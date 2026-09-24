package com.staffhub.repository;

import com.staffhub.model.AttendanceSchedule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public class AttendanceScheduleRepository {

    private final JdbcTemplate jdbc;

    public AttendanceScheduleRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public List<AttendanceSchedule> findAll() {

        return jdbc.query(
                """
                SELECT *
                FROM attendance_schedules
                ORDER BY start_time
                """,
                this::map
        );
    }

    public AttendanceSchedule findById(
            Long id
    ) {

        List<AttendanceSchedule> list =
                jdbc.query(
                        """
                        SELECT *
                        FROM attendance_schedules
                        WHERE id = ?
                        """,
                        this::map,
                        id
                );

        return list.isEmpty()
                ? null
                : list.get(0);
    }

    public void create(
            AttendanceSchedule s
    ) {

        jdbc.update(
                """
                INSERT INTO attendance_schedules
                (
                    schedule_name,
                    schedule_type,
                    schedule_date,
                    day_of_week,
                    start_time,
                    end_time,
                    enabled,
                    created_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                s.getScheduleName(),
                s.getScheduleType(),
                s.getScheduleDate() == null
                        ? null
                        : Date.valueOf(
                                s.getScheduleDate()
                        ),
                s.getDayOfWeek(),
                Time.valueOf(
                        s.getStartTime()
                ),
                Time.valueOf(
                        s.getEndTime()
                ),
                s.isEnabled(),
                s.getCreatedBy()
        );
    }

    public void update(
            Long id,
            AttendanceSchedule s
    ) {

        jdbc.update(
                """
                UPDATE attendance_schedules
                SET
                    schedule_name = ?,
                    schedule_type = ?,
                    schedule_date = ?,
                    day_of_week = ?,
                    start_time = ?,
                    end_time = ?,
                    enabled = ?,
                    updated_at = SYSDATETIME()
                WHERE id = ?
                """,
                s.getScheduleName(),
                s.getScheduleType(),
                s.getScheduleDate() == null
                        ? null
                        : Date.valueOf(
                                s.getScheduleDate()
                        ),
                s.getDayOfWeek(),
                Time.valueOf(
                        s.getStartTime()
                ),
                Time.valueOf(
                        s.getEndTime()
                ),
                s.isEnabled(),
                id
        );
    }

    public void delete(Long id) {

        jdbc.update(
                """
                DELETE FROM attendance_schedules
                WHERE id = ?
                """,
                id
        );
    }

    private AttendanceSchedule map(
            java.sql.ResultSet rs,
            int row
    ) throws java.sql.SQLException {

        AttendanceSchedule s =
                new AttendanceSchedule();

        s.setId(
                rs.getLong("id")
        );

        s.setScheduleName(
                rs.getString("schedule_name")
        );

        s.setScheduleType(
                rs.getString("schedule_type")
        );

        Date date =
                rs.getDate("schedule_date");

        if (date != null) {
            s.setScheduleDate(
                    date.toLocalDate()
            );
        }

        s.setDayOfWeek(
                rs.getString("day_of_week")
        );

        Time start =
                rs.getTime("start_time");

        Time end =
                rs.getTime("end_time");

        if (start != null) {
            s.setStartTime(
                    start.toLocalTime()
            );
        }

        if (end != null) {
            s.setEndTime(
                    end.toLocalTime()
            );
        }

        s.setEnabled(
                rs.getBoolean("enabled")
        );

        s.setCreatedBy(
                rs.getString("created_by")
        );

        Timestamp created =
                rs.getTimestamp("created_at");

        Timestamp updated =
                rs.getTimestamp("updated_at");

        if (created != null) {
            s.setCreatedAt(
                    created.toLocalDateTime()
            );
        }

        if (updated != null) {
            s.setUpdatedAt(
                    updated.toLocalDateTime()
            );
        }

        return s;
    }
}