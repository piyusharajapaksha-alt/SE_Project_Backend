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

    public synchronized AttendanceMonitor getMonitor() {

        AttendanceMonitor monitor =
                monitorRepository.getMonitor();

        if (monitor == null) {
            String code = generateCode();

            Long id =
                    monitorRepository.create(code);

            monitor = monitorRepository.getMonitor();
        }

        return monitor;
    }

    public synchronized AttendanceMonitor activate(
            String code,
            String user,
            String type
    ) {

        AttendanceMonitor monitor =
                getMonitor();

        if (!monitor.getActivationCode().equals(code)) {
            throw new IllegalArgumentException(
                    "Invalid attendance activation code"
            );
        }

        String token = generateQrToken();

        LocalDateTime now =
                LocalDateTime.now();

        monitorRepository.activate(
                monitor.getId(),
                user,
                type,
                token,
                1,
                now,
                now.plusSeconds(QR_SECONDS)
        );

        return monitorRepository.getMonitor();
    }

    public synchronized void deactivate() {

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor != null) {
            monitorRepository.deactivate(
                    monitor.getId()
            );
        }
    }

    public synchronized AttendanceMonitor rotateQr() {

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {
            throw new IllegalStateException(
                    "Attendance monitor is not active"
            );
        }

        String token = generateQrToken();

        LocalDateTime now =
                LocalDateTime.now();

        monitorRepository.rotate(
                monitor.getId(),
                token,
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

        AttendanceMonitor monitor =
                monitorRepository.getActiveMonitor();

        if (monitor == null) {
            throw new IllegalStateException(
                    "Attendance monitor is not active"
            );
        }

        if (!monitor.getCurrentQrToken().equals(token)) {
            throw new IllegalArgumentException(
                    "QR code is invalid or expired"
            );
        }

        if (
                monitor.getQrExpiresAt() == null ||
                monitor.getQrExpiresAt().isBefore(LocalDateTime.now())
        ) {
            throw new IllegalArgumentException(
                    "QR code has expired"
            );
        }

        LocalDate today = LocalDate.now();

        AttendanceRecord record =
                attendanceRepository.findByEmployeeAndDate(
                        employeeId,
                        today
                );

        if (record == null) {

            attendanceRepository.create(
                    employeeId,
                    today,
                    LocalDateTime.now(),
                    "QR",
                    monitor.getId()
            );

            AttendanceRecord created =
                    attendanceRepository.findByEmployeeAndDate(
                            employeeId,
                            today
                    );

            rotateQr();

            return created;
        }

        if (record.getCheckOut() != null) {
            throw new IllegalStateException(
                    "Attendance has already been checked out today"
            );
        }

        attendanceRepository.updateCheckOut(
                record.getId(),
                LocalDateTime.now(),
                "QR"
        );

        AttendanceRecord updated =
                attendanceRepository.findByEmployeeAndDate(
                        employeeId,
                        today
                );

        rotateQr();

        return updated;
    }

    public AttendanceRecord today(
            Long employeeId
    ) {
        return attendanceRepository.findTodayByEmployee(
                employeeId
        );
    }

    public List<AttendanceRecord> employeeHistory(
            Long employeeId
    ) {
        return attendanceRepository.findByEmployee(
                employeeId
        );
    }

    public List<AttendanceRecord> date(
            LocalDate date
    ) {
        return attendanceRepository.findByDate(date);
    }

    public void correct(
            Long id,
            String checkIn,
            String checkOut,
            String status,
            String reason
    ) {

        LocalDateTime in =
                checkIn == null || checkIn.isBlank()
                        ? null
                        : LocalDateTime.parse(checkIn);

        LocalDateTime out =
                checkOut == null || checkOut.isBlank()
                        ? null
                        : LocalDateTime.parse(checkOut);

        attendanceRepository.updateCorrection(
                id,
                in,
                out,
                status,
                reason
        );
    }

    private String generateCode() {

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