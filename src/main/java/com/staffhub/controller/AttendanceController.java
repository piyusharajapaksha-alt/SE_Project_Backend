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

    /*
     * QR MONITOR
     */

    @GetMapping("/monitor")
    public AttendanceMonitor monitor() {
        return service.getMonitor();
    }

    @PostMapping("/monitor/activate")
    public AttendanceMonitor activate(
            @RequestBody Map<String, String> body
    ) {

        String code = body.get("code");
        String user = body.get("user");
        String type = body.getOrDefault(
                "type",
                "MANUAL"
        );

        return service.activate(
                code,
                user,
                type
        );
    }

    @PostMapping("/monitor/deactivate")
    public void deactivate() {
        service.deactivate();
    }

    @PostMapping("/monitor/rotate")
    public AttendanceMonitor rotate() {
        return service.rotateQr();
    }

    /*
     * EMPLOYEE SCAN
     */

    @PostMapping("/scan")
    public AttendanceRecord scan(
            @RequestBody Map<String, Object> body
    ) {

        Long employeeId =
                Long.valueOf(
                        body.get("employeeId").toString()
                );

        String token =
                body.get("token").toString();

        return service.scan(
                employeeId,
                token
        );
    }

    /*
     * EMPLOYEE
     */

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
        return service.employeeHistory(employeeId);
    }

    /*
     * MANAGEMENT
     */

    @GetMapping("/records")
    public List<AttendanceRecord> records(
            @RequestParam(required = false) String date
    ) {

        LocalDate selectedDate =
                date == null
                        ? LocalDate.now()
                        : LocalDate.parse(date);

        return service.date(selectedDate);
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