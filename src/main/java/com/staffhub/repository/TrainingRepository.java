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
                                FROM training_programs
                                ORDER BY start_date ASC, id ASC
                                """;

                return jdbcTemplate.query(
                                sql,
                                (resultSet, rowNumber) -> {

                                        TrainingProgram training = mapTraining(resultSet);

                                        enrichTraining(training);

                                        return training;
                                });
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

                        TrainingProgram training = jdbcTemplate.queryForObject(
                                        sql,
                                        (resultSet, rowNumber) -> mapTraining(resultSet),
                                        id);

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
                        TrainingProgram training) {

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
                                OUTPUT INSERTED.id
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """;

                String trainingForJson = toTrainingForJson(
                                training.getTrainingFor());

                Long generatedId = jdbcTemplate.queryForObject(
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
                                training.getStatus());

                if (generatedId == null) {

                        throw new IllegalStateException(
                                        "Failed to create training program");
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
                        TrainingProgram training) {

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

                String trainingForJson = toTrainingForJson(
                                training.getTrainingFor());

                int rowsUpdated = jdbcTemplate.update(
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
                                id);

                if (rowsUpdated == 0) {

                        throw new IllegalArgumentException(
                                        "Training program not found");
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

                int rowsDeleted = jdbcTemplate.update(
                                sql,
                                id);

                if (rowsDeleted == 0) {

                        throw new IllegalArgumentException(
                                        "Training program not found");
                }
        }

        // ============================================================
        // COUNT REGISTRATIONS
        // ============================================================

        public int countRegistrations(
                        Long trainingId) {

                String sql = """
                                SELECT COUNT(*)
                                FROM training_registrations
                                WHERE training_id = ?
                                """;

                Integer count = jdbcTemplate.queryForObject(
                                sql,
                                Integer.class,
                                trainingId);

                return count != null ? count : 0;
        }

        // ============================================================
        // CHECK EMPLOYEE EXISTS
        // ============================================================

        public boolean employeeExists(
                        String employeeId) {

                String sql = """
                                SELECT
                                    CASE
                                        WHEN EXISTS (
                                            SELECT 1
                                            FROM employees
                                            WHERE employee_number = ?
                                        )
                                        THEN 1
                                        ELSE 0
                                    END
                                """;

                Integer exists = jdbcTemplate.queryForObject(
                                sql,
                                Integer.class,
                                employeeId);

                return exists != null && exists == 1;
        }

        // ============================================================
        // ASSIGN EMPLOYEE
        // ============================================================

        public void assignEmployee(
                        Long trainingId,
                        String employeeId) {

                String sql = """
                                IF NOT EXISTS (
                                    SELECT 1
                                    FROM training_assignments
                                    WHERE training_id = ?
                                      AND employee_id = ?
                                )
                                BEGIN
                                    INSERT INTO training_assignments (
                                        training_id,
                                        employee_id
                                    )
                                    VALUES (?, ?)
                                END
                                """;

                jdbcTemplate.update(
                                sql,
                                trainingId,
                                employeeId,
                                trainingId,
                                employeeId);
        }

        // ============================================================
        // REMOVE EMPLOYEE ASSIGNMENT
        // ============================================================

        public void removeEmployeeAssignment(
                        Long trainingId,
                        String employeeId) {

                String sql = """
                                DELETE FROM training_assignments
                                WHERE training_id = ?
                                  AND employee_id = ?
                                """;

                jdbcTemplate.update(
                                sql,
                                trainingId,
                                employeeId);
        }

        // ============================================================
        // GET EMPLOYEE NUMBERS BY DEPARTMENT
        // ============================================================

        public List<String> findEmployeeNumbersByDepartments(
                        List<String> departments) {

                if (departments == null ||
                                departments.isEmpty()) {

                        return new ArrayList<>();
                }

                String placeholders = String.join(
                                ",",
                                Collections.nCopies(
                                                departments.size(),
                                                "?"));

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
                                (resultSet, rowNumber) -> resultSet.getString(
                                                "employee_number"));
        }

        // ============================================================
        // REMOVE ASSIGNMENTS OUTSIDE SELECTED DEPARTMENTS
        // ============================================================

        public void removeAssignmentsOutsideDepartments(
                        Long trainingId,
                        List<String> departments) {

                if (departments == null ||
                                departments.isEmpty()) {

                        jdbcTemplate.update(
                                        """
                                                        DELETE FROM training_assignments
                                                        WHERE training_id = ?
                                                        """,
                                        trainingId);

                        return;
                }

                String placeholders = String.join(
                                ",",
                                Collections.nCopies(
                                                departments.size(),
                                                "?"));

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

                List<Object> parameters = new ArrayList<>();

                parameters.add(trainingId);
                parameters.addAll(departments);

                jdbcTemplate.update(
                                sql,
                                parameters.toArray());
        }

        // ============================================================
        // GET TRAINING EMPLOYEES
        // ============================================================

        public List<Map<String, Object>> findTrainingEmployees(
                        Long trainingId) {

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
                                trainingId);
        }

        // ============================================================
        // REGISTER EMPLOYEE
        // ============================================================

        public void registerEmployee(
                        Long trainingId,
                        String employeeId) {

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
                                employeeId);
        }

        // ============================================================
        // CHECK EMPLOYEE REGISTRATION
        // ============================================================

        public boolean isEmployeeRegistered(
                        Long trainingId,
                        String employeeId) {

                String sql = """
                                SELECT
                                    CASE
                                        WHEN EXISTS (
                                            SELECT 1
                                            FROM training_registrations
                                            WHERE training_id = ?
                                              AND employee_id = ?
                                        )
                                        THEN 1
                                        ELSE 0
                                    END
                                """;

                Integer exists = jdbcTemplate.queryForObject(
                                sql,
                                Integer.class,
                                trainingId,
                                employeeId);

                return exists != null && exists == 1;
        }

        // ============================================================
        // UNREGISTER EMPLOYEE
        // ============================================================

        public void unregisterEmployee(
                        Long trainingId,
                        String employeeId) {

                String sql = """
                                DELETE FROM training_registrations
                                WHERE training_id = ?
                                  AND employee_id = ?
                                """;

                jdbcTemplate.update(
                                sql,
                                trainingId,
                                employeeId);
        }

        // ============================================================
        // ENRICH TRAINING DATA
        // ============================================================

        private void enrichTraining(
                        TrainingProgram training) {

                Long trainingId = training.getId();

                // --------------------------------------------------------
                // Assigned employees
                // --------------------------------------------------------

                training.setAssignedEmployeeIds(
                                jdbcTemplate.query(
                                                """
                                                                SELECT employee_id
                                                                FROM training_assignments
                                                                WHERE training_id = ?
                                                                ORDER BY employee_id
                                                                """,

                                                (rs, rowNum) -> rs.getString(
                                                                "employee_id"),

                                                trainingId));

                // --------------------------------------------------------
                // Registered employees
                // --------------------------------------------------------

                training.setRegisteredEmployeeIds(
                                jdbcTemplate.query(
                                                """
                                                                SELECT employee_id
                                                                FROM training_registrations
                                                                WHERE training_id = ?
                                                                ORDER BY employee_id
                                                                """,

                                                (rs, rowNum) -> rs.getString(
                                                                "employee_id"),

                                                trainingId));

                // --------------------------------------------------------
                // Attendance
                // --------------------------------------------------------

                Map<String, String> attendance = new HashMap<>();

                try {

                        List<Map<String, Object>> attendanceRows = jdbcTemplate.queryForList(
                                        """
                                                        SELECT
                                                            employee_id,
                                                            status
                                                        FROM training_attendance
                                                        WHERE training_id = ?
                                                        """,
                                        trainingId);

                        for (Map<String, Object> row : attendanceRows) {

                                Object employeeId = row.get("employee_id");

                                Object status = row.get("status");

                                if (employeeId != null) {

                                        attendance.put(
                                                        employeeId.toString(),
                                                        status != null
                                                                        ? status.toString()
                                                                        : null);
                                }
                        }

                } catch (Exception ignored) {

                        /*
                         * Do not stop the complete training response
                         * if attendance data cannot be loaded.
                         */
                }

                training.setAttendance(attendance);

                // --------------------------------------------------------
                // Completion
                // --------------------------------------------------------

                Map<String, String> completion = new HashMap<>();

                try {

                        List<Map<String, Object>> completionRows = jdbcTemplate.queryForList(
                                        """
                                                        SELECT
                                                            employee_id,
                                                            status
                                                        FROM training_completion
                                                        WHERE training_id = ?
                                                        """,
                                        trainingId);

                        for (Map<String, Object> row : completionRows) {

                                Object employeeId = row.get("employee_id");

                                Object status = row.get("status");

                                if (employeeId != null) {

                                        completion.put(
                                                        employeeId.toString(),
                                                        status != null
                                                                        ? status.toString()
                                                                        : null);
                                }
                        }

                } catch (Exception ignored) {

                        /*
                         * Do not stop the complete training response
                         * if completion data cannot be loaded.
                         */
                }

                training.setCompletion(completion);
        }

        // ============================================================
        // MAP DATABASE ROW
        // ============================================================

        private TrainingProgram mapTraining(
                        ResultSet resultSet) throws SQLException {

                TrainingProgram training = new TrainingProgram();

                training.setId(
                                resultSet.getLong("id"));

                training.setTitle(
                                resultSet.getString("title"));

                training.setDescription(
                                resultSet.getString("description"));

                training.setTrainer(
                                resultSet.getString("trainer"));

                training.setCategory(
                                resultSet.getString("category"));

                if (resultSet.getDate("start_date") != null) {

                        training.setStartDate(
                                        resultSet
                                                        .getDate("start_date")
                                                        .toLocalDate());
                }

                if (resultSet.getDate("end_date") != null) {

                        training.setEndDate(
                                        resultSet
                                                        .getDate("end_date")
                                                        .toLocalDate());
                }

                training.setLocation(
                                resultSet.getString("location"));

                training.setCapacity(
                                resultSet.getInt("capacity"));

                training.setTrainingFor(
                                getTrainingFor(
                                                resultSet.getString(
                                                                "training_for")));

                training.setStatus(
                                resultSet.getString("status"));

                return training;
        }

        // ============================================================
        // CONVERT SQL SERVER JSON -> List<String>
        // ============================================================

        private List<String> getTrainingFor(
                        String trainingForJson) {

                List<String> result = new ArrayList<>();

                if (trainingForJson == null ||
                                trainingForJson.isBlank()) {

                        return result;
                }

                String json = trainingForJson.trim();

                /*
                 * Expected SQL Server value:
                 *
                 * ["Engineering"]
                 *
                 * or
                 *
                 * ["Engineering","Human Resources","IT"]
                 */

                if (json.startsWith("[") &&
                                json.endsWith("]")) {

                        json = json.substring(
                                        1,
                                        json.length() - 1).trim();
                }

                if (json.isBlank()) {
                        return result;
                }

                /*
                 * Split the simple JSON array.
                 *
                 * Department names in StaffHub do not contain
                 * commas, so this is sufficient for the current
                 * database structure.
                 */

                String[] values = json.split(",");

                for (String value : values) {

                        String cleaned = value.trim();

                        if (cleaned.startsWith("\"")) {

                                cleaned = cleaned.substring(1);
                        }

                        if (cleaned.endsWith("\"")) {

                                cleaned = cleaned.substring(
                                                0,
                                                cleaned.length() - 1);
                        }

                        cleaned = cleaned
                                        .replace("\\\"", "\"")
                                        .replace("\\\\", "\\");

                        if (!cleaned.isBlank()) {

                                result.add(cleaned);
                        }
                }

                return result;
        }

        // ============================================================
        // CONVERT List<String> -> SQL SERVER JSON
        // ============================================================

        private String toTrainingForJson(
                        List<String> trainingFor) {

                if (trainingFor == null ||
                                trainingFor.isEmpty()) {

                        return "[]";
                }

                StringBuilder json = new StringBuilder("[");

                for (int i = 0; i < trainingFor.size(); i++) {

                        if (i > 0) {
                                json.append(",");
                        }

                        String department = trainingFor.get(i);

                        if (department == null) {
                                department = "";
                        }

                        String escaped = department
                                        .replace("\\", "\\\\")
                                        .replace("\"", "\\\"");

                        json.append("\"")
                                        .append(escaped)
                                        .append("\"");
                }

                json.append("]");

                return json.toString();
        }
}
