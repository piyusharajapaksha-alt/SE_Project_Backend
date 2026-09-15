package com.staffhub.repository;

import com.staffhub.model.Grievance;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GrievanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public GrievanceRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    // FIND ALL
    // ============================================================

    public List<Grievance> findAll(
            String employeeId,
            String search,
            String status,
            String priority,
            String category
    ) {

        StringBuilder sql =
                new StringBuilder("""
                    SELECT
                        g.id,
                        g.employee_id,
                        CONCAT(
                            e.first_name,
                            ' ',
                            e.last_name
                        ) AS employee_name,
                        g.category,
                        g.priority,
                        g.description,
                        g.status,
                        g.assigned_to,
                        CASE
                            WHEN a.id IS NOT NULL
                            THEN CONCAT(
                                a.first_name,
                                ' ',
                                a.last_name
                            )
                            ELSE NULL
                        END AS assigned_to_name,
                        g.created_at
                    FROM grievances g
                    JOIN employees e
                        ON e.employee_number =
                           g.employee_id
                    LEFT JOIN employees a
                        ON a.employee_number =
                           g.assigned_to
                    WHERE 1 = 1
                    """);

        List<Object> params =
                new ArrayList<>();

        if (
                employeeId != null
                        && !employeeId.isBlank()
        ) {

            sql.append(
                    " AND g.employee_id = ?"
            );

            params.add(employeeId);
        }

        if (
                search != null
                        && !search.isBlank()
        ) {

            sql.append("""
                AND (
                    LOWER(g.description)
                        LIKE LOWER(?)

                    OR LOWER(g.category)
                        LIKE LOWER(?)

                    OR LOWER(e.first_name)
                        LIKE LOWER(?)

                    OR LOWER(e.last_name)
                        LIKE LOWER(?)
                )
                """);

            String searchValue =
                    "%" + search + "%";

            params.add(searchValue);
            params.add(searchValue);
            params.add(searchValue);
            params.add(searchValue);
        }

        if (
                status != null
                        && !status.isBlank()
        ) {

            sql.append(
                    " AND g.status = ?"
            );

            params.add(status);
        }

        if (
                priority != null
                        && !priority.isBlank()
        ) {

            sql.append(
                    " AND g.priority = ?"
            );

            params.add(priority);
        }

        if (
                category != null
                        && !category.isBlank()
        ) {

            sql.append(
                    " AND g.category = ?"
            );

            params.add(category);
        }

        sql.append(
                " ORDER BY g.created_at DESC"
        );

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> {

                    Grievance grievance =
                            new Grievance();

                    grievance.setId(
                            rs.getLong("id")
                    );

                    grievance.setEmployeeId(
                            rs.getString(
                                    "employee_id"
                            )
                    );

                    grievance.setEmployeeName(
                            rs.getString(
                                    "employee_name"
                            )
                    );

                    grievance.setCategory(
                            rs.getString(
                                    "category"
                            )
                    );

                    grievance.setPriority(
                            rs.getString(
                                    "priority"
                            )
                    );

                    grievance.setDescription(
                            rs.getString(
                                    "description"
                            )
                    );

                    grievance.setStatus(
                            rs.getString(
                                    "status"
                            )
                    );

                    grievance.setAssignedTo(
                            rs.getString(
                                    "assigned_to"
                            )
                    );

                    grievance.setAssignedToName(
                            rs.getString(
                                    "assigned_to_name"
                            )
                    );

                    Timestamp createdAt =
                            rs.getTimestamp(
                                    "created_at"
                            );

                    if (createdAt != null) {
                        grievance.setCreatedAt(
                                createdAt
                                        .toLocalDateTime()
                        );
                    }

                    return grievance;
                },
                params.toArray()
        );
    }

    // ============================================================
    // FIND BY ID
    // ============================================================

    public Grievance findById(
            Long id
    ) {

        String sql = """
            SELECT
                g.id,
                g.employee_id,
                CONCAT(
                    e.first_name,
                    ' ',
                    e.last_name
                ) AS employee_name,
                g.category,
                g.priority,
                g.description,
                g.status,
                g.assigned_to,
                CASE
                    WHEN a.id IS NOT NULL
                    THEN CONCAT(
                        a.first_name,
                        ' ',
                        a.last_name
                    )
                    ELSE NULL
                END AS assigned_to_name,
                g.created_at
            FROM grievances g
            JOIN employees e
                ON e.employee_number =
                   g.employee_id
            LEFT JOIN employees a
                ON a.employee_number =
                   g.assigned_to
            WHERE g.id = ?
            """;

        try {

            Grievance grievance =
                    jdbcTemplate.queryForObject(
                            sql,
                            (rs, rowNum) -> {

                                Grievance result =
                                        new Grievance();

                                result.setId(
                                        rs.getLong(
                                                "id"
                                        )
                                );

                                result.setEmployeeId(
                                        rs.getString(
                                                "employee_id"
                                        )
                                );

                                result.setEmployeeName(
                                        rs.getString(
                                                "employee_name"
                                        )
                                );

                                result.setCategory(
                                        rs.getString(
                                                "category"
                                        )
                                );

                                result.setPriority(
                                        rs.getString(
                                                "priority"
                                        )
                                );

                                result.setDescription(
                                        rs.getString(
                                                "description"
                                        )
                                );

                                result.setStatus(
                                        rs.getString(
                                                "status"
                                        )
                                );

                                result.setAssignedTo(
                                        rs.getString(
                                                "assigned_to"
                                        )
                                );

                                result.setAssignedToName(
                                        rs.getString(
                                                "assigned_to_name"
                                        )
                                );

                                Timestamp createdAt =
                                        rs.getTimestamp(
                                                "created_at"
                                        );

                                if (createdAt != null) {
                                    result.setCreatedAt(
                                            createdAt
                                                    .toLocalDateTime()
                                    );
                                }

                                return result;
                            },
                            id
                    );

            grievance.setResponses(
                    findResponses(id)
            );

            return grievance;

        } catch (
                EmptyResultDataAccessException exception
        ) {

            throw new IllegalArgumentException(
                    "Grievance not found: " + id
            );
        }
    }

    // ============================================================
    // CREATE
    // ============================================================

    public Long save(
            Grievance grievance
    ) {

        String sql = """
            INSERT INTO grievances (
                employee_id,
                category,
                priority,
                description,
                status
            )
            VALUES (
                ?,
                ?,
                ?,
                ?,
                'New'
            )
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                grievance.getEmployeeId(),
                grievance.getCategory(),
                grievance.getPriority(),
                grievance.getDescription()
        );
    }

    // ============================================================
    // UPDATE OWN GRIEVANCE
    //
    // Only:
    // employee_id matches
    // AND status = New
    //
    // This prevents changing complaints after HR has started
    // processing them.
    // ============================================================

    public int updateOwnGrievance(
            Long id,
            String employeeId,
            String category,
            String priority,
            String description
    ) {

        String sql = """
            UPDATE grievances
            SET
                category = ?,
                priority = ?,
                description = ?
            WHERE
                id = ?
                AND employee_id = ?
                AND status = 'New'
            """;

        return jdbcTemplate.update(
                sql,
                category,
                priority,
                description,
                id,
                employeeId
        );
    }

    // ============================================================
    // DELETE OWN GRIEVANCE
    //
    // Only:
    // employee_id matches
    // AND status = New
    // ============================================================

    public int deleteOwnGrievance(
            Long id,
            String employeeId
    ) {

        String sql = """
            DELETE FROM grievances
            WHERE
                id = ?
                AND employee_id = ?
                AND status = 'New'
            """;

        return jdbcTemplate.update(
                sql,
                id,
                employeeId
        );
    }

    // ============================================================
    // UPDATE STATUS
    // ============================================================

    public int updateStatus(
            Long id,
            String status,
            String assignedTo
    ) {

        String sql;

        if (
                assignedTo != null
                        && !assignedTo.isBlank()
        ) {

            sql = """
                UPDATE grievances
                SET
                    status = ?,
                    assigned_to = ?
                WHERE id = ?
                """;

            return jdbcTemplate.update(
                    sql,
                    status,
                    assignedTo,
                    id
            );
        }

        sql = """
            UPDATE grievances
            SET
                status = ?
            WHERE id = ?
            """;

        return jdbcTemplate.update(
                sql,
                status,
                id
        );
    }

    // ============================================================
    // ADD RESPONSE
    // ============================================================

    public Long addResponse(
            Long grievanceId,
            String employeeId,
            String text
    ) {

        String sql = """
            INSERT INTO grievance_responses (
                grievance_id,
                employee_id,
                response_text
            )
            VALUES (?, ?, ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                grievanceId,
                employeeId,
                text
        );
    }

    // ============================================================
    // FIND RESPONSES
    // ============================================================

    private List<Grievance.GrievanceResponse>
    findResponses(
            Long grievanceId
    ) {

        String sql = """
            SELECT
                r.id,
                r.employee_id,
                CONCAT(
                    e.first_name,
                    ' ',
                    e.last_name
                ) AS employee_name,
                r.response_text,
                r.created_at
            FROM grievance_responses r
            JOIN employees e
                ON e.employee_number =
                   r.employee_id
            WHERE r.grievance_id = ?
            ORDER BY r.created_at ASC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Grievance.GrievanceResponse
                            response =
                            new Grievance
                                    .GrievanceResponse();

                    response.setId(
                            rs.getLong("id")
                    );

                    response.setEmployeeId(
                            rs.getString(
                                    "employee_id"
                            )
                    );

                    response.setEmployeeName(
                            rs.getString(
                                    "employee_name"
                            )
                    );

                    response.setText(
                            rs.getString(
                                    "response_text"
                            )
                    );

                    Timestamp createdAt =
                            rs.getTimestamp(
                                    "created_at"
                            );

                    if (createdAt != null) {
                        response.setCreatedAt(
                                createdAt
                                        .toLocalDateTime()
                        );
                    }

                    return response;
                },
                grievanceId
        );
    }
}