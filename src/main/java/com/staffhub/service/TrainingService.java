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
}