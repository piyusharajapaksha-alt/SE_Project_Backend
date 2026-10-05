package com.staffhub.service;

import com.staffhub.model.Department;
import com.staffhub.repository.DepartmentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final CompanyContextService companyContextService;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            CompanyContextService companyContextService
    ) {

        this.departmentRepository =
                departmentRepository;

        this.companyContextService =
                companyContextService;
    }

    // ==========================================================
    // GET CURRENT COMPANY DEPARTMENTS
    // ==========================================================

    public List<Department> getDepartments() {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        return departmentRepository.findAll(
                companyId
        );
    }

    // ==========================================================
    // CREATE DEPARTMENT
    // ==========================================================

    @Transactional
    public Department createDepartment(
            String name
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        if (
                name == null ||
                name.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Department name is required"
            );
        }

        String cleanName =
                name.trim();

        if (cleanName.length() > 100) {

            throw new IllegalArgumentException(
                    "Department name cannot exceed 100 characters"
            );
        }

        return departmentRepository.create(
                cleanName,
                companyId
        );
    }

    // ==========================================================
    // DELETE DEPARTMENT
    // ==========================================================

    @Transactional
    public void deleteDepartment(
            Long id
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Department ID is required"
            );
        }

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        Department department =
                departmentRepository.findById(
                        id,
                        companyId
                );

        if (department == null) {

            throw new IllegalArgumentException(
                    "Department not found"
            );
        }

        if (!department.isActive()) {

            throw new IllegalArgumentException(
                    "Department is already inactive"
            );
        }

        int employeeCount =
                departmentRepository
                        .countEmployeesUsingDepartment(
                                department.getName(),
                                companyId
                        );

        if (employeeCount > 0) {

            throw new IllegalArgumentException(
                    "Cannot delete "
                            + department.getName()
                            + " because "
                            + employeeCount
                            + " employee(s) are assigned to it. "
                            + "Reassign those employees first."
            );
        }

        int deleted =
                departmentRepository.delete(
                        id,
                        companyId
                );

        if (deleted == 0) {

            throw new IllegalArgumentException(
                    "Unable to delete department"
            );
        }
    }

    // ==========================================================
    // VALIDATE ONE DEPARTMENT
    // ==========================================================

    public String requireDepartment(
            String department
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        String canonical =
                departmentRepository
                        .findCanonicalName(
                                department,
                                companyId
                        );

        if (canonical == null) {

            throw new IllegalArgumentException(
                    "Selected department does not exist"
            );
        }

        return canonical;
    }

    // ==========================================================
    // VALIDATE MULTIPLE DEPARTMENTS
    // ==========================================================

    public List<String> requireDepartments(
            List<String> departments
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        return departmentRepository
                .findCanonicalNames(
                        departments,
                        companyId
                );
    }
}