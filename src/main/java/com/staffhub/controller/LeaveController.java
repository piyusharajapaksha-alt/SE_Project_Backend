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
        this.leaveService =
                leaveService;
    }

    // ============================================================
    // GET ALL
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

            // IMPORTANT:
            // Empty database is NOT an error.
            //
            // If there are no records, this returns:
            // []
            return ResponseEntity.ok(
                    leaves
            );

        } catch (Exception error) {

            error.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            errorResponse(
                                    500,
                                    "Internal Server Error",
                                    error
                            )
                    );
        }
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeaveById(
            @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                    leaveService.getLeaveById(id)
            );

        } catch (
                IllegalArgumentException error
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            errorResponse(
                                    404,
                                    "Not Found",
                                    error
                            )
                    );

        } catch (Exception error) {

            error.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            errorResponse(
                                    500,
                                    "Internal Server Error",
                                    error
                            )
                    );
        }
    }

    // ============================================================
    // GET BALANCE
    // ============================================================

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<?> getLeaveBalance(
            @PathVariable String employeeId
    ) {

        try {

            return ResponseEntity.ok(
                    leaveService.getLeaveBalance(
                            employeeId
                    )
            );

        } catch (
                IllegalArgumentException error
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.BAD_REQUEST
                    )
                    .body(
                            errorResponse(
                                    400,
                                    "Bad Request",
                                    error
                            )
                    );

        } catch (Exception error) {

            error.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            errorResponse(
                                    500,
                                    "Internal Server Error",
                                    error
                            )
                    );
        }
    }

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping
    public ResponseEntity<?> createLeave(
            @RequestBody Leave leave
    ) {

        try {

            Leave created =
                    leaveService.createLeave(
                            leave
                    );

            return ResponseEntity
                    .status(
                            HttpStatus.CREATED
                    )
                    .body(created);

        } catch (
                IllegalArgumentException error
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.BAD_REQUEST
                    )
                    .body(
                            errorResponse(
                                    400,
                                    "Bad Request",
                                    error
                            )
                    );

        } catch (Exception error) {

            error.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            errorResponse(
                                    500,
                                    "Internal Server Error",
                                    error
                            )
                    );
        }
    }

    // ============================================================
    // APPROVE
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

        } catch (
                IllegalArgumentException error
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.BAD_REQUEST
                    )
                    .body(
                            errorResponse(
                                    400,
                                    "Bad Request",
                                    error
                            )
                    );

        } catch (Exception error) {

            error.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            errorResponse(
                                    500,
                                    "Internal Server Error",
                                    error
                            )
                    );
        }
    }

    // ============================================================
    // REJECT
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

        } catch (
                IllegalArgumentException error
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.BAD_REQUEST
                    )
                    .body(
                            errorResponse(
                                    400,
                                    "Bad Request",
                                    error
                            )
                    );

        } catch (Exception error) {

            error.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            errorResponse(
                                    500,
                                    "Internal Server Error",
                                    error
                            )
                    );
        }
    }

    // ============================================================
    // CANCEL
    // ============================================================

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelLeave(
            @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                    leaveService.cancelLeave(
                            id
                    )
            );

        } catch (
                IllegalArgumentException error
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.BAD_REQUEST
                    )
                    .body(
                            errorResponse(
                                    400,
                                    "Bad Request",
                                    error
                            )
                    );

        } catch (Exception error) {

            error.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            errorResponse(
                                    500,
                                    "Internal Server Error",
                                    error
                            )
                    );
        }
    }

    // ============================================================
    // STANDARD ERROR RESPONSE
    // ============================================================

    private Map<String, Object> errorResponse(
            int status,
            String error,
            Exception exception
    ) {

        String message =
                exception.getMessage();

        if (
                message == null
                        || message.isBlank()
        ) {
            message =
                    "Unknown server error";
        }

        return Map.of(
                "status",
                status,

                "error",
                error,

                "message",
                message
        );
    }
}