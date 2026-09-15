package com.staffhub.controller;

import com.staffhub.model.Leave;
import com.staffhub.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> getAllLeaves(
            @RequestParam(required = false)
            String employeeId,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String department,

            @RequestParam(required = false)
            String status
    ) {

        try {

            List<Leave> leaves =
                    leaveService.getAllLeaves(
                            employeeId,
                            search,
                            department,
                            status
                    );

            return ResponseEntity.ok(leaves);

        } catch (Exception error) {

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to load leave requests",
                    error
            );
        }
    }

    // ============================================================
    // GET /api/leave/{id}
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeaveById(
            @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                    leaveService.getLeaveById(id)
            );

        } catch (IllegalArgumentException error) {

            return errorResponse(
                    HttpStatus.NOT_FOUND,
                    error.getMessage(),
                    error
            );

        } catch (Exception error) {

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to load leave request",
                    error
            );
        }
    }

    // ============================================================
    // GET /api/leave/balance/{employeeId}
    // ============================================================

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<?> getLeaveBalance(
            @PathVariable String employeeId
    ) {

        try {

            Map<String, Object> balance =
                    leaveService.getLeaveBalance(
                            employeeId
                    );

            return ResponseEntity.ok(balance);

        } catch (IllegalArgumentException error) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    error.getMessage(),
                    error
            );

        } catch (Exception error) {

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to load leave balance",
                    error
            );
        }
    }

    // ============================================================
    // POST /api/leave
    // ============================================================

    @PostMapping
    public ResponseEntity<?> createLeave(
            @RequestBody Leave leave
    ) {

        try {

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            leaveService.createLeave(
                                    leave
                            )
                    );

        } catch (IllegalArgumentException error) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    error.getMessage(),
                    error
            );

        } catch (Exception error) {

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create leave request",
                    error
            );
        }
    }

    // ============================================================
    // PUT /api/leave/{id}/approve
    // ============================================================

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(
            @PathVariable Long id,

            @RequestBody(required = false)
            Map<String, String> body
    ) {

        try {

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

            return ResponseEntity.ok(
                    leaveService.approveLeave(
                            id,
                            approverId,
                            comment
                    )
            );

        } catch (IllegalArgumentException error) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    error.getMessage(),
                    error
            );

        } catch (Exception error) {

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to approve leave request",
                    error
            );
        }
    }

    // ============================================================
    // PUT /api/leave/{id}/reject
    // ============================================================

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeave(
            @PathVariable Long id,

            @RequestBody(required = false)
            Map<String, String> body
    ) {

        try {

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

            return ResponseEntity.ok(
                    leaveService.rejectLeave(
                            id,
                            approverId,
                            comment
                    )
            );

        } catch (IllegalArgumentException error) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    error.getMessage(),
                    error
            );

        } catch (Exception error) {

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to reject leave request",
                    error
            );
        }
    }

    // ============================================================
    // PUT /api/leave/{id}/cancel
    // ============================================================

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelLeave(
            @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                    leaveService.cancelLeave(id)
            );

        } catch (IllegalArgumentException error) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    error.getMessage(),
                    error
            );

        } catch (Exception error) {

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to cancel leave request",
                    error
            );
        }
    }

    // ============================================================
    // ERROR RESPONSE
    // ============================================================

    private ResponseEntity<Map<String, Object>> errorResponse(
            HttpStatus status,
            String message,
            Exception error
    ) {

        return ResponseEntity
                .status(status)
                .body(
                        Map.of(
                                "status", status.value(),
                                "error", status.getReasonPhrase(),
                                "message",
                                message == null
                                        ? "Unknown error"
                                        : message
                        )
                );
    }
}

