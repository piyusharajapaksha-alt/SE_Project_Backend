package com.staffhub.service;

import com.staffhub.model.Leave;
import com.staffhub.repository.LeaveRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;


    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }


    // ============================================================
    // GET ALL
    // ============================================================

    public List<Leave> getAllLeaves(
            String employeeId,
            String search,
            String department,
            String status
    ) {

        return leaveRepository.findAll(
                employeeId,
                search,
                department,
                status
        );
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    public Leave getLeaveById(Long id) {

        Leave leave = leaveRepository.findById(id);

        if (leave == null) {
            throw new IllegalArgumentException(
                    "Leave request not found"
            );
        }

        return leave;
    }


    // ============================================================
    // CREATE
    // ============================================================

    public Leave createLeave(Leave leave) {

        validateLeave(leave);

        leave.setStatus("Pending");

        return leaveRepository.create(leave);
    }


    // ============================================================
    // APPROVE
    // ============================================================

    public Leave approveLeave(
            Long id,
            String approverId,
            String comment
    ) {

        return leaveRepository.approve(
                id,
                approverId,
                comment
        );
    }


    // ============================================================
    // REJECT
    // ============================================================

    public Leave rejectLeave(
            Long id,
            String approverId,
            String comment
    ) {

        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException(
                    "A rejection comment is required"
            );
        }

        return leaveRepository.reject(
                id,
                approverId,
                comment
        );
    }


    // ============================================================
    // CANCEL
    // ============================================================

    public Leave cancelLeave(Long id) {

        return leaveRepository.cancel(id);
    }


    // ============================================================
    // LEAVE BALANCE
    // ============================================================

    public Map<String, Object> getLeaveBalance(
            String employeeId
    ) {

        int year = LocalDate.now().getYear();

        long annualUsed =
                leaveRepository.getApprovedLeaveDays(
                        employeeId,
                        "Annual Leave",
                        year
                );

        long sickUsed =
                leaveRepository.getApprovedLeaveDays(
                        employeeId,
                        "Sick Leave",
                        year
                );

        long personalUsed =
                leaveRepository.getApprovedLeaveDays(
                        employeeId,
                        "Personal Leave",
                        year
                );


        // StaffHub yearly allocation
        int annualTotal = 14;
        int sickTotal = 7;
        int personalTotal = 5;


        Map<String, Object> annual = createBalance(
                annualTotal,
                annualUsed
        );

        Map<String, Object> sick = createBalance(
                sickTotal,
                sickUsed
        );

        Map<String, Object> personal = createBalance(
                personalTotal,
                personalUsed
        );


        Map<String, Object> result = new HashMap<>();

        result.put("annualLeave", annual);
        result.put("sickLeave", sick);
        result.put("personalLeave", personal);

        return result;
    }


    // ============================================================
    // VALIDATION
    // ============================================================

    private void validateLeave(Leave leave) {

        if (leave.getEmployeeId() == null
                || leave.getEmployeeId().isBlank()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }


        if (leave.getType() == null
                || leave.getType().isBlank()) {

            throw new IllegalArgumentException(
                    "Leave type is required"
            );
        }


        if (leave.getStartDate() == null) {

            throw new IllegalArgumentException(
                    "Start date is required"
            );
        }


        if (leave.getEndDate() == null) {

            throw new IllegalArgumentException(
                    "End date is required"
            );
        }


        if (leave.getEndDate()
                .isBefore(leave.getStartDate())) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }


        if (leave.getReason() == null
                || leave.getReason().isBlank()) {

            throw new IllegalArgumentException(
                    "Reason is required"
            );
        }
    }


    // ============================================================
    // BALANCE HELPER
    // ============================================================

    private Map<String, Object> createBalance(
            int total,
            long used
    ) {

        Map<String, Object> balance =
                new HashMap<>();

        long remaining =
                Math.max(0, total - used);

        balance.put("total", total);
        balance.put("used", used);
        balance.put("remaining", remaining);

        return balance;
    }
}