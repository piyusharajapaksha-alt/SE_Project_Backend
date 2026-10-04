package com.staffhub.service;

import com.staffhub.model.AttendanceSchedule;
import com.staffhub.repository.AttendanceScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceScheduleService {

    private final AttendanceScheduleRepository repository;
    private final CompanyContextService companyContextService;

    public AttendanceScheduleService(
            AttendanceScheduleRepository repository,
            CompanyContextService companyContextService
    ) {
        this.repository = repository;
        this.companyContextService =
                companyContextService;
    }

    public List<AttendanceSchedule> findAll() {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        return repository.findAll(
                companyId
        );
    }

    public AttendanceSchedule create(
            AttendanceSchedule schedule
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        repository.create(
                schedule,
                companyId
        );

        return repository.findLatest(
                companyId
        );
    }

    public AttendanceSchedule update(
            Long id,
            AttendanceSchedule schedule
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        repository.update(
                id,
                schedule,
                companyId
        );

        return repository.findById(
                id,
                companyId
        );
    }

    public void delete(Long id) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        repository.delete(
                id,
                companyId
        );
    }
}