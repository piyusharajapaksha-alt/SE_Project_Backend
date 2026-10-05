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

    private final DepartmentService departmentService;

    private final CompanyContextService companyContextService;


    public TrainingService(
            TrainingRepository trainingRepository,
            DepartmentService departmentService,
            CompanyContextService companyContextService
    ) {

        this.trainingRepository =
                trainingRepository;

        this.departmentService =
                departmentService;

        this.companyContextService =
                companyContextService;
    }


    // ============================================================
    // GET ALL
    // ============================================================

    public List<TrainingProgram> getAllTrainingPrograms() {

        List<TrainingProgram> programs =
                trainingRepository.findAll();

        /*
         * Existing training rows are normalized here too.
         * This keeps old data compatible after the department
         * table is introduced.
         */
        for (TrainingProgram program : programs) {

            program.setTrainingFor(
                    normalizeDepartments(
                            program.getTrainingFor()
                    )
            );
        }

        return programs;
    }


    // ============================================================
    // GET ONE
    // ============================================================

    public TrainingProgram getTrainingProgramById(
            Long id
    ) {

        TrainingProgram program =
                trainingRepository.findById(id);

        if (program == null) {
            return null;
        }

        program.setTrainingFor(
                normalizeDepartments(
                        program.getTrainingFor()
                )
        );

        return program;
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
         * Convert whatever React sent into the exact
         * department names stored in the department table.
         */
        training.setTrainingFor(
                normalizeDepartments(
                        training.getTrainingFor()
                )
        );

        TrainingProgram created =
                trainingRepository.create(
                        training
                );

        synchronizeDepartmentAssignments(
                created.getId(),
                created.getTrainingFor()
        );

        return getTrainingProgramById(
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

        int registeredCount =
                trainingRepository
                        .countRegistrations(id);

        if (
                training.getCapacity() <
                registeredCount
        ) {

            throw new IllegalArgumentException(
                    "Capacity cannot be lower than the current number of registered employees"
            );
        }

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

        synchronizeDepartmentAssignments(
                id,
                updated.getTrainingFor()
        );

        return getTrainingProgramById(id);
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

        return trainingRepository
                .findTrainingEmployees(
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

        if (
                employeeIds == null ||
                employeeIds.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "At least one employee must be selected"
            );
        }

        for (String employeeId : employeeIds) {

            if (
                    employeeId == null ||
                    employeeId.trim().isEmpty()
            ) {
                continue;
            }

            String normalized =
                    employeeId.trim();

            if (
                    !trainingRepository
                            .employeeExists(
                                    normalized
                            )
            ) {

                throw new IllegalArgumentException(
                        "Employee not found: "
                                + normalized
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

        if (
                employeeId == null ||
                employeeId.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        trainingRepository
                .removeEmployeeAssignment(
                        trainingId,
                        employeeId.trim()
                );
    }


    // ============================================================
    // REGISTER
    // ============================================================

    @Transactional
    public void registerEmployee(
            Long trainingId,
            String employeeId
    ) {

        ensureTrainingExists(trainingId);

        if (
                employeeId == null ||
                employeeId.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        String normalized =
                employeeId.trim();

        if (
                !trainingRepository
                        .employeeExists(
                                normalized
                        )
        ) {

            throw new IllegalArgumentException(
                    "Employee not found: "
                            + normalized
            );
        }

        TrainingProgram training =
                trainingRepository.findById(
                        trainingId
                );

        if (
                "Cancelled".equals(
                        training.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Cannot register for a cancelled training"
            );
        }

        if (
                "Completed".equals(
                        training.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Cannot register for a completed training"
            );
        }

        if (
                trainingRepository
                        .isEmployeeRegistered(
                                trainingId,
                                normalized
                        )
        ) {

            throw new IllegalArgumentException(
                    "Employee is already registered for this training"
            );
        }

        int registeredCount =
                trainingRepository
                        .countRegistrations(
                                trainingId
                        );

        if (
                registeredCount >=
                training.getCapacity()
        ) {

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
    // UNREGISTER
    // ============================================================

    @Transactional
    public void unregisterEmployee(
            Long trainingId,
            String employeeId
    ) {

        ensureTrainingExists(trainingId);

        if (
                employeeId == null ||
                employeeId.trim().isEmpty()
        ) {

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
    // AUTOMATIC DEPARTMENT ASSIGNMENT
    // ============================================================

    private void synchronizeDepartmentAssignments(
            Long trainingId,
            List<String> departments
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        List<String> normalizedDepartments =
                normalizeDepartments(
                        departments
                );

        /*
         * Remove employees who are no longer in
         * a selected department.
         */
        trainingRepository
                .removeAssignmentsOutsideDepartments(
                        trainingId,
                        normalizedDepartments,
                        companyId
                );

        /*
         * Find ALL employees belonging to the selected
         * departments in THIS company.
         */
        List<String> employeeIds =
                trainingRepository
                        .findEmployeeNumbersByDepartments(
                                normalizedDepartments,
                                companyId
                        );

        /*
         * Assign every matching employee.
         */
        for (String employeeId : employeeIds) {

            if (
                    employeeId == null ||
                    employeeId.trim().isEmpty()
            ) {
                continue;
            }

            trainingRepository.assignEmployee(
                    trainingId,
                    employeeId.trim()
            );
        }
    }


    // ============================================================
    // DEPARTMENT NORMALIZATION
    // ============================================================

    private List<String> normalizeDepartments(
            List<String> departments
    ) {

        if (
                departments == null ||
                departments.isEmpty()
        ) {

            return new ArrayList<>();
        }

        /*
         * DepartmentService checks the department table.
         *
         * There is NO:
         *
         * HR -> Human Resources
         * IT -> Engineering
         *
         * mapping here.
         *
         * The database decides the valid department names.
         */
        return departmentService
                .requireDepartments(
                        departments
                );
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

        if (
                training.getTitle() == null ||
                training.getTitle().trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Training title is required"
            );
        }

        if (
                training.getTrainer() == null ||
                training.getTrainer().trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Trainer is required"
            );
        }

        if (
                training.getCategory() == null ||
                training.getCategory().trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Category is required"
            );
        }

        if (
                training.getStartDate() == null
        ) {

            throw new IllegalArgumentException(
                    "Start date is required"
            );
        }

        if (
                training.getEndDate() != null &&
                training.getEndDate()
                        .isBefore(
                                training.getStartDate()
                        )
        ) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }

        if (
                training.getLocation() == null ||
                training.getLocation()
                        .trim()
                        .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Location is required"
            );
        }

        if (
                training.getCapacity() == null ||
                training.getCapacity() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Capacity must be greater than 0"
            );
        }

        if (
                training.getTrainingFor() == null ||
                training.getTrainingFor().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "At least one department must be selected"
            );
        }

        if (
                training.getStatus() == null ||
                training.getStatus().trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Status is required"
            );
        }

        List<String> validStatuses =
                List.of(
                        "Upcoming",
                        "Ongoing",
                        "Completed",
                        "Cancelled"
                );

        if (
                !validStatuses.contains(
                        training.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid training status"
            );
        }
    }


    // ============================================================
    // EXISTENCE
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