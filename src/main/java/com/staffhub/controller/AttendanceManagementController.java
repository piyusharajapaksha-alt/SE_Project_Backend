package com.staffhub.controller;

import com.staffhub.model.AttendanceRecord;
import com.staffhub.service.AttendanceManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/attendance/management")
public class AttendanceManagementController {

    private final AttendanceManagementService service;

    public AttendanceManagementController(
            AttendanceManagementService service
    ) {
        this.service = service;
    }

    // ============================================================
    // CREATE BRAND-NEW MANUAL ATTENDANCE
    // ============================================================

    @PostMapping("/manual")
    public AttendanceRecord createManual(
            @RequestBody Map<String, String> body
    ) {

        if (body == null) {
            throw new IllegalArgumentException(
                    "Request body is required"
            );
        }

        return service.createManual(
                body.get("employeeNumber"),
                body.get("date"),
                body.get("checkIn"),
                body.get("checkOut"),
                body.get("status"),
                body.get("reason")
        );
    }

    // ============================================================
    // EDIT / CORRECT EXISTING ATTENDANCE
    // ============================================================

    @PutMapping("/records/{id}")
    public AttendanceRecord correct(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        if (body == null) {
            throw new IllegalArgumentException(
                    "Request body is required"
            );
        }

        return service.correct(
                id,
                body.get("checkIn"),
                body.get("checkOut"),
                body.get("status"),
                body.get("reason")
        );
    }

    // ============================================================
    // DELETE REAL DATABASE RECORD
    // ============================================================

    @DeleteMapping("/records/{id}")
    public Map<String, String> delete(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        String verification =
                body == null
                        ? null
                        : body.get("verification");

        service.delete(
                id,
                verification
        );

        return Map.of(
                "message",
                "Attendance record deleted successfully"
        );
    }
}

