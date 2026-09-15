package com.staffhub.controller;

import com.staffhub.model.Leave;
import com.staffhub.service.LeaveService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin
public class LeaveController {

    private final LeaveService leaveService;


    public LeaveController(
            LeaveService leaveService
    ) {

        this.leaveService = leaveService;
    }


    // ============================================================
    // GET /api/leave
    // ============================================================

    @GetMapping
    public List<Leave> getAllLeaves(

            @RequestParam(required = false)
            String employeeId,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String department,

            @RequestParam(required = false)
            String status
    ) {

        return leaveService.getAllLeaves(
                employeeId,
                search,
                department,
                status
        );
    }


    // ============================================================
    // GET /api/leave/{id}
    // ============================================================

    @GetMapping("/{id}")
    public Leave getLeaveById(
            @PathVariable Long id
    ) {

        return leaveService.getLeaveById(id);
    }


    // ============================================================
    // GET /api/leave/balance/{employeeId}
    // ============================================================

    @GetMapping("/balance/{employeeId}")
    public Map<String, Object> getLeaveBalance(
            @PathVariable String employeeId
    ) {

        return leaveService.getLeaveBalance(
                employeeId
        );
    }


    // ============================================================
    // POST /api/leave
    // ============================================================

    @PostMapping
    public Leave createLeave(
            @RequestBody Leave leave
    ) {

        return leaveService.createLeave(
                leave
        );
    }


    // ============================================================
    // PUT /api/leave/{id}/approve
    // ============================================================

    @PutMapping("/{id}/approve")
    public Leave approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false)
            Map<String, String> body
    ) {

        String comment = "";
        String approverId = "";

        if (body != null) {

            comment =
                    body.getOrDefault(
                            "comment",
                            ""
                    );

            approverId =
                    body.getOrDefault(
                            "approverId",
                            ""
                    );
        }

        return leaveService.approveLeave(
                id,
                approverId,
                comment
        );
    }


    // ============================================================
    // PUT /api/leave/{id}/reject
    // ============================================================

    @PutMapping("/{id}/reject")
    public Leave rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false)
            Map<String, String> body
    ) {

        String comment = "";
        String approverId = "";

        if (body != null) {

            comment =
                    body.getOrDefault(
                            "comment",
                            ""
                    );

            approverId =
                    body.getOrDefault(
                            "approverId",
                            ""
                    );
        }

        return leaveService.rejectLeave(
                id,
                approverId,
                comment
        );
    }


    // ============================================================
    // PUT /api/leave/{id}/cancel
    // ============================================================

    @PutMapping("/{id}/cancel")
    public Leave cancelLeave(
            @PathVariable Long id
    ) {

        return leaveService.cancelLeave(
                id
        );
    }
}