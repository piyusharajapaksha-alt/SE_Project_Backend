package com.staffhub.repository;

import com.staffhub.model.TrainingProgram;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TrainingRepository {

    private final JdbcTemplate jdbcTemplate;

    public TrainingRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // ============================================================
    // GET ALL TRAINING PROGRAMS
    // ============================================================

    public List<TrainingProgram> findAll() {

        String sql = """
                SELECT
                    id,
                    title,
                    description,
                    trainer,
                    category,
                    start_date,
                    end_date,
                    location,
                    capacity,
                    training_for,
                    status
                FROM training_programs
                ORDER BY start_date ASC, id ASC
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {

                    TrainingProgram training =
                            mapTraining(resultSet);

                    enrichTraining(training);

                    return training;
                }
        );
    }


    // ============================================================
    // GET ONE TRAINING PROGRAM
    // ============================================================

    public TrainingProgram findById(Long id) {

        String sql = """
                SELECT
                    id,
                    title,
                    description,
                    trainer,
                    category,
                    start_date,
                    end_date,
                    location,
                    capacity,
                    training_for,
                    status
                FROM training_programs
                WHERE id = ?
                """;

        try {

            TrainingProgram training =
                    jdbcTemplate.queryForObject(
                            sql,
                            (resultSet, rowNumber) ->
                                    mapTraining(resultSet),
                            id
                    );

            if (training != null) {
                enrichTraining(training);
            }

            return training;

        } catch (EmptyResultDataAccessException exception) {

            return null;
        }
    }


    // ============================================================
    // CREATE
    // ============================================================

    public TrainingProgram create(
            TrainingProgram training
    ) {

        String sql = """
                INSERT INTO training_programs (
                    title,
                    description,
                    trainer,
                    category,
                    start_date,
                    end_date,
                    location,
                    capacity,
                    training_for,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        Long generatedId =
                jdbcTemplate.query(
                        connection -> {

                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            sql
                                    );

                            statement.setString(
                                    1,
                                    training.getTitle()
                            );

                            statement.setString(
                                    2,
                                    training.getDescription()
                            );

                            statement.setString(
                                    3,
                                    training.getTrainer()
                            );

                            statement.setString(
                                    4,
                                    training.getCategory()
                            );

                            statement.setObject(
                                    5,
                                    training.getStartDate()
                            );

                            statement.setObject(
                                    6,
                                    training.getEndDate()
                            );

                            statement.setString(
                                    7,
                                    training.getLocation()
                            );

                            statement.setInt(
                                    8,
                                    training.getCapacity()
                            );

                            Array trainingForArray =
                                    connection.createArrayOf(
                                            "text",
                                            training.getTrainingFor()
                                                    .toArray()
                                    );

                            statement.setArray(
                                    9,
                                    trainingForArray
                            );

                            statement.setString(
                                    10,
                                    training.getStatus()
                            );

                            return statement;
                        },
                        resultSet -> {

                            if (resultSet.next()) {

                                return resultSet.getLong(
                                        "id"
                                );
                            }

                            throw new IllegalStateException(
                                    "Failed to create training program"
                            );
                        }
                );

        training.setId(generatedId);

        enrichTraining(training);

        return training;
    }


    // ============================================================
    // UPDATE
    // ============================================================

    public TrainingProgram update(
            Long id,
            TrainingProgram training
    ) {

        String sql = """
                UPDATE training_programs
                SET
                    title = ?,
                    description = ?,
                    trainer = ?,
                    category = ?,
                    start_date = ?,
                    end_date = ?,
                    location = ?,
                    capacity = ?,
                    training_for = ?,
                    status = ?
                WHERE id = ?
                """;

        int rowsUpdated =
                jdbcTemplate.update(
                        connection -> {

                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            sql
                                    );

                            statement.setString(
                                    1,
                                    training.getTitle()
                            );

                            statement.setString(
                                    2,
                                    training.getDescription()
                            );

                            statement.setString(
                                    3,
                                    training.getTrainer()
                            );

                            statement.setString(
                                    4,
                                    training.getCategory()
                            );

                            statement.setObject(
                                    5,
                                    training.getStartDate()
                            );

                            statement.setObject(
                                    6,
                                    training.getEndDate()
                            );

                            statement.setString(
                                    7,
                                    training.getLocation()
                            );

                            statement.setInt(
                                    8,
                                    training.getCapacity()
                            );

                            Array trainingForArray =
                                    connection.createArrayOf(
                                            "text",
                                            training.getTrainingFor()
                                                    .toArray()
                                    );

                            statement.setArray(
                                    9,
                                    trainingForArray
                            );

                            statement.setString(
                                    10,
                                    training.getStatus()
                            );

                            statement.setLong(
                                    11,
                                    id
                            );

                            return statement;
                        }
                );

        if (rowsUpdated == 0) {

            throw new IllegalArgumentException(
                    "Training program not found"
            );
        }

        training.setId(id);

        enrichTraining(training);

        return training;
    }


    // ============================================================
    // DELETE
    // ============================================================

    public void delete(Long id) {

        String sql = """
                DELETE FROM training_programs
                WHERE id = ?
                """;

        int rowsDeleted =
                jdbcTemplate.update(
                        sql,
                        id
                );

        if (rowsDeleted == 0) {

            throw new IllegalArgumentException(
                    "Training program not found"
            );
        }
    }


    // ============================================================
    // COUNT REGISTRATIONS
    // ============================================================

    public int countRegistrations(
            Long trainingId
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM training_registrations
                WHERE training_id = ?
                """;

        Integer count =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        trainingId
                );

        return count != null ? count : 0;
    }


    // ============================================================
    // CHECK EMPLOYEE
    // ============================================================

    public boolean employeeExists(
            String employeeId
    ) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM employees
                    WHERE employee_number = ?
                )
                """;

        Boolean exists =
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        employeeId
                );

        return Boolean.TRUE.equals(exists);
    }


    // ============================================================
    // ASSIGN EMPLOYEE
    // ============================================================

    public void assignEmployee(
            Long trainingId,
            String employeeId
    ) {

        String sql = """
                INSERT INTO training_assignments (
                    training_id,
                    employee_id
                )
                VALUES (?, ?)
                ON CONFLICT (
                    training_id,
                    employee_id
                )
                DO NOTHING
                """;

        jdbcTemplate.update(
                sql,
                trainingId,
                employeeId
        );
    }


    // ============================================================
    // REMOVE ASSIGNMENT
    // ============================================================

    public void removeEmployeeAssignment(
            Long trainingId,
            String employeeId
    ) {

        String sql = """
                DELETE FROM training_assignments
                WHERE training_id = ?
                  AND employee_id = ?
                """;

        jdbcTemplate.update(
                sql,
                trainingId,
                employeeId
        );
    }


    // ============================================================
    // GET EMPLOYEES FOR SELECTED DEPARTMENTS
    // ============================================================

    public List<String> findEmployeeNumbersByDepartments(
            List<String> departments
    ) {

        if (departments == null ||
                departments.isEmpty()) {

            return new ArrayList<>();
        }

        String placeholders =
                String.join(
                        ",",
                        java.util.Collections.nCopies(
                                departments.size(),
                                "?"
                        )
                );

        String sql = """
                SELECT employee_number
                FROM employees
                WHERE department IN (
                """
                + placeholders +
                """
                )
                ORDER BY employee_number
                """;

        return jdbcTemplate.query(
                sql,
                departments.toArray(),
                (resultSet, rowNumber) ->
                        resultSet.getString(
                                "employee_number"
                        )
        );
    }


    // ============================================================
    // REMOVE ASSIGNMENTS NOT IN SELECTED DEPARTMENTS
    // ============================================================

    public void removeAssignmentsOutsideDepartments(
            Long trainingId,
            List<String> departments
    ) {

        if (departments == null ||
                departments.isEmpty()) {

            jdbcTemplate.update(
                    """
                    DELETE FROM training_assignments
                    WHERE training_id = ?
                    """,
                    trainingId
            );

            return;
        }

        String placeholders =
                String.join(
                        ",",
                        java.util.Collections.nCopies(
                                departments.size(),
                                "?"
                        )
                );

        String sql = """
                DELETE FROM training_assignments
                WHERE training_id = ?
                  AND employee_id NOT IN (
                      SELECT employee_number
                      FROM employees
                      WHERE department IN (
                """
                + placeholders +
                """
                      )
                  )
                """;

        List<Object> parameters =
                new ArrayList<>();

        parameters.add(trainingId);

        parameters.addAll(departments);

        jdbcTemplate.update(
                sql,
                parameters.toArray()
        );
    }


    // ============================================================
    // GET TRAINING EMPLOYEES
    // ============================================================

    public List<Map<String, Object>> findTrainingEmployees(
            Long trainingId
    ) {

        String sql = """
                SELECT
                    e.id,
                    e.employee_number,
                    e.first_name,
                    e.last_name,
                    e.email,
                    e.department,
                    e.position,
                    CASE
                        WHEN ta.employee_id IS NOT NULL
                        THEN 'Assigned'
                        ELSE 'Not Assigned'
                    END AS assignment_status,
                    CASE
                        WHEN tr.employee_id IS NOT NULL
                        THEN 'Registered'
                        ELSE 'Not Registered'
                    END AS registration_status
                FROM employees e
                LEFT JOIN training_assignments ta
                    ON ta.employee_id = e.employee_number
                   AND ta.training_id = ?
                LEFT JOIN training_registrations tr
                    ON tr.employee_id = e.employee_number
                   AND tr.training_id = ?
                WHERE ta.employee_id IS NOT NULL
                ORDER BY
                    e.department ASC,
                    e.first_name ASC,
                    e.last_name ASC
                """;

        return jdbcTemplate.queryForList(
                sql,
                trainingId,
                trainingId
        );
    }


    // ============================================================
    // REGISTER EMPLOYEE
    // ============================================================

    public void registerEmployee(
            Long trainingId,
            String employeeId
    ) {

        String sql = """
                INSERT INTO training_registrations (
                    training_id,
                    employee_id
                )
                VALUES (?, ?)
                """;

        jdbcTemplate.update(
                sql,
                trainingId,
                employeeId
        );
    }


    // ============================================================
    // CHECK REGISTRATION
    // ============================================================

    public boolean isEmployeeRegistered(
            Long trainingId,
            String employeeId
    ) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM training_registrations
                    WHERE training_id = ?
                      AND employee_id = ?
                )
                """;

        Boolean exists =
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        trainingId,
                        employeeId
                );

        return Boolean.TRUE.equals(exists);
    }


    // ============================================================
    // UNREGISTER EMPLOYEE
    // ============================================================

    public void unregisterEmployee(
            Long trainingId,
            String employeeId
    ) {

        String sql = """
                DELETE FROM training_registrations
                WHERE training_id = ?
                  AND employee_id = ?
                """;

        jdbcTemplate.update(
                sql,
                trainingId,
                employeeId
        );
    }


    // ============================================================
    // ENRICH TRAINING DATA
    // ============================================================

    private void enrichTraining(
            TrainingProgram training
    ) {

        Long trainingId =
                training.getId();

        training.setAssignedEmployeeIds(
                jdbcTemplate.query(
                        """
                        SELECT employee_id
                        FROM training_assignments
                        WHERE training_id = ?
                        ORDER BY employee_id
                        """,
                        (rs, rowNum) ->
                                rs.getString(
                                        "employee_id"
                                ),
                        trainingId
                )
        );


        training.setRegisteredEmployeeIds(
                jdbcTemplate.query(
                        """
                        SELECT employee_id
                        FROM training_registrations
                        WHERE training_id = ?
                        ORDER BY employee_id
                        """,
                        (rs, rowNum) ->
                                rs.getString(
                                        "employee_id"
                                ),
                        trainingId
                )
        );


        Map<String, String> attendance =
                new HashMap<>();

        try {

            jdbcTemplate.query(
                    """
                    SELECT
                        employee_id,
                        status
                    FROM training_attendance
                    WHERE training_id = ?
                    """,
                    rs -> {

                        attendance.put(
                                rs.getString("employee_id"),
                                rs.getString("status")
                        );
                    },
                    trainingId
            );

        } catch (Exception ignored) {
            /*
             * Keeps existing training functionality working
             * even if attendance table has not yet been created.
             */
        }

        training.setAttendance(attendance);


        Map<String, String> completion =
                new HashMap<>();

        try {

            jdbcTemplate.query(
                    """
                    SELECT
                        employee_id,
                        status
                    FROM training_completion
                    WHERE training_id = ?
                    """,
                    rs -> {

                        completion.put(
                                rs.getString("employee_id"),
                                rs.getString("status")
                        );
                    },
                    trainingId
            );

        } catch (Exception ignored) {
            /*
             * Completion functionality can be added later.
             */
        }

        training.setCompletion(completion);
    }


    // ============================================================
    // MAP DATABASE ROW
    // ============================================================

    private TrainingProgram mapTraining(
            java.sql.ResultSet resultSet
    ) throws java.sql.SQLException {

        TrainingProgram training =
                new TrainingProgram();

        training.setId(
                resultSet.getLong("id")
        );

        training.setTitle(
                resultSet.getString("title")
        );

        training.setDescription(
                resultSet.getString("description")
        );

        training.setTrainer(
                resultSet.getString("trainer")
        );

        training.setCategory(
                resultSet.getString("category")
        );


        if (resultSet.getDate("start_date") != null) {

            training.setStartDate(
                    resultSet
                            .getDate("start_date")
                            .toLocalDate()
            );
        }


        if (resultSet.getDate("end_date") != null) {

            training.setEndDate(
                    resultSet
                            .getDate("end_date")
                            .toLocalDate()
            );
        }


        training.setLocation(
                resultSet.getString("location")
        );

        training.setCapacity(
                resultSet.getInt("capacity")
        );


        training.setTrainingFor(
                getTrainingFor(
                        resultSet.getArray("training_for")
                )
        );


        training.setStatus(
                resultSet.getString("status")
        );

        return training;
    }


    // ============================================================
    // POSTGRES TEXT[] → JAVA LIST
    // ============================================================

    private List<String> getTrainingFor(
            Array sqlArray
    ) {

        if (sqlArray == null) {

            return new ArrayList<>();
        }

        try {

            Object[] values =
                    (Object[]) sqlArray.getArray();

            List<String> result =
                    new ArrayList<>();

            for (Object value : values) {

                if (value != null) {

                    result.add(
                            value.toString()
                    );
                }
            }

            return result;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to read training_for",
                    exception
            );
        }
    }
}