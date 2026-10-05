package com.staffhub.service;

import com.staffhub.model.Grievance;
import com.staffhub.repository.GrievanceRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrievanceService {

    private final GrievanceRepository grievanceRepository;

    private final JdbcTemplate jdbcTemplate;

    private final CompanyContextService companyContextService;

    public GrievanceService(
            GrievanceRepository grievanceRepository,
            JdbcTemplate jdbcTemplate,
            CompanyContextService companyContextService
    ) {
        this.grievanceRepository =
                grievanceRepository;

        this.jdbcTemplate =
                jdbcTemplate;

        this.companyContextService =
                companyContextService;
    }

    // ============================================================
    // GET ALL
    // ============================================================

    public List<Grievance> getAll(
            String employeeId,
            String search,
            String status,
            String priority,
            String category
    ) {

        return grievanceRepository.findAll(
                employeeId,
                search,
                status,
                priority,
                category
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    public Grievance getById(
            Long id
    ) {

        return grievanceRepository.findById(id);
    }

    // ============================================================
    // CREATE
    // ============================================================

    public Long create(
            Grievance grievance
    ) {

        if (
                grievance.getEmployeeId() == null
                        || grievance.getEmployeeId().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (
                grievance.getCategory() == null
                        || grievance.getCategory().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Category is required"
            );
        }

        if (
                grievance.getDescription() == null
                        || grievance.getDescription().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Description is required"
            );
        }

        String description =
                grievance.getDescription().trim();

        if (description.length() < 20) {

            throw new IllegalArgumentException(
                    "Description must contain at least 20 characters"
            );
        }

        grievance.setEmployeeId(
                grievance.getEmployeeId().trim()
        );

        grievance.setCategory(
                grievance.getCategory().trim()
        );

        grievance.setDescription(
                description
        );

        if (
                grievance.getPriority() == null
                        || grievance.getPriority().isBlank()
        ) {

            grievance.setPriority("Medium");

        } else {

            grievance.setPriority(
                    grievance.getPriority().trim()
            );
        }

        return grievanceRepository.save(
                grievance
        );
    }

    // ============================================================
    // UPDATE OWN GRIEVANCE
    // ============================================================

    public void updateOwnGrievance(
            Long id,
            String employeeId,
            Grievance grievance
    ) {

        if (
                employeeId == null
                        || employeeId.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (
                grievance.getCategory() == null
                        || grievance.getCategory().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Category is required"
            );
        }

        if (
                grievance.getDescription() == null
                        || grievance.getDescription().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Description is required"
            );
        }

        String description =
                grievance.getDescription().trim();

        if (description.length() < 20) {

            throw new IllegalArgumentException(
                    "Description must contain at least 20 characters"
            );
        }

        String priority =
                grievance.getPriority();

        if (
                priority == null
                        || priority.isBlank()
        ) {

            priority = "Medium";
        }

        int updated =
                grievanceRepository.updateOwnGrievance(
                        id,
                        employeeId.trim(),
                        grievance.getCategory().trim(),
                        priority.trim(),
                        description
                );

        if (updated == 0) {

            throw new IllegalArgumentException(
                    "Grievance cannot be edited. It may not belong to this employee or may already be under processing."
            );
        }
    }

    // ============================================================
    // DELETE OWN GRIEVANCE
    // ============================================================

    public void deleteOwnGrievance(
            Long id,
            String employeeId
    ) {

        if (
                employeeId == null
                        || employeeId.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        int deleted =
                grievanceRepository.deleteOwnGrievance(
                        id,
                        employeeId.trim()
                );

        if (deleted == 0) {

            throw new IllegalArgumentException(
                    "Grievance cannot be deleted. It may not belong to this employee or may already be under processing."
            );
        }
    }

    // ============================================================
    // UPDATE STATUS
    // ============================================================

    public void updateStatus(
            Long id,
            String status,
            String assignedTo
    ) {

        if (
                status == null
                        || status.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Status is required"
            );
        }

        String cleanStatus =
                status.trim();

        String cleanAssignedTo =
                assignedTo == null
                        ? null
                        : assignedTo.trim();

        // --------------------------------------------------------
        // Assignment validation
        // --------------------------------------------------------

        if (
                cleanStatus.equalsIgnoreCase(
                        "Assigned"
                )
        ) {

            if (
                    cleanAssignedTo == null
                            || cleanAssignedTo.isBlank()
            ) {

                throw new IllegalArgumentException(
                        "An employee must be selected when assigning a grievance"
                );
            }

            validateAssignee(
                    cleanAssignedTo
            );
        }

        // --------------------------------------------------------
        // Prevent an arbitrary employee number from being
        // written when changing another status.
        // --------------------------------------------------------

        if (
                !cleanStatus.equalsIgnoreCase(
                        "Assigned"
                )
                && cleanAssignedTo != null
                && !cleanAssignedTo.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "An assignee can only be supplied when status is Assigned"
            );
        }

        int updated =
                grievanceRepository.updateStatus(
                        id,
                        cleanStatus,
                        cleanAssignedTo
                );

        if (updated == 0) {

            throw new IllegalArgumentException(
                    "Grievance not found or could not be updated"
            );
        }
    }

    // ============================================================
    // VALIDATE ASSIGNEE
    // ============================================================
    //
    // Only employees in the current company who are:
    //
    // - HR Manager
    // - Grievance Officer
    //
    // can be assigned a grievance.
    //
    // ============================================================

    private void validateAssignee(
            String employeeNumber
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM employees
                        WHERE employee_number = ?
                          AND company_id = ?
                          AND (
                              role = 'HR Manager'
                              OR role = 'Grievance Officer'
                          )
                        """,
                        Integer.class,
                        employeeNumber,
                        companyId
                );

        if (
                count == null
                        || count == 0
        ) {

            throw new IllegalArgumentException(
                    "Selected employee is not an eligible grievance officer or HR Manager in this company"
            );
        }
    }

    // ============================================================
    // ADD RESPONSE
    // ============================================================

    public void addResponse(
            Long grievanceId,
            String employeeId,
            String text
    ) {

        if (
                employeeId == null
                        || employeeId.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (
                text == null
                        || text.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Response cannot be empty"
            );
        }

        grievanceRepository.addResponse(
                grievanceId,
                employeeId.trim(),
                text.trim()
        );
    }
}