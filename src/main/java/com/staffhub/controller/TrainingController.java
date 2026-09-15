package com.staffhub.controller;

import com.staffhub.model.TrainingProgram;
import com.staffhub.service.TrainingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    // GET ALL TRAINING PROGRAMS
    // GET /api/training
    // ============================================================

    @GetMapping
    public List<TrainingProgram> getAllTrainingPrograms() {

        return trainingService.getAllTrainingPrograms();
    }


    // ============================================================
    // GET ONE TRAINING PROGRAM
    // GET /api/training/{id}
    // ============================================================

    @GetMapping("/{id}")
    public TrainingProgram getTrainingProgramById(
            @PathVariable Long id
    ) {

        return trainingService.getTrainingProgramById(id);
    }


    // ============================================================
    // CREATE TRAINING PROGRAM
    // POST /api/training
    // ============================================================

    @PostMapping
    public TrainingProgram createTrainingProgram(
            @RequestBody TrainingProgram training
    ) {

        return trainingService.createTrainingProgram(
                training
        );
    }


    // ============================================================
    // UPDATE TRAINING PROGRAM
    // PUT /api/training/{id}
    // ============================================================

    @PutMapping("/{id}")
    public TrainingProgram updateTrainingProgram(
            @PathVariable Long id,
            @RequestBody TrainingProgram training
    ) {

        return trainingService.updateTrainingProgram(
                id,
                training
        );
    }


    // ============================================================
    // DELETE TRAINING PROGRAM
    // DELETE /api/training/{id}
    // ============================================================

    @DeleteMapping("/{id}")
    public void deleteTrainingProgram(
            @PathVariable Long id
    ) {

        trainingService.deleteTrainingProgram(id);
    }


    // ============================================================
    // GET TRAINING EMPLOYEES
    // GET /api/training/{id}/employees
    // ============================================================

    @GetMapping("/{id}/employees")
    public List<Map<String, Object>> getTrainingEmployees(
            @PathVariable Long id
    ) {

        return trainingService.getTrainingEmployees(id);
    }


    // ============================================================
    // ASSIGN EMPLOYEES
    // POST /api/training/{id}/employees
    // ============================================================

    @PostMapping("/{id}/employees")
    public void assignEmployees(
            @PathVariable Long id,
            @RequestBody List<String> employeeIds
    ) {

        trainingService.assignEmployees(
                id,
                employeeIds
        );
    }


    // ============================================================
    // REMOVE EMPLOYEE ASSIGNMENT
    // DELETE /api/training/{id}/employees/{employeeId}
    // ============================================================

    @DeleteMapping(
            "/{id}/employees/{employeeId}"
    )
    public void removeEmployeeAssignment(
            @PathVariable Long id,
            @PathVariable String employeeId
    ) {

        trainingService.removeEmployeeAssignment(
                id,
                employeeId
        );
    }


    // ============================================================
    // REGISTER EMPLOYEE
    // POST /api/training/{id}/register
    // ============================================================

    @PostMapping("/{id}/register")
    public void registerEmployee(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {

        String employeeId =
                request.get("employeeId");

        trainingService.registerEmployee(
                id,
                employeeId
        );
    }


    // ============================================================
    // UNREGISTER EMPLOYEE
    // DELETE /api/training/{id}/register/{employeeId}
    // ============================================================

    @DeleteMapping(
            "/{id}/register/{employeeId}"
    )
    public void unregisterEmployee(
            @PathVariable Long id,
            @PathVariable String employeeId
    ) {

        trainingService.unregisterEmployee(
                id,
                employeeId
        );
    }
}