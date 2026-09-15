package com.staffhub.repository;

import com.staffhub.model.Event;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EventRepository {

    private final JdbcTemplate jdbcTemplate;

    public EventRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    // FIND ALL EVENTS
    // ============================================================

    public List<Event> findAll(
            String search,
            String category,
            String status,
            String employeeId
    ) {

        StringBuilder sql =
                new StringBuilder("""
                    SELECT
                        e.id,
                        e.title,
                        e.description,
                        e.organizer_id,
                        CONCAT(
                            emp.first_name,
                            ' ',
                            emp.last_name
                        ) AS organizer,
                        e.category,
                        e.event_date,
                        e.start_time,
                        e.end_time,
                        e.location,
                        e.capacity,
                        e.status,
                        COUNT(er.id) AS registered_count
                    FROM events e
                    JOIN employees emp
                        ON emp.employee_number =
                           e.organizer_id
                    LEFT JOIN event_registrations er
                        ON er.event_id = e.id
                    WHERE 1 = 1
                    """);

        List<Object> params =
                new ArrayList<>();

        // --------------------------------------------------------
        // Search
        // --------------------------------------------------------

        if (
                search != null
                        && !search.isBlank()
        ) {

            sql.append("""
                AND (
                    LOWER(e.title)
                        LIKE LOWER(?)

                    OR LOWER(
                        COALESCE(e.description, '')
                    )
                        LIKE LOWER(?)

                    OR LOWER(e.location)
                        LIKE LOWER(?)

                    OR LOWER(e.category)
                        LIKE LOWER(?)
                )
                """);

            String value =
                    "%" + search.trim() + "%";

            params.add(value);
            params.add(value);
            params.add(value);
            params.add(value);
        }

        // --------------------------------------------------------
        // Category
        // --------------------------------------------------------

        if (
                category != null
                        && !category.isBlank()
                        && !category.equalsIgnoreCase("All")
        ) {

            sql.append(
                    " AND e.category = ? "
            );

            params.add(
                    category.trim()
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
                    " AND e.status = ? "
            );

            params.add(
                    status.trim()
            );
        }

        // --------------------------------------------------------
        // GROUP / ORDER
        // --------------------------------------------------------

        sql.append("""
            GROUP BY
                e.id,
                e.title,
                e.description,
                e.organizer_id,
                emp.first_name,
                emp.last_name,
                e.category,
                e.event_date,
                e.start_time,
                e.end_time,
                e.location,
                e.capacity,
                e.status
            ORDER BY
                e.event_date ASC,
                e.start_time ASC,
                e.id ASC
            """);

        List<Event> events =
                jdbcTemplate.query(
                        sql.toString(),
                        params.toArray(),
                        this::mapEvent
                );

        for (Event event : events) {

            loadRegistrationData(
                    event,
                    employeeId
            );
        }

        return events;
    }

    // ============================================================
    // FIND ONE EVENT
    // ============================================================

    public Event findById(
            Long id,
            String employeeId
    ) {

        String sql = """
            SELECT
                e.id,
                e.title,
                e.description,
                e.organizer_id,
                CONCAT(
                    emp.first_name,
                    ' ',
                    emp.last_name
                ) AS organizer,
                e.category,
                e.event_date,
                e.start_time,
                e.end_time,
                e.location,
                e.capacity,
                e.status,
                COUNT(er.id) AS registered_count
            FROM events e
            JOIN employees emp
                ON emp.employee_number =
                   e.organizer_id
            LEFT JOIN event_registrations er
                ON er.event_id = e.id
            WHERE e.id = ?
            GROUP BY
                e.id,
                e.title,
                e.description,
                e.organizer_id,
                emp.first_name,
                emp.last_name,
                e.category,
                e.event_date,
                e.start_time,
                e.end_time,
                e.location,
                e.capacity,
                e.status
            """;

        try {

            Event event =
                    jdbcTemplate.queryForObject(
                            sql,
                            this::mapEvent,
                            id
                    );

            if (event == null) {

                throw new IllegalArgumentException(
                        "Event not found"
                );
            }

            loadRegistrationData(
                    event,
                    employeeId
            );

            return event;

        } catch (
                EmptyResultDataAccessException exception
        ) {

            throw new IllegalArgumentException(
                    "Event not found"
            );
        }
    }

    // ============================================================
    // CREATE
    // ============================================================

    public Event create(
            Event event
    ) {

        String sql = """
            INSERT INTO events (
                title,
                description,
                organizer_id,
                category,
                event_date,
                start_time,
                end_time,
                location,
                capacity,
                status
            )
            OUTPUT INSERTED.id
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Long id =
                jdbcTemplate.queryForObject(
                        sql,
                        Long.class,
                        event.getTitle(),
                        event.getDescription(),
                        event.getOrganizerId(),
                        event.getCategory(),
                        event.getDate(),
                        event.getTime(),
                        event.getEndTime(),
                        event.getLocation(),
                        event.getCapacity(),
                        event.getStatus()
                );

        if (id == null) {

            throw new IllegalStateException(
                    "Failed to create event"
            );
        }

        event.setId(id);

        return event;
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public Event update(
            Long id,
            Event event
    ) {

        Integer registeredCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM event_registrations
                        WHERE event_id = ?
                        """,
                        Integer.class,
                        id
                );

        if (registeredCount == null) {
            registeredCount = 0;
        }

        if (
                event.getCapacity() == null
                        || event.getCapacity()
                        < registeredCount
        ) {

            throw new IllegalArgumentException(
                    "Capacity cannot be lower than the current number of registrations"
            );
        }

        String sql = """
            UPDATE events
            SET
                title = ?,
                description = ?,
                organizer_id = ?,
                category = ?,
                event_date = ?,
                start_time = ?,
                end_time = ?,
                location = ?,
                capacity = ?,
                status = ?,
                updated_at = SYSDATETIME()
            WHERE id = ?
            """;

        int updated =
                jdbcTemplate.update(
                        sql,
                        event.getTitle(),
                        event.getDescription(),
                        event.getOrganizerId(),
                        event.getCategory(),
                        event.getDate(),
                        event.getTime(),
                        event.getEndTime(),
                        event.getLocation(),
                        event.getCapacity(),
                        event.getStatus(),
                        id
                );

        if (updated == 0) {

            throw new IllegalArgumentException(
                    "Event not found"
            );
        }

        event.setId(id);

        return event;
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void delete(
            Long id
    ) {

        int deleted =
                jdbcTemplate.update(
                        """
                        DELETE FROM events
                        WHERE id = ?
                        """,
                        id
                );

        if (deleted == 0) {

            throw new IllegalArgumentException(
                    "Event not found"
            );
        }
    }

    // ============================================================
    // REGISTER
    // ============================================================

    public void register(
            Long eventId,
            String employeeId
    ) {

        Event event =
                findById(
                        eventId,
                        employeeId
                );

        if (
                "Cancelled".equals(
                        event.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Cannot register for a cancelled event"
            );
        }

        if (
                !"Upcoming".equals(
                        event.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Registration is only available for upcoming events"
            );
        }

        if (
                event.getAvailableSeats() == null
                        || event.getAvailableSeats() <= 0
        ) {

            throw new IllegalArgumentException(
                    "This event is full"
            );
        }

        Integer existing =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM event_registrations
                        WHERE event_id = ?
                          AND employee_id = ?
                        """,
                        Integer.class,
                        eventId,
                        employeeId
                );

        if (
                existing != null
                        && existing > 0
        ) {

            throw new IllegalArgumentException(
                    "Employee is already registered for this event"
            );
        }

        jdbcTemplate.update(
                """
                INSERT INTO event_registrations (
                    event_id,
                    employee_id
                )
                VALUES (?, ?)
                """,
                eventId,
                employeeId
        );
    }

    // ============================================================
    // UNREGISTER
    // ============================================================

    public void unregister(
            Long eventId,
            String employeeId
    ) {

        int deleted =
                jdbcTemplate.update(
                        """
                        DELETE FROM event_registrations
                        WHERE event_id = ?
                          AND employee_id = ?
                        """,
                        eventId,
                        employeeId
                );

        if (deleted == 0) {

            throw new IllegalArgumentException(
                    "Registration not found"
            );
        }
    }

    // ============================================================
    // REGISTRATION DATA
    // ============================================================

    private void loadRegistrationData(
            Event event,
            String employeeId
    ) {

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM event_registrations
                        WHERE event_id = ?
                        """,
                        Integer.class,
                        event.getId()
                );

        int registeredCount =
                count == null ? 0 : count;

        event.setRegisteredCount(
                registeredCount
        );

        event.setAvailableSeats(
                Math.max(
                        event.getCapacity()
                                - registeredCount,
                        0
                )
        );

        List<String> registeredIds =
                jdbcTemplate.query(
                        """
                        SELECT employee_id
                        FROM event_registrations
                        WHERE event_id = ?
                        ORDER BY registered_at ASC
                        """,
                        (rs, rowNum) ->
                                rs.getString(
                                        "employee_id"
                                ),
                        event.getId()
                );

        event.setRegisteredIds(
                registeredIds
        );

        List<Event.Registrant> registrants =
                jdbcTemplate.query(
                        """
                        SELECT
                            e.employee_number,
                            CONCAT(
                                e.first_name,
                                ' ',
                                e.last_name
                            ) AS employee_name,
                            e.department
                        FROM event_registrations er
                        JOIN employees e
                            ON e.employee_number =
                               er.employee_id
                        WHERE er.event_id = ?
                        ORDER BY er.registered_at ASC
                        """,
                        (rs, rowNum) ->
                                new Event.Registrant(
                                        rs.getString(
                                                "employee_number"
                                        ),
                                        rs.getString(
                                                "employee_name"
                                        ),
                                        rs.getString(
                                                "department"
                                        )
                                ),
                        event.getId()
                );

        event.setRegistrantNames(
                registrants
        );
    }

    // ============================================================
    // MAPPER
    // ============================================================

    private Event mapEvent(
            ResultSet rs,
            int rowNumber
    ) throws java.sql.SQLException {

        Event event =
                new Event();

        event.setId(
                rs.getLong("id")
        );

        event.setTitle(
                rs.getString("title")
        );

        event.setDescription(
                rs.getString("description")
        );

        event.setOrganizerId(
                rs.getString("organizer_id")
        );

        event.setOrganizer(
                rs.getString("organizer")
        );

        event.setCategory(
                rs.getString("category")
        );

        if (
                rs.getDate("event_date")
                        != null
        ) {

            event.setDate(
                    rs.getDate(
                            "event_date"
                    ).toLocalDate()
            );
        }

        if (
                rs.getTime("start_time")
                        != null
        ) {

            event.setTime(
                    rs.getTime(
                            "start_time"
                    ).toLocalTime()
            );
        }

        if (
                rs.getTime("end_time")
                        != null
        ) {

            event.setEndTime(
                    rs.getTime(
                            "end_time"
                    ).toLocalTime()
            );
        }

        event.setLocation(
                rs.getString("location")
        );

        event.setCapacity(
                rs.getInt("capacity")
        );

        event.setStatus(
                rs.getString("status")
        );

        event.setRegisteredCount(
                rs.getInt("registered_count")
        );

        event.setAvailableSeats(
                Math.max(
                        event.getCapacity()
                                - event.getRegisteredCount(),
                        0
                )
        );

        return event;
    }
}