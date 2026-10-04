package com.staffhub.service;

import com.staffhub.model.AttendanceRecord;
import com.staffhub.repository.AttendanceManagementRepository;
import com.staffhub.repository.AttendanceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AttendanceManagementService {

    private static final String HR_MANAGER = "HR Manager";

    private final AttendanceManagementRepository repository;
    private final AttendanceRepository attendanceRepository;
    private final CompanyContextService companyContextService;

    public AttendanceManagementService(
            AttendanceManagementRepository repository,
            AttendanceRepository attendanceRepository,
            CompanyContextService companyContextService
    ) {
        this.repository = repository;
        this.attendanceRepository = attendanceRepository;
        this.companyContextService = companyContextService;
    }

    // ============================================================
    // CREATE BRAND-NEW MANUAL ATTENDANCE
    // ============================================================

    @Transactional
    public AttendanceRecord createManual(
            String employeeNumber,
            String dateValue,
            String checkInValue,
            String checkOutValue,
            String status,
            String reason
    ) {

        requireAuthorizedUser();

        if (employeeNumber == null || employeeNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Employee number is required"
            );
        }

        if (dateValue == null || dateValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Attendance date is required"
            );
        }

        LocalDate date;

        try {
            date = LocalDate.parse(dateValue.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid attendance date"
            );
        }

        String normalizedStatus = normalizeStatus(status);

        LocalDateTime checkIn =
                parseDateTime(checkInValue);

        LocalDateTime checkOut =
                parseDateTime(checkOutValue);

        validateTimes(
                checkIn,
                checkOut
        );

        if (
                requiresCheckIn(normalizedStatus)
                        && checkIn == null
        ) {
            throw new IllegalArgumentException(
                    "Check-in time is required for "
                            + normalizedStatus
            );
        }

        Long companyId =
                companyContextService.getCurrentCompanyId();

        Long employeeId =
                repository.findActiveEmployeeByNumber(
                        employeeNumber,
                        companyId
                );

        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "Active employee "
                            + employeeNumber
                            + " was not found in the current company"
            );
        }

        /*
         * Do not create duplicate attendance rows.
         * The existing record must be edited instead.
         */
        if (
                repository.existsForEmployeeAndDate(
                        employeeId,
                        companyId,
                        date
                )
        ) {
            throw new IllegalArgumentException(
                    "This employee already has an attendance record for "
                            + date
                            + ". Edit the existing record instead."
            );
        }

        String safeReason =
                reason == null
                        ? ""
                        : reason.trim();

        if (safeReason.isBlank()) {
            throw new IllegalArgumentException(
                    "A reason is required for manual attendance"
            );
        }

        Long recordId =
                repository.createManual(
                        employeeId,
                        companyId,
                        date,
                        checkIn,
                        checkOut,
                        normalizedStatus,
                        safeReason
                );

        String username = currentUsername();

        repository.createManagementEvent(
                companyId,
                recordId,
                employeeId,
                "MANUAL_ATTENDANCE_CREATED",
                username,
                "Manual attendance created for "
                        + employeeNumber
                        + " on "
                        + date
                        + ". Reason: "
                        + safeReason
        );

        AttendanceRecord created =
                repository.findById(
                        recordId,
                        companyId
                );

        if (created == null) {
            throw new IllegalStateException(
                    "Manual attendance was created but could not be loaded"
            );
        }

        return created;
    }

    // ============================================================
    // CORRECT / EDIT EXISTING ATTENDANCE
    // ============================================================

    @Transactional
    public AttendanceRecord correct(
            Long id,
            String checkInValue,
            String checkOutValue,
            String status,
            String reason
    ) {

        requireAuthorizedUser();

        if (id == null) {
            throw new IllegalArgumentException(
                    "Attendance record ID is required"
            );
        }

        Long companyId =
                companyContextService.getCurrentCompanyId();

        AttendanceRecord existing =
                repository.findById(
                        id,
                        companyId
                );

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Attendance record was not found"
            );
        }

        String normalizedStatus =
                normalizeStatus(status);

        LocalDateTime checkIn =
                parseDateTime(checkInValue);

        LocalDateTime checkOut =
                parseDateTime(checkOutValue);

        validateTimes(
                checkIn,
                checkOut
        );

        if (
                requiresCheckIn(normalizedStatus)
                        && checkIn == null
        ) {
            throw new IllegalArgumentException(
                    "Check-in time is required for "
                            + normalizedStatus
            );
        }

        String safeReason =
                reason == null
                        ? ""
                        : reason.trim();

        if (safeReason.isBlank()) {
            throw new IllegalArgumentException(
                    "A reason is required when correcting attendance"
            );
        }

        /*
         * IMPORTANT:
         *
         * updateCorrection() belongs to AttendanceRepository,
         * NOT AttendanceManagementRepository.
         */
        attendanceRepository.updateCorrection(
                id,
                companyId,
                checkIn,
                checkOut,
                normalizedStatus,
                safeReason
        );

        String username = currentUsername();

        repository.createManagementEvent(
                companyId,
                id,
                existing.getEmployeeId(),
                "ATTENDANCE_RECORD_CORRECTED",
                username,
                "Attendance record #"
                        + id
                        + " for employee "
                        + existing.getEmployeeNumber()
                        + " was corrected. Reason: "
                        + safeReason
        );

        AttendanceRecord updated =
                repository.findById(
                        id,
                        companyId
                );

        if (updated == null) {
            throw new IllegalStateException(
                    "Attendance was updated but could not be loaded"
            );
        }

        return updated;
    }

    // ============================================================
    // DELETE REAL DATABASE RECORD
    // ============================================================

    @Transactional
    public void delete(
            Long id,
            String verification
    ) {

        requireAuthorizedUser();

        if (id == null) {
            throw new IllegalArgumentException(
                    "Attendance record ID is required"
            );
        }

        if (
                verification == null
                        || !"DELETE".equals(
                        verification.trim()
                                .toUpperCase(Locale.ROOT)
                )
        ) {
            throw new IllegalArgumentException(
                    "Deletion verification failed"
            );
        }

        Long companyId =
                companyContextService.getCurrentCompanyId();

        AttendanceRecord record =
                repository.findById(
                        id,
                        companyId
                );

        if (record == null) {
            throw new IllegalArgumentException(
                    "Attendance record was not found"
            );
        }

        String username = currentUsername();

        /*
         * THIS REALLY DELETES THE DATABASE ROW.
         */
        repository.deleteRecord(
                id,
                companyId
        );

        /*
         * Audit event is kept after deletion.
         * attendance_record_id is NULL because
         * the attendance row no longer exists.
         */
        repository.createManagementEvent(
                companyId,
                null,
                record.getEmployeeId(),
                "ATTENDANCE_RECORD_DELETED",
                username,
                "Attendance record #"
                        + id
                        + " for employee "
                        + record.getEmployeeNumber()
                        + " on "
                        + record.getAttendanceDate()
                        + " was permanently deleted."
        );
    }

    // ============================================================
    // AUTHORIZATION
    // ============================================================

    private void requireAuthorizedUser() {

        String username = currentUsername();

        String role =
                repository.findCurrentUserRole(
                        username
                );

        if (
                role == null
                        || !HR_MANAGER.equalsIgnoreCase(
                        role.trim()
                )
        ) {
            throw new SecurityException(
                    "Only an authorized HR Manager can modify attendance records"
            );
        }
    }

    // ============================================================
    // CURRENT USER
    // ============================================================

    private String currentUsername() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || authentication.getName() == null
                        || authentication.getName().isBlank()
        ) {
            throw new SecurityException(
                    "No authenticated user found"
            );
        }

        return authentication
                .getName()
                .trim();
    }

    // ============================================================
    // DATE / TIME PARSER
    // ============================================================

    private LocalDateTime parseDateTime(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        String normalized =
                value.trim();

        /*
         * Frontend may send ISO values containing Z,
         * for example:
         *
         * 2026-10-05T08:30:00.000Z
         *
         * Remove timezone information because the
         * attendance DB uses LocalDateTime.
         */
        try {

            if (normalized.endsWith("Z")) {

                return java.time.OffsetDateTime
                        .parse(normalized)
                        .toLocalDateTime();
            }

            return LocalDateTime.parse(
                    normalized
            );

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                    "Invalid date/time value: "
                            + value
            );
        }
    }

    // ============================================================
    // TIME VALIDATION
    // ============================================================

    private void validateTimes(
            LocalDateTime checkIn,
            LocalDateTime checkOut
    ) {

        if (
                checkIn != null
                        && checkOut != null
                        && checkOut.isBefore(checkIn)
        ) {
            throw new IllegalArgumentException(
                    "Check-out cannot be earlier than check-in"
            );
        }
    }

    // ============================================================
    // STATUS NORMALIZATION
    // ============================================================

    private String normalizeStatus(
            String status
    ) {

        if (
                status == null
                        || status.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Attendance status is required"
            );
        }

        String value =
                status
                        .trim()
                        .toUpperCase(Locale.ROOT);

        return switch (value) {

            case "PRESENT" ->
                    "Present";

            case "LATE" ->
                    "Late";

            case "ABSENT" ->
                    "Absent";

            case "ON LEAVE" ->
                    "On Leave";

            case "HALF DAY" ->
                    "Half Day";

            default ->
                    throw new IllegalArgumentException(
                            "Invalid attendance status"
                    );
        };
    }

    // ============================================================
    // STATUS TIME REQUIREMENT
    // ============================================================

    private boolean requiresCheckIn(
            String status
    ) {

        return
                "Present".equals(status)
                        || "Late".equals(status)
                        || "Half Day".equals(status);
    }
}

