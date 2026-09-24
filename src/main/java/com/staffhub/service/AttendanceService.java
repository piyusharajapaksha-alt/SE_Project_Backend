package com.staffhub.service;

import com.staffhub.model.AttendanceMonitor;
import com.staffhub.model.AttendanceRecord;
import com.staffhub.repository.AttendanceMonitorRepository;
import com.staffhub.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        return monitor;
    }

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

        if (!monitor.getActivationCode()
                .equals(code.trim())) {

            throw new IllegalArgumentException(
                    "Invalid attendance activation code"
            );
        }

        String activationType =
                type == null || type.isBlank()
                        ? "MANUAL"
                        : type.toUpperCase();

        LocalDateTime now =
                LocalDateTime.now();

        monitorRepository.activate(
                monitor.getId(),
                user,
                activationType,
                generateQrToken(),
                1,
                now,
                now.plusSeconds(QR_SECONDS)
        );

        monitor =
                monitorRepository.getMonitor();

        monitorRepository.createSession(
                monitor.getId(),
                activationType,
                user
        );

        return monitorRepository.getMonitor();
    }

    public synchronized void deactivate(
            String user,
            String type
    ) {

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {
            return;
        }

        Long sessionId =
                monitorRepository.getOpenSessionId(
                        monitor.getId()
                );

        if (sessionId != null) {

            monitorRepository.closeSession(
                    sessionId,
                    user,
                    type == null
                            ? "MANUAL"
                            : type
            );
        }

        monitorRepository.deactivate(
                monitor.getId(),
                generateActivationCode()
        );
    }

    public synchronized AttendanceMonitor rotateQr() {

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {
            throw new IllegalStateException(
                    "Attendance monitor is not active"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        monitorRepository.rotate(
                monitor.getId(),
                generateQrToken(),
                monitor.getQrSequence() + 1,
                now,
                now.plusSeconds(QR_SECONDS)
        );

        return monitorRepository.getMonitor();
    }

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

        if (!attendanceRepository
                .employeeExists(employeeId)) {

            throw new IllegalArgumentException(
                    "Employee does not exist"
            );
        }

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {
            throw new IllegalStateException(
                    "Attendance monitor is not active"
            );
        }

        if (
                monitor.getCurrentQrToken() == null
                ||
                !monitor.getCurrentQrToken()
                        .equals(token)
        ) {

            throw new IllegalArgumentException(
                    "QR code is invalid or expired"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (
                monitor.getQrExpiresAt() == null
                ||
                monitor.getQrExpiresAt()
                        .isBefore(now)
        ) {

            throw new IllegalArgumentException(
                    "QR code has expired"
            );
        }

        LocalDate today =
                LocalDate.now();

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

        if (existing == null) {

            String status =
                    calculateStatus(
                            monitor
                    );

            Long id =
                    attendanceRepository.create(
                            employeeId,
                            today,
                            now,
                            "QR",
                            sessionId,
                            status
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

            rotateQr();

            return created;
        }

        if (existing.getCheckOut() != null) {

            throw new IllegalStateException(
                    "Attendance has already been checked out today"
            );
        }

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

        rotateQr();

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

    private String calculateStatus(
            AttendanceMonitor monitor
    ) {

        LocalTime now =
                LocalTime.now();

        /*
         * Default:
         * first scan is PRESENT.
         *
         * Schedule-specific lateness is handled
         * by the schedule system.
         */
        return "PRESENT";
    }

    private LocalDateTime parseDateTime(
            String value
    ) {

        if (
                value == null
                ||
                value.isBlank()
        ) {
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