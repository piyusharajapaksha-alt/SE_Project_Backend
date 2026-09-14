package com.staffhub.repository;

import com.staffhub.model.TrainingProgram;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TrainingRepository {

    private final JdbcTemplate jdbcTemplate;

    public TrainingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // ============================================================
    // Get all training programs
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

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> {

            TrainingProgram training = new TrainingProgram();

            training.setId(resultSet.getLong("id"));
            training.setTitle(resultSet.getString("title"));
            training.setDescription(resultSet.getString("description"));
            training.setTrainer(resultSet.getString("trainer"));
            training.setCategory(resultSet.getString("category"));

            if (resultSet.getDate("start_date") != null) {
                training.setStartDate(
                        resultSet.getDate("start_date").toLocalDate()
                );
            }

            if (resultSet.getDate("end_date") != null) {
                training.setEndDate(
                        resultSet.getDate("end_date").toLocalDate()
                );
            }

            training.setLocation(resultSet.getString("location"));
            training.setCapacity(resultSet.getInt("capacity"));

            training.setTrainingFor(
                    getTrainingFor(resultSet.getArray("training_for"))
            );

            training.setStatus(resultSet.getString("status"));

            return training;
        });
    }


    // ============================================================
    // Get one training program by ID
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

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, rowNumber) -> {

                    TrainingProgram training = new TrainingProgram();

                    training.setId(resultSet.getLong("id"));
                    training.setTitle(resultSet.getString("title"));
                    training.setDescription(resultSet.getString("description"));
                    training.setTrainer(resultSet.getString("trainer"));
                    training.setCategory(resultSet.getString("category"));

                    if (resultSet.getDate("start_date") != null) {
                        training.setStartDate(
                                resultSet.getDate("start_date").toLocalDate()
                        );
                    }

                    if (resultSet.getDate("end_date") != null) {
                        training.setEndDate(
                                resultSet.getDate("end_date").toLocalDate()
                        );
                    }

                    training.setLocation(resultSet.getString("location"));
                    training.setCapacity(resultSet.getInt("capacity"));

                    training.setTrainingFor(
                            getTrainingFor(resultSet.getArray("training_for"))
                    );

                    training.setStatus(resultSet.getString("status"));

                    return training;
                },
                id
        );
    }


    // ============================================================
    // Create training program
    // ============================================================

    public TrainingProgram create(TrainingProgram training) {

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

        Long generatedId = jdbcTemplate.query(
                connection -> {

                    PreparedStatement statement =
                            connection.prepareStatement(sql);

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
                (resultSet) -> {

                    if (resultSet.next()) {
                        return resultSet.getLong("id");
                    }

                    throw new IllegalStateException(
                            "Failed to create training program"
                    );
                }
        );

        training.setId(generatedId);

        return training;
    }


    // ============================================================
    // Update training program
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

        int rowsUpdated = jdbcTemplate.update(
                connection -> {

                    PreparedStatement statement =
                            connection.prepareStatement(sql);

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

        return training;
    }


    // ============================================================
    // Convert PostgreSQL TEXT[] → Java List<String>
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
                    result.add(value.toString());
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