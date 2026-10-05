package com.staffhub.service;

import com.staffhub.model.TrainingProgram;
import com.staffhub.repository.TrainingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public TrainingService(
            TrainingRepository trainingRepository
    ) {
        this.trainingRepository = trainingRepository;
    }


    // ============================================================
    // GET ALL
    // ============================================================

    public List<TrainingProgram> getAllTrainingPrograms() {

        return trainingRepository.findAll();
    }


    // ============================================================
    // GET ONE
    // ============================================================

    public TrainingProgram getTrainingProgramById(
            Long id
    ) {

        return trainingRepository.findById(id);
    }


    // ============================================================
    // CREATE
    // ============================================================

    @Transactional
    public TrainingProgram createTrainingProgram(
            TrainingProgram training
    ) {

        validateTraining(training);

        /*
         * Normalize department names before saving.
         *
         * Example:
         *
         * " HR "
         * "hr"
         * "HR"
         *
         * become one value:
         *
         * "HR"
         */
        training.setTrainingFor(
                normalizeDepartments(
                        training.getTrainingFor()
                )
        );

        TrainingProgram created =
                trainingRepository.create(training);

        /*
         * Automatically assign every employee
         * belonging to the selected departments.
         */
        synchronizeDepartmentAssignments(
                created.getId(),
                created.getTrainingFor()
        );

        /*
         * Reload the training so the response contains
         * the final assignment list.
         */
        return trainingRepository.findById(
                created.getId()
        );
    }


    // ============================================================
    // UPDATE
    // ============================================================

    @Transactional
    public TrainingProgram updateTrainingProgram(
            Long id,
            TrainingProgram training
    ) {

        validateTraining(training);

        TrainingProgram existing =
                trainingRepository.findById(id);

        if (existing == null) {

            throw new IllegalArgumentException(
                    "Training program not found"
            );
        }

        /*
         * Do not allow capacity to become lower than
         * the current number of registrations.
         */
        int registeredCount =
                trainingRepository.countRegistrations(id);

        if (training.getCapacity() < registeredCount) {

            throw new IllegalArgumentException(
                    "Capacity cannot be lower than the current number of registered employees"
            );
        }

        /*
         * Normalize departments before saving.
         */
        training.setTrainingFor(
                normalizeDepartments(
                        training.getTrainingFor()
                )
        );

        TrainingProgram updated =
                trainingRepository.update(
                        id,
                        training
                );

        /*
         * Synchronize employee assignments.
         *
         * This does two things:
         *
         * 1. Removes employees belonging to departments
         *    that are no longer selected.
         *
         * 2. Adds all employees belonging to the
         *    newly/currently selected departments.
         */
        synchronizeDepartmentAssignments(
                id,
                updated.getTrainingFor()
        );

        /*
         * Reload the training so the frontend receives
         * the final assignment list.
         */
        return trainingRepository.findById(id);
    }


    // ============================================================
    // DELETE
    // ============================================================

    @Transactional
    public void deleteTrainingProgram(
            Long id
    ) {

        TrainingProgram existing =
                trainingRepository.findById(id);

        if (existing == null) {

            throw new IllegalArgumentException(
                    "Training program not found"
            );
        }

        trainingRepository.delete(id);
    }


    // ============================================================
    // GET TRAINING EMPLOYEES
    // ============================================================

    public List<Map<String, Object>> getTrainingEmployees(
            Long trainingId
    ) {

        ensureTrainingExists(trainingId);

        return trainingRepository.findTrainingEmployees(
                trainingId
        );
    }


    // ============================================================
    // ASSIGN EMPLOYEES
    // ============================================================

    @Transactional
    public void assignEmployees(
            Long trainingId,
            List<String> employeeIds
    ) {

        ensureTrainingExists(trainingId);

        if (employeeIds == null ||
                employeeIds.isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one employee must be selected"
            );
        }

        for (String employeeId : employeeIds) {

            if (employeeId == null ||
                    employeeId.trim().isEmpty()) {

                continue;
            }

            String normalized =
                    employeeId.trim();

            if (!trainingRepository.employeeExists(
                    normalized
            )) {

                throw new IllegalArgumentException(
                        "Employee not found: " + normalized
                );
            }

            trainingRepository.assignEmployee(
                    trainingId,
                    normalized
            );
        }
    }


    // ============================================================
    // REMOVE ASSIGNMENT
    // ============================================================

    @Transactional
    public void removeEmployeeAssignment(
            Long trainingId,
            String employeeId
    ) {

        ensureTrainingExists(trainingId);

        if (employeeId == null ||
                employeeId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        trainingRepository.removeEmployeeAssignment(
                trainingId,
                employeeId.trim()
        );
    }


    // ============================================================
    // REGISTER EMPLOYEE
    // ============================================================

    @Transactional
    public void registerEmployee(
            Long trainingId,
            String employeeId
    ) {

        ensureTrainingExists(trainingId);

        if (employeeId == null ||
                employeeId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        String normalized =
                employeeId.trim();

        if (!trainingRepository.employeeExists(
                normalized
        )) {

            throw new IllegalArgumentException(
                    "Employee not found: " + normalized
            );
        }

        TrainingProgram training =
                trainingRepository.findById(
                        trainingId
                );

        if ("Cancelled".equals(
                training.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Cannot register for a cancelled training"
            );
        }

        if ("Completed".equals(
                training.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Cannot register for a completed training"
            );
        }

        if (trainingRepository.isEmployeeRegistered(
                trainingId,
                normalized
        )) {

            throw new IllegalArgumentException(
                    "Employee is already registered for this training"
            );
        }

        int registeredCount =
                trainingRepository.countRegistrations(
                        trainingId
                );

        if (registeredCount >= training.getCapacity()) {

            throw new IllegalArgumentException(
                    "Training capacity is full"
            );
        }

        trainingRepository.registerEmployee(
                trainingId,
                normalized
        );
    }


    // ============================================================
    // UNREGISTER EMPLOYEE
    // ============================================================

    @Transactional
    public void unregisterEmployee(
            Long trainingId,
            String employeeId
    ) {

        ensureTrainingExists(trainingId);

        if (employeeId == null ||
                employeeId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        trainingRepository.unregisterEmployee(
                trainingId,
                employeeId.trim()
        );
    }


    // ============================================================
    // SYNCHRONIZE DEPARTMENT ASSIGNMENTS
    // ============================================================

    private void synchronizeDepartmentAssignments(
            Long trainingId,
            List<String> trainingFor
    ) {

        /*
         * Always normalize before querying the database.
         */
        List<String> departments =
                normalizeDepartments(trainingFor);

        /*
         * Remove assignments belonging to departments
         * that are no longer selected.
         *
         * Example:
         *
         * OLD:
         * IT + HR
         *
         * NEW:
         * IT
         *
         * HR employees are removed.
         */
        trainingRepository.removeAssignmentsOutsideDepartments(
                trainingId,
                departments
        );

        /*
         * Find ALL employees belonging to the selected
         * departments.
         *
         * Repository performs case-insensitive and
         * whitespace-safe department matching.
         */
        List<String> employeeIds =
                trainingRepository.findEmployeeNumbersByDepartments(
                        departments
                );

        /*
         * Assign every matching employee.
         *
         * The repository already prevents duplicate
         * training assignments.
         */
        for (String employeeId : employeeIds) {

            if (employeeId == null ||
                    employeeId.trim().isEmpty()) {

                continue;
            }

            trainingRepository.assignEmployee(
                    trainingId,
                    employeeId.trim()
            );
        }
    }


    // ============================================================
    // NORMALIZE DEPARTMENTS
    // ============================================================

    private List<String> normalizeDepartments(
            List<String> departments
    ) {

        List<String> normalized =
                new ArrayList<>();

        if (departments == null ||
                departments.isEmpty()) {

            return normalized;
        }

        for (String department : departments) {

            if (department == null) {
                continue;
            }

            String value =
                    department.trim();

            if (value.isEmpty()) {
                continue;
            }

            /*
             * Keep the first selected spelling for storage,
             * but compare department names case-insensitively.
             *
             * Example:
             *
             * HR
             * hr
             * HR
             *
             * becomes:
             *
             * HR
             */
            boolean alreadyExists =
                    normalized.stream()
                            .anyMatch(
                                    existing ->
                                            existing.equalsIgnoreCase(
                                                    value
                                            )
                            );

            if (!alreadyExists) {

                normalized.add(value);
            }
        }

        return normalized;
    }


    // ============================================================
    // VALIDATION
    // ============================================================

    private void validateTraining(
            TrainingProgram training
    ) {

        if (training == null) {

            throw new IllegalArgumentException(
                    "Training data is required"
            );
        }


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


        if (training.getTrainingFor() == null ||
                training.getTrainingFor().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one department must be selected"
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

        if (!validStatuses.contains(
                training.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Invalid training status"
            );
        }
    }


    // ============================================================
    // EXISTENCE CHECK
    // ============================================================

    private void ensureTrainingExists(
            Long id
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Training ID is required"
            );
        }

        TrainingProgram training =
                trainingRepository.findById(id);

        if (training == null) {

            throw new IllegalArgumentException(
                    "Training program not found"
            );
        }
    }
}