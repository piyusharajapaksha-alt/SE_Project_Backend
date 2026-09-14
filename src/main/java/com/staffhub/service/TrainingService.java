package com.staffhub.service;

import com.staffhub.model.TrainingProgram;
import com.staffhub.repository.TrainingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public TrainingService(
            TrainingRepository trainingRepository
    ) {
        this.trainingRepository = trainingRepository;
    }


    // ============================================================
    // Get all training programs
    // ============================================================

    public List<TrainingProgram> getAllTrainingPrograms() {

        return trainingRepository.findAll();
    }


    // ============================================================
    // Get one training program
    // ============================================================

    public TrainingProgram getTrainingProgramById(Long id) {

        return trainingRepository.findById(id);
    }


    // ============================================================
    // Create training program
    // ============================================================

    public TrainingProgram createTrainingProgram(
            TrainingProgram training
    ) {

        validateTraining(training);

        return trainingRepository.create(training);
    }


    // ============================================================
    // Validation
    // ============================================================

    private void validateTraining(
            TrainingProgram training
    ) {

        if (training.getTitle() == null ||
                training.getTitle().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Training title is required"
            );
        }


        if (training.getTrainer() == null ||
                training.getTrainer().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Trainer is required"
            );
        }


        if (training.getCategory() == null ||
                training.getCategory().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Category is required"
            );
        }


        if (training.getStartDate() == null) {

            throw new IllegalArgumentException(
                    "Start date is required"
            );
        }


        if (training.getEndDate() != null &&
                training.getEndDate()
                        .isBefore(training.getStartDate())) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }


        if (training.getLocation() == null ||
                training.getLocation().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Location is required"
            );
        }


        if (training.getCapacity() == null ||
                training.getCapacity() <= 0) {

            throw new IllegalArgumentException(
                    "Capacity must be greater than 0"
            );
        }


        if (training.getStatus() == null ||
                training.getStatus().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Status is required"
            );
        }


        List<String> validStatuses = List.of(
                "Upcoming",
                "Ongoing",
                "Completed",
                "Cancelled"
        );

        if (!validStatuses.contains(training.getStatus())) {

            throw new IllegalArgumentException(
                    "Invalid training status"
            );
        }
    }
}