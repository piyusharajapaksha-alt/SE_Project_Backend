package com.staffhub.controller;

import com.staffhub.model.AttendanceSchedule;
import com.staffhub.repository.AttendanceScheduleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance/schedules")
@CrossOrigin
public class AttendanceScheduleController {

    private final AttendanceScheduleRepository repository;

    public AttendanceScheduleController(
            AttendanceScheduleRepository repository
    ) {
        this.repository = repository;
    }

    @GetMapping
    public List<AttendanceSchedule> all() {
        return repository.findAll();
    }

    @PostMapping
    public AttendanceSchedule create(
            @RequestBody AttendanceSchedule schedule
    ) {

        repository.create(schedule);

        return schedule;
    }

    @PutMapping("/{id}")
    public AttendanceSchedule update(
            @PathVariable Long id,
            @RequestBody AttendanceSchedule schedule
    ) {

        repository.update(
                id,
                schedule
        );

        schedule.setId(id);

        return schedule;
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) {

        repository.delete(id);
    }
}