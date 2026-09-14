package com.staffhub.controller;

import com.staffhub.model.TrainingProgram;
import com.staffhub.service.TrainingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training")
@CrossOrigin
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(
            TrainingService trainingService
    ) {
        this.trainingService = trainingService;
    }


    // ============================================================
    // GET /api/training
    // ============================================================

    @GetMapping
    public List<TrainingProgram> getAllTrainingPrograms() {

        return trainingService.getAllTrainingPrograms();
    }


    // ============================================================
    // GET /api/training/{id}
    // ============================================================

    @GetMapping("/{id}")
    public TrainingProgram getTrainingProgramById(
            @PathVariable Long id
    ) {

        return trainingService.getTrainingProgramById(id);
    }


    // ============================================================
    // POST /api/training
    // ============================================================

    @PostMapping
    public TrainingProgram createTrainingProgram(
            @RequestBody TrainingProgram training
    ) {

        return trainingService.createTrainingProgram(training);
    }
}