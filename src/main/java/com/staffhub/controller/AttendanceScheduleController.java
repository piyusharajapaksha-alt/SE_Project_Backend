package com.staffhub.controller;

import com.staffhub.model.AttendanceSchedule;
import com.staffhub.repository.AttendanceScheduleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}")
    public AttendanceSchedule get(
            @PathVariable Long id
    ) {

        AttendanceSchedule schedule =
                repository.findById(id);

        if (schedule == null) {
            throw new IllegalArgumentException(
                    "Attendance schedule not found"
            );
        }

        return schedule;
    }

    @PostMapping
    public AttendanceSchedule create(
            @RequestBody AttendanceSchedule schedule
    ) {

        repository.create(schedule);

        return repository.findAll()
                .stream()
                .filter(s ->
                        s.getScheduleName()
                                .equals(
                                        schedule.getScheduleName()
                                )
                )
                .findFirst()
                .orElse(schedule);
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

        return repository.findById(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(
            @PathVariable Long id
    ) {

        repository.delete(id);

        return Map.of(
                "message",
                "Attendance schedule deleted successfully"
        );
    }
}