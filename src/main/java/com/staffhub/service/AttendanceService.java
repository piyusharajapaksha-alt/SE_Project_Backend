package com.staffhub.service;

import com.staffhub.model.AttendanceMonitor;
import com.staffhub.model.AttendanceRecord;
import com.staffhub.repository.AttendanceMonitorRepository;
import com.staffhub.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AttendanceService {

    private static final int QR_SECONDS = 10;

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMonitorRepository monitorRepository;

    private final SecureRandom random = new SecureRandom();

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            AttendanceMonitorRepository monitorRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.monitorRepository = monitorRepository;
    }

    /**
     * Returns the current attendance monitor.
     *
     * If no monitor exists, one is created.
     *
     * If the monitor is active and the current QR has expired,
     * a new QR is automatically generated.
     */
    public synchronized AttendanceMonitor getMonitor() {

        AttendanceMonitor monitor =
                monitorRepository.getMonitor();

        if (monitor == null) {

            monitorRepository.create(
                    generateActivationCode()
            );

            monitor =
                    monitorRepository.getMonitor();
        }

        if (monitor != null
                && monitor.isActive()
                && qrExpired(monitor)) {

            monitor =
                    rotateQrInternal(
                            monitor,
                            "SYSTEM",
                            true
                    );
        }

        return monitor;
    }

    /**
     * Manual activation using the temporary 6-digit code.
     */
    public synchronized AttendanceMonitor activate(
            String code,
            String user,
            String type
    ) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Activation code is required"
            );
        }

        AttendanceMonitor monitor =
                getMonitor();

        if (monitor == null) {
            throw new IllegalStateException(
                    "Attendance monitor could not be loaded"
            );
        }

        if (monitor.isActive()) {
            throw new IllegalStateException(
                    "Attendance monitor is already active"
            );
        }

        if (!code.trim().equals(
                monitor.getActivationCode()
        )) {

            throw new IllegalArgumentException(
                    "Invalid attendance activation code"
            );
        }

        return activateInternal(
                monitor,
                user,
                type
        );
    }

    /**
     * Activation performed automatically by a schedule.
     *
     * Scheduled activation does not require the temporary
     * six-digit activation code.
     */
    public synchronized AttendanceMonitor activateScheduled(
            String scheduleName
    ) {

        AttendanceMonitor monitor =
                getMonitor();

        if (monitor == null) {
            throw new IllegalStateException(
                    "Attendance monitor could not be loaded"
            );
        }

        if (monitor.isActive()) {
            return monitor;
        }

        String user =
                scheduleName == null
                        || scheduleName.isBlank()
                        ? "SCHEDULE"
                        : "SCHEDULE:" + scheduleName;

        return activateInternal(
                monitor,
                user,
                "SCHEDULE"
        );
    }

    /**
     * Internal activation method.
     */
    private AttendanceMonitor activateInternal(
            AttendanceMonitor monitor,
            String user,
            String type
    ) {

        String activationType =
                type == null || type.isBlank()
                        ? "MANUAL"
                        : type.trim().toUpperCase();

        String activatedBy =
                user == null || user.isBlank()
                        ? "SYSTEM"
                        : user.trim();

        LocalDateTime now =
                LocalDateTime.now();

        String token =
                generateQrToken();

        monitorRepository.activate(
                monitor.getId(),
                activatedBy,
                activationType,
                token,
                1,
                now,
                now.plusSeconds(QR_SECONDS)
        );

        Long sessionId =
                monitorRepository.createSession(
                        monitor.getId(),
                        activationType,
                        activatedBy
                );

        monitorRepository.logEvent(
                monitor.getId(),
                null,
                null,
                "MONITOR_ACTIVATED",
                1,
                activatedBy,
                "Attendance monitor activated as "
                        + activationType
                        + ". Session ID: "
                        + sessionId
        );

        return monitorRepository.getMonitor();
    }

    /**
     * Manual or scheduled deactivation.
     */
    public synchronized void deactivate(
            String user,
            String type
    ) {

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {
            return;
        }

        String performedBy =
                user == null || user.isBlank()
                        ? "SYSTEM"
                        : user.trim();

        String deactivationType =
                type == null || type.isBlank()
                        ? "MANUAL"
                        : type.trim().toUpperCase();

        Long sessionId =
                monitorRepository.getOpenSessionId(
                        monitor.getId()
                );

        if (sessionId != null) {

            monitorRepository.closeSession(
                    sessionId,
                    performedBy,
                    deactivationType
            );
        }

        monitorRepository.logEvent(
                monitor.getId(),
                null,
                null,
                "MONITOR_DEACTIVATED",
                monitor.getQrSequence(),
                performedBy,
                "Attendance monitor deactivated as "
                        + deactivationType
        );

        /*
         * Generate a new temporary activation code.
         *
         * This means the next activation requires a new
         * six-digit code.
         */
        monitorRepository.deactivate(
                monitor.getId(),
                generateActivationCode()
        );
    }

    /**
     * Manually rotate the QR code.
     */
    public synchronized AttendanceMonitor rotateQr() {

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {
            throw new IllegalStateException(
                    "Attendance monitor is not active"
            );
        }

        return rotateQrInternal(
                monitor,
                "SYSTEM",
                false
        );
    }

    /**
     * Internal QR rotation.
     */
    private AttendanceMonitor rotateQrInternal(
            AttendanceMonitor monitor,
            String performedBy,
            boolean automatic
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        int currentSequence =
                monitor.getQrSequence();

        int sequence =
                currentSequence <= 0
                        ? 1
                        : currentSequence + 1;

        monitorRepository.rotate(
                monitor.getId(),
                generateQrToken(),
                sequence,
                now,
                now.plusSeconds(QR_SECONDS)
        );

        monitorRepository.logEvent(
                monitor.getId(),
                null,
                null,
                "QR_ROTATED",
                sequence,
                performedBy,
                automatic
                        ? "QR automatically rotated after expiration"
                        : "QR manually rotated"
        );

        return monitorRepository.getMonitor();
    }

    /**
     * Employee scans the current QR code.
     *
     * employeeIdentifier may be:
     *
     * EMP001
     * EMP002
     * 1
     * 2
     *
     * The repository resolves the identifier to the actual
     * database employee ID.
     *
     * First successful scan = CHECK_IN.
     * Second successful scan = CHECK_OUT.
     */
    public synchronized AttendanceRecord scan(
            String employeeIdentifier,
            String token
    ) {

        if (employeeIdentifier == null
                || employeeIdentifier.isBlank()) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (token == null || token.isBlank()) {

            throw new IllegalArgumentException(
                    "QR token is required"
            );
        }

        /*
         * Resolve EMP001 / EMP002 / numeric ID
         * to the database employee ID.
         */
        Long employeeId =
                attendanceRepository.resolveEmployeeId(
                        employeeIdentifier.trim()
                );

        if (employeeId == null) {

            throw new IllegalArgumentException(
                    "Employee does not exist or is inactive"
            );
        }

        LocalDate today =
                LocalDate.now();

        /*
         * Employees on approved leave cannot mark attendance.
         */
        if (attendanceRepository.employeeOnApprovedLeave(
                employeeIdentifier.trim(),
                today
        )) {

            throw new IllegalStateException(
                    "Employee is on approved leave today"
            );
        }

        /*
         * Get active attendance monitor.
         */
        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {

            throw new IllegalStateException(
                    "Attendance monitor is not active"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        /*
         * Validate QR token.
         */
        if (monitor.getCurrentQrToken() == null
                || !monitor.getCurrentQrToken()
                .equals(token.trim())) {

            throw new IllegalArgumentException(
                    "QR code is invalid or expired"
            );
        }

        /*
         * Validate QR expiration.
         */
        if (monitor.getQrExpiresAt() == null
                || !monitor.getQrExpiresAt()
                .isAfter(now)) {

            throw new IllegalArgumentException(
                    "QR code has expired. Please scan the new QR code."
            );
        }

        /*
         * Find today's attendance record.
         */
        AttendanceRecord existing =
                attendanceRepository
                        .findByEmployeeAndDate(
                                employeeId,
                                today
                        );

        /*
         * Get currently open monitor session.
         */
        Long sessionId =
                monitorRepository.getOpenSessionId(
                        monitor.getId()
                );

        if (sessionId == null) {

            throw new IllegalStateException(
                    "Attendance monitor session is not available"
            );
        }

        /*
         * =====================================================
         * CHECK IN
         * =====================================================
         */
        if (existing == null) {

            Long id =
                    attendanceRepository.create(
                            employeeId,
                            today,
                            now,
                            "QR",
                            sessionId,
                            "PRESENT"
                    );

            AttendanceRecord created =
                    attendanceRepository
                            .findByEmployeeAndDate(
                                    employeeId,
                                    today
                            );

            monitorRepository.logEvent(
                    monitor.getId(),
                    id,
                    employeeId,
                    "CHECK_IN",
                    monitor.getQrSequence(),
                    employeeIdentifier.trim(),
                    "Employee checked in using QR"
            );

            /*
             * Immediately rotate QR after successful scan.
             */
            rotateQrInternal(
                    monitor,
                    employeeIdentifier.trim(),
                    false
            );

            return created;
        }

        /*
         * =====================================================
         * ALREADY CHECKED OUT
         * =====================================================
         */
        if (existing.getCheckOut() != null) {

            throw new IllegalStateException(
                    "Attendance has already been checked out today"
            );
        }

        /*
         * =====================================================
         * CHECK OUT
         * =====================================================
         */
        attendanceRepository.updateCheckOut(
                existing.getId(),
                now,
                "QR"
        );

        AttendanceRecord updated =
                attendanceRepository
                        .findByEmployeeAndDate(
                                employeeId,
                                today
                        );

        monitorRepository.logEvent(
                monitor.getId(),
                existing.getId(),
                employeeId,
                "CHECK_OUT",
                monitor.getQrSequence(),
                employeeIdentifier.trim(),
                "Employee checked out using QR"
        );

        /*
         * Immediately rotate QR after successful scan.
         */
        rotateQrInternal(
                monitor,
                employeeIdentifier.trim(),
                false
        );

        return updated;
    }

    /**
     * Get today's attendance for an employee.
     *
     * Accepts EMP001 or numeric database ID.
     */
    public AttendanceRecord today(
            String employeeIdentifier
    ) {

        Long employeeId =
                attendanceRepository.resolveEmployeeId(
                        employeeIdentifier
                );

        if (employeeId == null) {
            return null;
        }

        return attendanceRepository
                .findTodayByEmployee(
                        employeeId
                );
    }

    /**
     * Get attendance history for an employee.
     *
     * Accepts EMP001 or numeric database ID.
     */
    public List<AttendanceRecord> history(
            String employeeIdentifier
    ) {

        Long employeeId =
                attendanceRepository.resolveEmployeeId(
                        employeeIdentifier
                );

        if (employeeId == null) {
            return List.of();
        }

        return attendanceRepository
                .findByEmployee(
                        employeeId
                );
    }

    /**
     * Get attendance records for a specific date.
     */
    public List<AttendanceRecord> records(
            LocalDate date
    ) {

        if (date == null) {
            date = LocalDate.now();
        }

        return attendanceRepository
                .findByDate(date);
    }

    /**
     * Correct an attendance record manually.
     */
    public void correct(
            Long id,
            String checkIn,
            String checkOut,
            String status,
            String reason
    ) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Attendance record ID is required"
            );
        }

        LocalDateTime in =
                parseDateTime(checkIn);

        LocalDateTime out =
                parseDateTime(checkOut);

        attendanceRepository.updateCorrection(
                id,
                in,
                out,
                status,
                reason
        );
    }

    /**
     * Attendance summary.
     */
    public Map<String, Object> summary(
            LocalDate date
    ) {

        if (date == null) {
            date = LocalDate.now();
        }

        int activeEmployees =
                attendanceRepository
                        .countActiveEmployees();

        int onLeave =
                attendanceRepository
                        .countEmployeesOnApprovedLeave(
                                date
                        );

        int expected =
                Math.max(
                        0,
                        activeEmployees - onLeave
                );

        int attended =
                attendanceRepository
                        .countAttended(date);

        int checkedOut =
                attendanceRepository
                        .countCheckedOut(date);

        int working =
                attendanceRepository
                        .countCurrentlyWorking(date);

        int late =
                attendanceRepository
                        .countLate(date);

        int notAttended =
                Math.max(
                        0,
                        expected - attended
                );

        return Map.of(
                "date", date,
                "expected", expected,
                "attended", attended,
                "notAttended", notAttended,
                "onLeave", onLeave,
                "late", late,
                "checkedOut", checkedOut,
                "currentlyWorking", working
        );
    }

    /**
     * Check whether the current QR has expired.
     */
    private boolean qrExpired(
            AttendanceMonitor monitor
    ) {

        return monitor.getQrExpiresAt() == null
                || !monitor.getQrExpiresAt()
                .isAfter(LocalDateTime.now());
    }

    /**
     * Convert ISO date-time string to LocalDateTime.
     */
    private LocalDateTime parseDateTime(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(value);
    }

    /**
     * Generate a temporary six-digit activation code.
     */
    private String generateActivationCode() {

        return String.format(
                "%06d",
                random.nextInt(1_000_000)
        );
    }

    /**
     * Generate a unique QR token.
     */
    private String generateQrToken() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}