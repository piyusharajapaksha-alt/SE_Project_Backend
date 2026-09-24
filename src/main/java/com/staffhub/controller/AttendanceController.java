package com.staffhub.controller;

import com.staffhub.model.AttendanceMonitor;
import com.staffhub.model.AttendanceRecord;
import com.staffhub.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(
            AttendanceService service
    ) {
        this.service = service;
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
                body.get("user"),
                body.getOrDefault(
                        "type",
                        "MANUAL"
                )
        );
    }

    @PostMapping("/monitor/deactivate")
    public void deactivate(
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

        service.deactivate(
                user,
                "MANUAL"
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

        Long employeeId =
                Long.valueOf(
                        body.get("employeeId")
                                .toString()
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
    public void correct(
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
    }
}