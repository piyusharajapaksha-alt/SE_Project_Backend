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
        this.grievanceService =
                grievanceService;
    }

    // ============================================================
    // GET ALL GRIEVANCES
    // ============================================================

    @GetMapping
    public List<Grievance> getAll(
            @RequestParam(
                    required = false
            )
            String employeeId,

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    required = false
            )
            String status,

            @RequestParam(
                    required = false
            )
            String priority,

            @RequestParam(
                    required = false
            )
            String category
    ) {

        return grievanceService.getAll(
                employeeId,
                search,
                status,
                priority,
                category
        );
    }

    // ============================================================
    // GET ONE
    // ============================================================

    @GetMapping("/{id}")
    public Grievance getById(
            @PathVariable Long id
    ) {

        return grievanceService.getById(
                id
        );
    }

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping
    public Long create(
            @RequestBody Grievance grievance
    ) {

        return grievanceService.create(
                grievance
        );
    }

    // ============================================================
    // UPDATE OWN GRIEVANCE
    //
    // employeeId comes from the logged-in employee in the
    // current StaffHub authentication architecture.
    //
    // The service/repository additionally verifies that the
    // grievance belongs to that employee and is still New.
    // ============================================================

    @PutMapping("/{id}")
    public String updateOwnGrievance(
            @PathVariable Long id,
            @RequestBody Grievance grievance,
            @RequestParam String employeeId
    ) {

        grievanceService.updateOwnGrievance(
                id,
                employeeId,
                grievance
        );

        return "Grievance updated successfully";
    }

    // ============================================================
    // DELETE OWN GRIEVANCE
    // ============================================================

    @DeleteMapping("/{id}")
    public String deleteOwnGrievance(
            @PathVariable Long id,
            @RequestParam String employeeId
    ) {

        grievanceService.deleteOwnGrievance(
                id,
                employeeId
        );

        return "Grievance deleted successfully";
    }

    // ============================================================
    // ADD RESPONSE
    // ============================================================

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

    // ============================================================
    // UPDATE STATUS
    // ============================================================

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