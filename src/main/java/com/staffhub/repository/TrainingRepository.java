package com.staffhub.repository;

import com.staffhub.model.TrainingProgram;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TrainingRepository {

    private final JdbcTemplate jdbcTemplate;

    public TrainingRepository(JdbcTemplate jdbcTemplate) {
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
                FROM dbo.training_programs
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

        if (id == null) {
            return null;
        }

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
                FROM dbo.training_programs
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

        if (training == null) {
            throw new IllegalArgumentException(
                    "Training data is required"
            );
        }

        String sql = """
                INSERT INTO dbo.training_programs (
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
                OUTPUT INSERTED.id
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String trainingForJson =
                toTrainingForJson(
                        training.getTrainingFor()
                );

        Long generatedId =
                jdbcTemplate.queryForObject(
                        sql,
                        Long.class,

                        training.getTitle(),
                        training.getDescription(),
                        training.getTrainer(),
                        training.getCategory(),
                        training.getStartDate(),
                        training.getEndDate(),
                        training.getLocation(),
                        training.getCapacity(),
                        trainingForJson,
                        training.getStatus()
                );

        if (generatedId == null) {

            throw new IllegalStateException(
                    "Failed to create training program"
            );
        }

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

        if (id == null) {
            throw new IllegalArgumentException(
                    "Training ID is required"
            );
        }

        if (training == null) {
            throw new IllegalArgumentException(
                    "Training data is required"
            );
        }

        String sql = """
                UPDATE dbo.training_programs
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

        String trainingForJson =
                toTrainingForJson(
                        training.getTrainingFor()
                );

        int rowsUpdated =
                jdbcTemplate.update(
                        sql,

                        training.getTitle(),
                        training.getDescription(),
                        training.getTrainer(),
                        training.getCategory(),
                        training.getStartDate(),
                        training.getEndDate(),
                        training.getLocation(),
                        training.getCapacity(),
                        trainingForJson,
                        training.getStatus(),
                        id
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

        if (id == null) {
            throw new IllegalArgumentException(
                    "Training ID is required"
            );
        }

        int rowsDeleted =
                jdbcTemplate.update(
                        """
                        DELETE FROM dbo.training_programs
                        WHERE id = ?
                        """,
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

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM dbo.training_registrations
                        WHERE training_id = ?
                        """,
                        Integer.class,
                        trainingId
                );

        return count != null ? count : 0;
    }

    // ============================================================
    // CHECK EMPLOYEE EXISTS
    // ============================================================

    public boolean employeeExists(
            String employeeId
    ) {

        if (employeeId == null ||
                employeeId.trim().isEmpty()) {

            return false;
        }

        Integer exists =
                jdbcTemplate.queryForObject(
                        """
                        SELECT
                            CASE
                                WHEN EXISTS (
                                    SELECT 1
                                    FROM dbo.employees
                                    WHERE employee_number = ?
                                )
                                THEN 1
                                ELSE 0
                            END
                        """,
                        Integer.class,
                        employeeId.trim()
                );

        return exists != null && exists == 1;
    }

    // ============================================================
    // CHECK EMPLOYEE EXISTS IN COMPANY
    // ============================================================

    public boolean employeeExists(
            String employeeId,
            Long companyId
    ) {

        if (employeeId == null ||
                employeeId.trim().isEmpty() ||
                companyId == null) {

            return false;
        }

        Integer exists =
                jdbcTemplate.queryForObject(
                        """
                        SELECT
                            CASE
                                WHEN EXISTS (
                                    SELECT 1
                                    FROM dbo.employees
                                    WHERE employee_number = ?
                                      AND company_id = ?
                                )
                                THEN 1
                                ELSE 0
                            END
                        """,
                        Integer.class,
                        employeeId.trim(),
                        companyId
                );

        return exists != null && exists == 1;
    }

    // ============================================================
    // ASSIGN EMPLOYEE
    // ============================================================

    public void assignEmployee(
            Long trainingId,
            String employeeId
    ) {

        if (trainingId == null ||
                employeeId == null ||
                employeeId.trim().isEmpty()) {

            return;
        }

        String normalizedEmployeeId =
                employeeId.trim();

        String sql = """
                IF NOT EXISTS (
                    SELECT 1
                    FROM dbo.training_assignments
                    WHERE training_id = ?
                      AND employee_id = ?
                )
                BEGIN
                    INSERT INTO dbo.training_assignments (
                        training_id,
                        employee_id
                    )
                    VALUES (?, ?)
                END
                """;

        jdbcTemplate.update(
                sql,
                trainingId,
                normalizedEmployeeId,
                trainingId,
                normalizedEmployeeId
        );
    }

    // ============================================================
    // REMOVE EMPLOYEE ASSIGNMENT
    // ============================================================

    public void removeEmployeeAssignment(
            Long trainingId,
            String employeeId
    ) {

        if (trainingId == null ||
                employeeId == null ||
                employeeId.trim().isEmpty()) {

            return;
        }

        jdbcTemplate.update(
                """
                DELETE FROM dbo.training_assignments
                WHERE training_id = ?
                  AND employee_id = ?
                """,
                trainingId,
                employeeId.trim()
        );
    }

    // ============================================================
    // GET EMPLOYEE NUMBERS BY DEPARTMENT
    //
    // IMPORTANT:
    //
    // The comparison is normalized on BOTH sides:
    //
    // department table name
    // employee department
    // selected department parameter
    //
    // This fixes:
    //
    // "HR" vs "hr"
    // "Finance" vs " finance "
    // "Human Resources" vs "human resources"
    // ============================================================

    public List<String> findEmployeeNumbersByDepartments(
            List<String> departments,
            Long companyId
    ) {

        if (departments == null ||
                departments.isEmpty() ||
                companyId == null) {

            return new ArrayList<>();
        }

        List<String> normalizedDepartments =
                normalizeDepartmentValues(departments);

        if (normalizedDepartments.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders =
                String.join(
                        ",",
                        Collections.nCopies(
                                normalizedDepartments.size(),
                                "?"
                        )
                );

        String sql = """
                SELECT DISTINCT
                    e.employee_number

                FROM dbo.employees e

                INNER JOIN dbo.departments d
                    ON d.company_id = e.company_id
                   AND LOWER(LTRIM(RTRIM(d.name))) =
                       LOWER(LTRIM(RTRIM(e.department)))

                WHERE e.company_id = ?
                  AND d.company_id = ?
                  AND d.active = 1

                  AND LOWER(LTRIM(RTRIM(d.name))) IN (
                """
                + placeholders +
                """
                  )

                ORDER BY
                    e.employee_number
                """;

        List<Object> parameters =
                new ArrayList<>();

        parameters.add(companyId);
        parameters.add(companyId);

        /*
         * IMPORTANT:
         *
         * The SQL normalizes d.name.
         * The parameters are also normalized
         * before being supplied.
         */
        for (String department :
                normalizedDepartments) {

            parameters.add(
                    department
                            .trim()
                            .toLowerCase()
            );
        }

        return jdbcTemplate.query(
                sql,
                parameters.toArray(),
                (resultSet, rowNumber) ->
                        resultSet.getString(
                                "employee_number"
                        )
        );
    }

    // ============================================================
    // REMOVE ASSIGNMENTS OUTSIDE SELECTED DEPARTMENTS
    // ============================================================

    public void removeAssignmentsOutsideDepartments(
            Long trainingId,
            List<String> departments,
            Long companyId
    ) {

        if (trainingId == null) {
            return;
        }

        /*
         * If there are no selected departments,
         * remove automatic department assignments.
         */
        if (departments == null ||
                departments.isEmpty()) {

            jdbcTemplate.update(
                    """
                    DELETE FROM dbo.training_assignments
                    WHERE training_id = ?
                    """,
                    trainingId
            );

            return;
        }

        if (companyId == null) {
            throw new IllegalArgumentException(
                    "Company ID is required"
            );
        }

        List<String> normalizedDepartments =
                normalizeDepartmentValues(
                        departments
                );

        if (normalizedDepartments.isEmpty()) {

            jdbcTemplate.update(
                    """
                    DELETE FROM dbo.training_assignments
                    WHERE training_id = ?
                    """,
                    trainingId
            );

            return;
        }

        String placeholders =
                String.join(
                        ",",
                        Collections.nCopies(
                                normalizedDepartments.size(),
                                "?"
                        )
                );

        String sql = """
                DELETE FROM dbo.training_assignments

                WHERE training_id = ?

                  AND employee_id NOT IN
                  (
                      SELECT DISTINCT
                          e.employee_number

                      FROM dbo.employees e

                      INNER JOIN dbo.departments d
                          ON d.company_id = e.company_id
                         AND LOWER(LTRIM(RTRIM(d.name))) =
                             LOWER(LTRIM(RTRIM(e.department)))

                      WHERE e.company_id = ?
                        AND d.company_id = ?
                        AND d.active = 1

                        AND LOWER(LTRIM(RTRIM(d.name))) IN (
                """
                + placeholders +
                """
                        )
                  )
                """;

        List<Object> parameters =
                new ArrayList<>();

        parameters.add(trainingId);
        parameters.add(companyId);
        parameters.add(companyId);

        for (String department :
                normalizedDepartments) {

            parameters.add(
                    department
                            .trim()
                            .toLowerCase()
            );
        }

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

                FROM dbo.employees e

                INNER JOIN dbo.training_assignments ta
                    ON ta.employee_id = e.employee_number
                   AND ta.training_id = ?

                LEFT JOIN dbo.training_registrations tr
                    ON tr.employee_id = e.employee_number
                   AND tr.training_id = ?

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

        jdbcTemplate.update(
                """
                INSERT INTO dbo.training_registrations (
                    training_id,
                    employee_id
                )
                VALUES (?, ?)
                """,
                trainingId,
                employeeId
        );
    }

    // ============================================================
    // CHECK EMPLOYEE REGISTRATION
    // ============================================================

    public boolean isEmployeeRegistered(
            Long trainingId,
            String employeeId
    ) {

        Integer exists =
                jdbcTemplate.queryForObject(
                        """
                        SELECT
                            CASE
                                WHEN EXISTS (
                                    SELECT 1
                                    FROM dbo.training_registrations
                                    WHERE training_id = ?
                                      AND employee_id = ?
                                )
                                THEN 1
                                ELSE 0
                            END
                        """,
                        Integer.class,
                        trainingId,
                        employeeId
                );

        return exists != null && exists == 1;
    }

    // ============================================================
    // UNREGISTER EMPLOYEE
    // ============================================================

    public void unregisterEmployee(
            Long trainingId,
            String employeeId
    ) {

        jdbcTemplate.update(
                """
                DELETE FROM dbo.training_registrations
                WHERE training_id = ?
                  AND employee_id = ?
                """,
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

        if (training == null ||
                training.getId() == null) {

            return;
        }

        Long trainingId =
                training.getId();

        // --------------------------------------------------------
        // Assigned employees
        // --------------------------------------------------------

        training.setAssignedEmployeeIds(
                jdbcTemplate.query(
                        """
                        SELECT employee_id
                        FROM dbo.training_assignments
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

        // --------------------------------------------------------
        // Registered employees
        // --------------------------------------------------------

        training.setRegisteredEmployeeIds(
                jdbcTemplate.query(
                        """
                        SELECT employee_id
                        FROM dbo.training_registrations
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

        // --------------------------------------------------------
        // Attendance
        // --------------------------------------------------------

        Map<String, String> attendance =
                new HashMap<>();

        try {

            List<Map<String, Object>>
                    attendanceRows =
                    jdbcTemplate.queryForList(
                            """
                            SELECT
                                employee_id,
                                status
                            FROM dbo.training_attendance
                            WHERE training_id = ?
                            """,
                            trainingId
                    );

            for (
                    Map<String, Object> row :
                    attendanceRows
            ) {

                Object employeeId =
                        row.get("employee_id");

                Object status =
                        row.get("status");

                if (employeeId != null) {

                    attendance.put(
                            employeeId.toString(),
                            status != null
                                    ? status.toString()
                                    : null
                    );
                }
            }

        } catch (Exception ignored) {

            /*
             * Attendance is optional enrichment.
             *
             * Do not fail the complete training
             * response if the attendance table is
             * unavailable or has a schema difference.
             */
        }

        training.setAttendance(
                attendance
        );

        // --------------------------------------------------------
        // Completion
        // --------------------------------------------------------

        Map<String, String> completion =
                new HashMap<>();

        try {

            List<Map<String, Object>>
                    completionRows =
                    jdbcTemplate.queryForList(
                            """
                            SELECT
                                employee_id,
                                status
                            FROM dbo.training_completion
                            WHERE training_id = ?
                            """,
                            trainingId
                    );

            for (
                    Map<String, Object> row :
                    completionRows
            ) {

                Object employeeId =
                        row.get("employee_id");

                Object status =
                        row.get("status");

                if (employeeId != null) {

                    completion.put(
                            employeeId.toString(),
                            status != null
                                    ? status.toString()
                                    : null
                    );
                }
            }

        } catch (Exception ignored) {

            /*
             * Completion is optional enrichment.
             */
        }

        training.setCompletion(
                completion
        );
    }

    // ============================================================
    // MAP DATABASE ROW
    // ============================================================

    private TrainingProgram mapTraining(
            ResultSet resultSet
    ) throws SQLException {

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

        if (
                resultSet.getDate("start_date")
                        != null
        ) {

            training.setStartDate(
                    resultSet
                            .getDate("start_date")
                            .toLocalDate()
            );
        }

        if (
                resultSet.getDate("end_date")
                        != null
        ) {

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
                        resultSet.getString(
                                "training_for"
                        )
                )
        );

        training.setStatus(
                resultSet.getString("status")
        );

        return training;
    }

    // ============================================================
    // SQL SERVER JSON -> LIST
    // ============================================================

    private List<String> getTrainingFor(
            String trainingForJson
    ) {

        List<String> result =
                new ArrayList<>();

        if (
                trainingForJson == null ||
                trainingForJson.isBlank()
        ) {

            return result;
        }

        String json =
                trainingForJson.trim();

        /*
         * Expected:
         *
         * ["Finance"]
         *
         * or:
         *
         * ["Finance","Human Resources"]
         */

        if (
                json.startsWith("[") &&
                json.endsWith("]")
        ) {

            json = json.substring(
                    1,
                    json.length() - 1
            ).trim();
        }

        if (json.isBlank()) {
            return result;
        }

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {

            char character =
                    json.charAt(i);

            if (escaped) {

                current.append(character);
                escaped = false;
                continue;
            }

            if (character == '\\') {

                current.append(character);
                escaped = true;
                continue;
            }

            if (character == '"') {

                insideQuotes = !insideQuotes;
                continue;
            }

            if (
                    character == ',' &&
                    !insideQuotes
            ) {

                addParsedDepartment(
                        result,
                        current.toString()
                );

                current.setLength(0);

                continue;
            }

            current.append(character);
        }

        addParsedDepartment(
                result,
                current.toString()
        );

        return result;
    }

    // ============================================================
    // ADD PARSED DEPARTMENT
    // ============================================================

    private void addParsedDepartment(
            List<String> result,
            String value
    ) {

        if (value == null) {
            return;
        }

        String cleaned =
                value.trim()
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");

        if (
                !cleaned.isBlank() &&
                !result.contains(cleaned)
        ) {

            result.add(cleaned);
        }
    }

    // ============================================================
    // LIST -> SQL SERVER JSON
    // ============================================================

    private String toTrainingForJson(
            List<String> trainingFor
    ) {

        if (
                trainingFor == null ||
                trainingFor.isEmpty()
        ) {

            return "[]";
        }

        StringBuilder json =
                new StringBuilder("[");

        boolean first = true;

        for (String department :
                trainingFor) {

            if (
                    department == null ||
                    department.trim().isEmpty()
            ) {
                continue;
            }

            if (!first) {
                json.append(",");
            }

            String escaped =
                    department
                            .trim()
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"");

            json.append("\"")
                    .append(escaped)
                    .append("\"");

            first = false;
        }

        json.append("]");

        return json.toString();
    }

    // ============================================================
    // NORMALIZE DEPARTMENT VALUES
    // ============================================================

    private List<String> normalizeDepartmentValues(
            List<String> departments
    ) {

        List<String> result =
                new ArrayList<>();

        if (departments == null) {
            return result;
        }

        for (String department :
                departments) {

            if (
                    department == null ||
                    department.trim().isEmpty()
            ) {
                continue;
            }

            String normalized =
                    department.trim();

            boolean exists =
                    result.stream()
                            .anyMatch(
                                    existing ->
                                            existing
                                                    .equalsIgnoreCase(
                                                            normalized
                                                    )
                            );

            if (!exists) {
                result.add(normalized);
            }
        }

        return result;
    }
}

