package com.staffhub.service;

import com.staffhub.model.Department;
import com.staffhub.repository.DepartmentRepository;

import org.springframework.stereotype.Service;

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
    // VALIDATE DEPARTMENT
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