package com.staffhub.service;

import com.staffhub.model.Grievance;
import com.staffhub.repository.GrievanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrievanceService {

    private final GrievanceRepository grievanceRepository;

    public GrievanceService(
            GrievanceRepository grievanceRepository
    ) {
        this.grievanceRepository = grievanceRepository;
    }

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

    public Grievance getById(Long id) {

        return grievanceRepository.findById(id);
    }

    public Long create(Grievance grievance) {

        if (grievance.getEmployeeId() == null
                || grievance.getEmployeeId().isBlank()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (grievance.getCategory() == null
                || grievance.getCategory().isBlank()) {

            throw new IllegalArgumentException(
                    "Category is required"
            );
        }

        if (grievance.getDescription() == null
                || grievance.getDescription().isBlank()) {

            throw new IllegalArgumentException(
                    "Description is required"
            );
        }

        if (grievance.getPriority() == null
                || grievance.getPriority().isBlank()) {

            grievance.setPriority("Medium");
        }

        return grievanceRepository.save(grievance);
    }

    public void updateStatus(
            Long id,
            String status,
            String updatedBy
    ) {

        grievanceRepository.updateStatus(
                id,
                status,
                updatedBy
        );
    }

    public void addResponse(
            Long grievanceId,
            String employeeId,
            String text
    ) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Response cannot be empty"
            );
        }

        grievanceRepository.addResponse(
                grievanceId,
                employeeId,
                text
        );
    }
}