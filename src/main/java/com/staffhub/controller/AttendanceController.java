package com.staffhub.controller;

import com.staffhub.model.AttendanceEvent;
import com.staffhub.model.AttendanceMonitor;
import com.staffhub.model.AttendanceRecord;
import com.staffhub.model.AttendanceSchedule;
import com.staffhub.repository.AttendanceMonitorRepository;
import com.staffhub.service.AttendanceScheduleService;
import com.staffhub.service.AttendanceService;
import com.staffhub.service.CompanyContextService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    private final AttendanceMonitorRepository monitorRepository;

    private final AttendanceScheduleService scheduleService;

    private final CompanyContextService companyContextService;

    public AttendanceController(
            AttendanceService service,
            AttendanceMonitorRepository monitorRepository,
            AttendanceScheduleService scheduleService,
            CompanyContextService companyContextService
    ) {
        this.service = service;
        this.monitorRepository = monitorRepository;
        this.scheduleService = scheduleService;
        this.companyContextService = companyContextService;
    }

    // ============================================================
    // PUBLIC QR MONITOR
    // ============================================================

    @GetMapping("/monitor")
    public AttendanceMonitor monitor(
            @RequestParam(required = false)
            Long monitorId
    ) {
        return service.getPublicMonitor(
                monitorId
        );
    }

    // ============================================================
    // AUTHORIZE
    // ============================================================

    @PostMapping("/monitor/authorize")
    public AttendanceMonitor authorize(
            @RequestBody Map<String, String> body
    ) {

        if (
                body == null
                        || body.get("code") == null
        ) {
            throw new IllegalArgumentException(
                    "Activation code is required"
            );
        }

        return service.authorizeMonitor(
                body.get("code")
        );
    }

    // ============================================================
    // ACCEPTED MONITORS
    // ============================================================

    @GetMapping("/monitor/accepted")
    public List<AttendanceMonitor> acceptedMonitors() {

        return service.getAcceptedMonitors();
    }

    // ============================================================
    // ACTIVATE
    // ============================================================

    @PostMapping("/monitor/activate")
    public AttendanceMonitor activate(
            @RequestBody Map<String, Object> body
    ) {

        Long monitorId =
                readMonitorId(body);

        return service.activate(
                monitorId
        );
    }

    // ============================================================
    // DEACTIVATE
    // ============================================================

    @PostMapping("/monitor/deactivate")
    public Map<String, String> deactivate(
            @RequestBody Map<String, Object> body
    ) {

        Long monitorId =
                readMonitorId(body);

        service.deactivate(
                monitorId,
                "MANUAL"
        );

        return Map.of(
                "message",
                "Attendance monitor deactivated successfully"
        );
    }

    // ============================================================
    // REJECT
    // ============================================================

    @PostMapping("/monitor/reject")
    public Map<String, String> reject(
            @RequestBody Map<String, Object> body
    ) {

        Long monitorId =
                readMonitorId(body);

        service.rejectMonitor(
                monitorId
        );

        return Map.of(
                "message",
                "Attendance monitor rejected successfully"
        );
    }

    // ============================================================
    // ROTATE
    // ============================================================

    @PostMapping("/monitor/rotate")
    public AttendanceMonitor rotate(
            @RequestBody Map<String, Object> body
    ) {

        Long monitorId =
                readMonitorId(body);

        return service.rotateQr(
                monitorId
        );
    }

    // ============================================================
    // QR SCAN
    // ============================================================

    @PostMapping("/scan")
    public AttendanceRecord scan(
            @RequestBody Map<String, Object> body
    ) {

        if (
                body == null
                        || body.get("employeeId") == null
                        || body.get("monitorId") == null
                        || body.get("token") == null
        ) {
            throw new IllegalArgumentException(
                    "employeeId, monitorId and token are required"
            );
        }

        return service.scan(
                body.get("employeeId").toString(),
                Long.valueOf(
                        body.get("monitorId").toString()
                ),
                body.get("token").toString()
        );
    }

    // ============================================================
    // EMPLOYEE ATTENDANCE
    // ============================================================

    @GetMapping("/employee/{employeeId}/today")
    public AttendanceRecord today(
            @PathVariable String employeeId
    ) {

        return service.today(
                employeeId
        );
    }

    @GetMapping("/employee/{employeeId}")
    public List<AttendanceRecord> history(
            @PathVariable String employeeId
    ) {

        return service.history(
                employeeId
        );
    }

    // ============================================================
    // MANAGEMENT
    // ============================================================

    @GetMapping("/records")
    public List<AttendanceRecord> records(
            @RequestParam(required = false)
            String date
    ) {

        return service.records(
                date == null
                        ? LocalDate.now()
                        : LocalDate.parse(date)
        );
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(
            @RequestParam(required = false)
            String date
    ) {

        return service.summary(
                date == null
                        ? LocalDate.now()
                        : LocalDate.parse(date)
        );
    }

    @PutMapping("/records/{id}")
    public Map<String, String> correct(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        service.correct(
                id,
                body == null
                        ? null
                        : body.get("checkIn"),
                body == null
                        ? null
                        : body.get("checkOut"),
                body == null
                        ? null
                        : body.get("status"),
                body == null
                        ? null
                        : body.get("reason")
        );

        return Map.of(
                "message",
                "Attendance record corrected successfully"
        );
    }

    // ============================================================
    // EVENTS
    // ============================================================

    @GetMapping("/events")
    public List<AttendanceEvent> events(
            @RequestParam(
                    required = false,
                    defaultValue = "50"
            )
            int limit
    ) {

        return monitorRepository.findEvents(
                getCompanyId(),
                limit
        );
    }

    // ============================================================
    // SCHEDULES
    // ============================================================
    {/*
    @GetMapping("/schedules")
    public List<AttendanceSchedule> schedules() {

        return scheduleService.findAll();
    }

    @PostMapping("/schedules")
    public AttendanceSchedule createSchedule(
            @RequestBody AttendanceSchedule schedule
    ) {

        return scheduleService.create(
                schedule
        );
    }

    @PutMapping("/schedules/{id}")
    public AttendanceSchedule updateSchedule(
            @PathVariable Long id,
            @RequestBody AttendanceSchedule schedule
    ) {

        return scheduleService.update(
                id,
                schedule
        );
    }

    @DeleteMapping("/schedules/{id}")
    public Map<String, String> deleteSchedule(
            @PathVariable Long id
    ) {

        scheduleService.delete(
                id
        );

        return Map.of(
                "message",
                "Attendance schedule deleted successfully"
        );
    }
     */}
    // ============================================================
    // HELPERS
    // ============================================================

    private Long getCompanyId() {

        return companyContextService
                .getCurrentCompanyId();
    }

    private Long readMonitorId(
            Map<String, Object> body
    ) {

        if (
                body == null
                        || body.get("monitorId") == null
        ) {
            throw new IllegalArgumentException(
                    "monitorId is required"
            );
        }

        try {

            return Long.valueOf(
                    body.get("monitorId")
                            .toString()
            );

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "monitorId must be a valid number"
            );
        }
    }
}