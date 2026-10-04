package com.staffhub.controller;

import com.staffhub.model.AttendanceSchedule;
import com.staffhub.service.AttendanceScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance/schedules")
@CrossOrigin
public class AttendanceScheduleController {

    private final AttendanceScheduleService service;

    public AttendanceScheduleController(
            AttendanceScheduleService service
    ) {
        this.service = service;
    }

    /*
     * ============================================================
     * GET ALL SCHEDULES
     * ============================================================
     */

    @GetMapping
    public List<AttendanceSchedule> getAll() {

        return service.findAll();
    }

    /*
     * ============================================================
     * CREATE
     * ============================================================
     */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceSchedule create(
            @RequestBody AttendanceSchedule schedule
    ) {

        return service.create(
                schedule
        );
    }

    /*
     * ============================================================
     * UPDATE
     * ============================================================
     */

    @PutMapping("/{id}")
    public AttendanceSchedule update(
            @PathVariable Long id,
            @RequestBody AttendanceSchedule schedule
    ) {

        return service.update(
                id,
                schedule
        );
    }

    /*
     * ============================================================
     * DELETE
     * ============================================================
     */

    @DeleteMapping("/{id}")
    public Map<String, String> delete(
            @PathVariable Long id
    ) {

        service.delete(id);

        return Map.of(
                "message",
                "Attendance schedule deleted successfully"
        );
    }
}