package com.staffhub.controller;

import com.staffhub.model.AttendanceEvent;
import com.staffhub.model.AttendanceMonitor;
import com.staffhub.model.AttendanceRecord;
import com.staffhub.service.AttendanceService;
import com.staffhub.repository.AttendanceMonitorRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin
public class AttendanceController {

    private final AttendanceService service;

    private final AttendanceMonitorRepository monitorRepository;

    public AttendanceController(
            AttendanceService service,
            AttendanceMonitorRepository monitorRepository
    ) {
        this.service = service;
        this.monitorRepository = monitorRepository;
    }

    @GetMapping("/monitor")
    public AttendanceMonitor monitor() {

        return service.getMonitor();
    }

    @PostMapping("/monitor/activate")
    public AttendanceMonitor activate(
            @RequestBody Map<String, String> body
    ) {

        return service.activate(
                body.get("code"),
                body.getOrDefault(
                        "user",
                        "SYSTEM"
                ),
                body.getOrDefault(
                        "type",
                        "MANUAL"
                )
        );
    }

    @PostMapping("/monitor/deactivate")
    public Map<String, String> deactivate(
            @RequestBody(required = false)
            Map<String, String> body
    ) {

        String user =
                body == null
                        ? "SYSTEM"
                        : body.getOrDefault(
                                "user",
                                "SYSTEM"
                        );

        String type =
                body == null
                        ? "MANUAL"
                        : body.getOrDefault(
                                "type",
                                "MANUAL"
                        );

        service.deactivate(
                user,
                type
        );

        return Map.of(
                "message",
                "Attendance monitor deactivated successfully"
        );
    }

    @PostMapping("/monitor/rotate")
    public AttendanceMonitor rotate() {

        return service.rotateQr();
    }

    @PostMapping("/scan")
    public AttendanceRecord scan(
            @RequestBody Map<String, Object> body
    ) {

        if (body == null
                || body.get("employeeId") == null
                || body.get("token") == null) {

            throw new IllegalArgumentException(
                    "employeeId and token are required"
            );
        }

        Long employeeId =
                Long.valueOf(
                        body.get(
                                "employeeId"
                        ).toString()
                );

        String token =
                body.get("token")
                        .toString();

        return service.scan(
                employeeId,
                token
        );
    }

    @GetMapping("/employee/{employeeId}/today")
    public AttendanceRecord today(
            @PathVariable Long employeeId
    ) {

        return service.today(employeeId);
    }

    @GetMapping("/employee/{employeeId}")
    public List<AttendanceRecord> history(
            @PathVariable Long employeeId
    ) {

        return service.history(
                employeeId
        );
    }

    @GetMapping("/records")
    public List<AttendanceRecord> records(
            @RequestParam(required = false)
            String date
    ) {

        LocalDate selectedDate =
                date == null
                        ? LocalDate.now()
                        : LocalDate.parse(date);

        return service.records(
                selectedDate
        );
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(
            @RequestParam(required = false)
            String date
    ) {

        LocalDate selectedDate =
                date == null
                        ? LocalDate.now()
                        : LocalDate.parse(date);

        return service.summary(
                selectedDate
        );
    }

    @PutMapping("/records/{id}")
    public Map<String, String> correct(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        service.correct(
                id,
                body.get("checkIn"),
                body.get("checkOut"),
                body.get("status"),
                body.get("reason")
        );

        return Map.of(
                "message",
                "Attendance record corrected successfully"
        );
    }

    /**
     * Activity/audit log for Attendance Management.
     *
     * Example:
     * GET /api/attendance/events
     * GET /api/attendance/events?limit=50
     */
    @GetMapping("/events")
    public List<AttendanceEvent> events(
            @RequestParam(
                    required = false,
                    defaultValue = "50"
            )
            int limit
    ) {

        return monitorRepository.findEvents(
                limit
        );
    }
}