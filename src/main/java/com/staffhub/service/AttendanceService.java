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

    private final SecureRandom random =
            new SecureRandom();

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            AttendanceMonitorRepository monitorRepository
    ) {
        this.attendanceRepository =
                attendanceRepository;

        this.monitorRepository =
                monitorRepository;
    }

    /**
     * Returns the current monitor.
     *
     * If the QR has expired while the monitor is active,
     * automatically creates a new QR token.
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
     * Manual activation using the temporary 6 digit code.
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
     * Internal activation used by automatic schedules.
     *
     * Does NOT require the manual 6 digit activation code.
     */
    public synchronized AttendanceMonitor activateScheduled(
            String scheduleName
    ) {

        AttendanceMonitor monitor =
                getMonitor();

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
                        : user;

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
                        : user;

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

        monitorRepository.deactivate(
                monitor.getId(),
                generateActivationCode()
        );
    }

    /**
     * Rotate QR manually.
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

    private AttendanceMonitor rotateQrInternal(
            AttendanceMonitor monitor,
            String performedBy,
            boolean automatic
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        int sequence =
                monitor.getQrSequence() + 1;

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
     * Employee scans QR.
     *
     * First successful scan = CHECK_IN.
     * Second successful scan = CHECK_OUT.
     */
    public synchronized AttendanceRecord scan(
            Long employeeId,
            String token
    ) {

        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "QR token is required"
            );
        }

        if (!attendanceRepository.employeeExists(employeeId)) {
            throw new IllegalArgumentException(
                    "Employee does not exist or is inactive"
            );
        }

        LocalDate today =
                LocalDate.now();

        if (attendanceRepository
                .employeeOnApprovedLeave(
                        employeeId,
                        today
                )) {

            throw new IllegalStateException(
                    "Employee is on approved leave today"
            );
        }

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {
            throw new IllegalStateException(
                    "Attendance monitor is not active"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (monitor.getCurrentQrToken() == null
                || !monitor.getCurrentQrToken()
                .equals(token)) {

            throw new IllegalArgumentException(
                    "QR code is invalid or expired"
            );
        }

        if (monitor.getQrExpiresAt() == null
                || !monitor.getQrExpiresAt()
                .isAfter(now)) {

            throw new IllegalArgumentException(
                    "QR code has expired. Please scan the new QR code."
            );
        }

        AttendanceRecord existing =
                attendanceRepository
                        .findByEmployeeAndDate(
                                employeeId,
                                today
                        );

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
         * CHECK IN
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
                    employeeId.toString(),
                    "Employee checked in using QR"
            );

            /*
             * Immediately rotate after successful scan.
             */
            rotateQrInternal(
                    monitor,
                    employeeId.toString(),
                    false
            );

            return created;
        }

        /*
         * Already checked out.
         */
        if (existing.getCheckOut() != null) {

            throw new IllegalStateException(
                    "Attendance has already been checked out today"
            );
        }

        /*
         * CHECK OUT
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
                employeeId.toString(),
                "Employee checked out using QR"
        );

        /*
         * Immediately rotate after successful scan.
         */
        rotateQrInternal(
                monitor,
                employeeId.toString(),
                false
        );

        return updated;
    }

    public AttendanceRecord today(
            Long employeeId
    ) {

        return attendanceRepository
                .findTodayByEmployee(
                        employeeId
                );
    }

    public List<AttendanceRecord> history(
            Long employeeId
    ) {

        return attendanceRepository
                .findByEmployee(
                        employeeId
                );
    }

    public List<AttendanceRecord> records(
            LocalDate date
    ) {

        return attendanceRepository
                .findByDate(date);
    }

    public void correct(
            Long id,
            String checkIn,
            String checkOut,
            String status,
            String reason
    ) {

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

    public Map<String, Object> summary(
            LocalDate date
    ) {

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

    private boolean qrExpired(
            AttendanceMonitor monitor
    ) {

        return monitor.getQrExpiresAt() == null
                || !monitor.getQrExpiresAt()
                .isAfter(LocalDateTime.now());
    }

    private LocalDateTime parseDateTime(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(value);
    }

    private String generateActivationCode() {

        return String.format(
                "%06d",
                random.nextInt(1_000_000)
        );
    }

    private String generateQrToken() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}