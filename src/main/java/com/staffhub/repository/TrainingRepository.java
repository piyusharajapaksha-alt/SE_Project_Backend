package com.staffhub.repository;

import com.staffhub.model.TrainingProgram;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

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
                training.getStatus()
        );

        training.setId(generatedId);

        return training;
    }
}