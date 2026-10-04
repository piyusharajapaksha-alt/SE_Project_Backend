package com.staffhub.repository;

import com.staffhub.model.AttendanceSchedule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class AttendanceScheduleRepository {

    private final JdbcTemplate jdbc;

    public AttendanceScheduleRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public List<AttendanceSchedule> findAll(
            Long companyId
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    schedule_name,
                    schedule_type,
                    schedule_date,
                    day_of_week,
                    start_time,
                    end_time,
                    enabled,
                    created_by,
                    created_at,
                    updated_at
                FROM dbo.attendance_schedules
                WHERE company_id = ?
                ORDER BY start_time, id
                """,
                this::map,
                companyId
        );
    }

    public AttendanceSchedule findById(
            Long id,
            Long companyId
    ) {

        List<AttendanceSchedule> result =
                jdbc.query(
                        """
                        SELECT
                            id,
                            schedule_name,
                            schedule_type,
                            schedule_date,
                            day_of_week,
                            start_time,
                            end_time,
                            enabled,
                            created_by,
                            created_at,
                            updated_at
                        FROM dbo.attendance_schedules
                        WHERE id = ?
                          AND company_id = ?
                        """,
                        this::map,
                        id,
                        companyId
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public AttendanceSchedule findLatest(
            Long companyId
    ) {

        List<AttendanceSchedule> result =
                jdbc.query(
                        """
                        SELECT TOP 1
                            id,
                            schedule_name,
                            schedule_type,
                            schedule_date,
                            day_of_week,
                            start_time,
                            end_time,
                            enabled,
                            created_by,
                            created_at,
                            updated_at
                        FROM dbo.attendance_schedules
                        WHERE company_id = ?
                        ORDER BY id DESC
                        """,
                        this::map,
                        companyId
                );

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public void create(
            AttendanceSchedule schedule,
            Long companyId
    ) {

        validate(schedule);

        jdbc.update(
                """
                INSERT INTO dbo.attendance_schedules
                (
                    company_id,
                    schedule_name,
                    schedule_type,
                    schedule_date,
                    day_of_week,
                    start_time,
                    end_time,
                    enabled,
                    created_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                companyId,
                schedule.getScheduleName(),
                schedule.getScheduleType(),
                schedule.getScheduleDate() == null
                        ? null
                        : Date.valueOf(
                                schedule.getScheduleDate()
                        ),
                normalizeDay(
                        schedule.getDayOfWeek()
                ),
                Time.valueOf(
                        schedule.getStartTime()
                ),
                Time.valueOf(
                        schedule.getEndTime()
                ),
                schedule.isEnabled(),
                schedule.getCreatedBy()
        );
    }

    public void update(
            Long id,
            AttendanceSchedule schedule,
            Long companyId
    ) {

        if (
                findById(
                        id,
                        companyId
                ) == null
        ) {
            throw new IllegalArgumentException(
                    "Attendance schedule not found"
            );
        }

        validate(schedule);

        jdbc.update(
                """
                UPDATE dbo.attendance_schedules
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
                  AND company_id = ?
                """,
                schedule.getScheduleName(),
                schedule.getScheduleType(),
                schedule.getScheduleDate() == null
                        ? null
                        : Date.valueOf(
                                schedule.getScheduleDate()
                        ),
                normalizeDay(
                        schedule.getDayOfWeek()
                ),
                Time.valueOf(
                        schedule.getStartTime()
                ),
                Time.valueOf(
                        schedule.getEndTime()
                ),
                schedule.isEnabled(),
                id,
                companyId
        );
    }

    public void delete(
            Long id,
            Long companyId
    ) {

        int result =
                jdbc.update(
                        """
                        DELETE FROM dbo.attendance_schedules
                        WHERE id = ?
                          AND company_id = ?
                        """,
                        id,
                        companyId
                );

        if (result == 0) {
            throw new IllegalArgumentException(
                    "Attendance schedule not found"
            );
        }
    }

    private void validate(
            AttendanceSchedule schedule
    ) {

        if (
                schedule.getScheduleName() == null
                        || schedule.getScheduleName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Schedule name is required"
            );
        }

        if (
                schedule.getScheduleType() == null
                        || schedule.getScheduleType().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Schedule type is required"
            );
        }

        String type =
                schedule.getScheduleType()
                        .trim()
                        .toUpperCase();

        if (
                !type.equals("ONCE")
                        && !type.equals("DAILY")
                        && !type.equals("WEEKLY")
        ) {
            throw new IllegalArgumentException(
                    "Schedule type must be ONCE, DAILY or WEEKLY"
            );
        }

        if (
                schedule.getStartTime() == null
                        || schedule.getEndTime() == null
        ) {
            throw new IllegalArgumentException(
                    "Start and end time are required"
            );
        }

        if (
                !schedule.getEndTime()
                        .isAfter(
                                schedule.getStartTime()
                        )
        ) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        if (
                type.equals("ONCE")
                        && schedule.getScheduleDate() == null
        ) {
            throw new IllegalArgumentException(
                    "Schedule date is required"
            );
        }

        if (
                type.equals("WEEKLY")
                        && normalizeDay(
                        schedule.getDayOfWeek()
                ) == null
        ) {
            throw new IllegalArgumentException(
                    "Day of week is required"
            );
        }
    }

    private String normalizeDay(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        String day =
                value.trim()
                        .toUpperCase();

        return switch (day) {
            case "1", "MONDAY" ->
                    "MONDAY";
            case "2", "TUESDAY" ->
                    "TUESDAY";
            case "3", "WEDNESDAY" ->
                    "WEDNESDAY";
            case "4", "THURSDAY" ->
                    "THURSDAY";
            case "5", "FRIDAY" ->
                    "FRIDAY";
            case "6", "SATURDAY" ->
                    "SATURDAY";
            case "7", "SUNDAY" ->
                    "SUNDAY";
            default ->
                    throw new IllegalArgumentException(
                            "Invalid day of week"
                    );
        };
    }

    private AttendanceSchedule map(
            java.sql.ResultSet rs,
            int row
    ) throws java.sql.SQLException {

        AttendanceSchedule schedule =
                new AttendanceSchedule();

        schedule.setId(
                rs.getLong("id")
        );

        schedule.setScheduleName(
                rs.getString("schedule_name")
        );

        schedule.setScheduleType(
                rs.getString("schedule_type")
        );

        Date date =
                rs.getDate("schedule_date");

        if (date != null) {
            schedule.setScheduleDate(
                    date.toLocalDate()
            );
        }

        schedule.setDayOfWeek(
                rs.getString("day_of_week")
        );

        Time start =
                rs.getTime("start_time");

        Time end =
                rs.getTime("end_time");

        if (start != null) {
            schedule.setStartTime(
                    start.toLocalTime()
            );
        }

        if (end != null) {
            schedule.setEndTime(
                    end.toLocalTime()
            );
        }

        schedule.setEnabled(
                rs.getBoolean("enabled")
        );

        schedule.setCreatedBy(
                rs.getString("created_by")
        );

        Timestamp created =
                rs.getTimestamp("created_at");

        Timestamp updated =
                rs.getTimestamp("updated_at");

        if (created != null) {
            schedule.setCreatedAt(
                    created.toLocalDateTime()
            );
        }

        if (updated != null) {
            schedule.setUpdatedAt(
                    updated.toLocalDateTime()
            );
        }

        return schedule;
    }
}