package com.staffhub.controller;

import com.staffhub.model.Grievance;
import com.staffhub.service.GrievanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grievances")
public class GrievanceController {

    private final GrievanceService grievanceService;

    public GrievanceController(
            GrievanceService grievanceService
    ) {
        this.grievanceService = grievanceService;
    }

    @GetMapping
    public List<Grievance> getAll(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category
    ) {

        return grievanceService.getAll(
                employeeId,
                search,
                status,
                priority,
                category
        );
    }

    @GetMapping("/{id}")
    public Grievance getById(
            @PathVariable Long id
    ) {

        return grievanceService.getById(id);
    }

    @PostMapping
    public Long create(
            @RequestBody Grievance grievance
    ) {

        return grievanceService.create(grievance);
    }

    @PostMapping("/{id}/responses")
    public String addResponse(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        grievanceService.addResponse(
                id,
                body.get("employeeId"),
                body.get("text")
        );

        return "Response added successfully";
    }

    @PutMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        grievanceService.updateStatus(
                id,
                body.get("status"),
                body.get("updatedBy")
        );

        return "Status updated successfully";
    }
}