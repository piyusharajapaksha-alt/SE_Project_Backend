package com.staffhub.service;

import com.staffhub.model.AttendanceMonitor;
import com.staffhub.model.AttendanceRecord;
import com.staffhub.repository.AttendanceMonitorRepository;
import com.staffhub.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CompanyContextService companyContextService;

    private final SecureRandom random =
            new SecureRandom();

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            AttendanceMonitorRepository monitorRepository,
            CompanyContextService companyContextService
    ) {
        this.attendanceRepository =
                attendanceRepository;

        this.monitorRepository =
                monitorRepository;

        this.companyContextService =
                companyContextService;
    }

    /*
     * ============================================================
     * PUBLIC QR MONITOR
     * ============================================================
     *
     * monitorId belongs to the browser/device.
     *
     * It is NOT the company ID.
     */

    @Transactional
    public synchronized AttendanceMonitor getPublicMonitor(
            Long monitorId
    ) {

        if (monitorId != null) {

            AttendanceMonitor existing =
                    monitorRepository.findById(
                            monitorId
                    );

            if (existing != null) {

                if (
                        existing.isActive()
                                && qrExpired(existing)
                ) {

                    if (
                            existing.isAuthorized()
                                    && existing.getCompanyId()
                                    != null
                    ) {
                        return rotateQrInternal(
                                existing,
                                "SYSTEM",
                                true
                        );
                    }
                }

                return existing;
            }
        }

        String activationCode =
                generateActivationCode();

        Long id =
                monitorRepository.create(
                        activationCode
                );

        return monitorRepository.findById(id);
    }

    /*
     * ============================================================
     * ACCEPT NEW MONITOR
     * ============================================================
     *
     * OTP is used ONLY here.
     */

    @Transactional
    public synchronized AttendanceMonitor authorizeMonitor(
            String code
    ) {

        if (
                code == null
                        || code.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Activation code is required"
            );
        }

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        String user =
                currentUsername();

        AttendanceMonitor monitor =
                monitorRepository
                        .findUnassignedByCode(
                                code.trim()
                        );

        if (monitor == null) {
            throw new IllegalArgumentException(
                    "Invalid or already accepted monitor code"
            );
        }

        monitorRepository.authorize(
                monitor.getId(),
                companyId,
                user
        );

        monitor =
                monitorRepository.findByCompany(
                        monitor.getId(),
                        companyId
                );

        monitorRepository.logEvent(
                monitor.getId(),
                companyId,
                null,
                null,
                "MONITOR_AUTHORIZED",
                null,
                user,
                "QR monitor accepted by company"
        );

        return monitor;
    }

    /*
     * ============================================================
     * ACCEPTED MONITORS
     * ============================================================
     */

    public List<AttendanceMonitor>
    getAcceptedMonitors() {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        return monitorRepository
                .findAcceptedByCompany(
                        companyId
                );
    }

    /*
     * ============================================================
     * REJECT MONITOR
     * ============================================================
     */

    @Transactional
    public synchronized void rejectMonitor(
            Long monitorId
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        AttendanceMonitor monitor =
                monitorRepository.findByCompany(
                        monitorId,
                        companyId
                );

        if (monitor == null) {
            throw new IllegalArgumentException(
                    "Monitor does not belong to your company"
            );
        }

        if (monitor.isActive()) {
            deactivate(
                    monitorId,
                    "MANUAL"
            );
        }

        monitorRepository.logEvent(
                monitorId,
                companyId,
                null,
                null,
                "MONITOR_REJECTED",
                monitor.getQrSequence(),
                currentUsername(),
                "Monitor rejected and returned to unassigned state"
        );

        monitorRepository.reject(
                monitorId,
                companyId
        );
    }

    /*
     * ============================================================
     * ACTIVATE ACCEPTED MONITOR
     * ============================================================
     */

    @Transactional
    public synchronized AttendanceMonitor activate(
            Long monitorId
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        AttendanceMonitor monitor =
                monitorRepository.findByCompany(
                        monitorId,
                        companyId
                );

        if (monitor == null) {
            throw new IllegalArgumentException(
                    "Monitor does not belong to your company"
            );
        }

        if (monitor.isActive()) {
            return monitor;
        }

        LocalDateTime now =
                LocalDateTime.now();

        String token =
                generateQrToken();

        monitorRepository.activate(
                monitorId,
                companyId,
                currentUsername(),
                "MANUAL",
                token,
                1,
                now,
                now.plusSeconds(QR_SECONDS)
        );

        Long sessionId =
                monitorRepository.createSession(
                        monitorId,
                        companyId,
                        "MANUAL",
                        currentUsername()
                );

        monitorRepository.logEvent(
                monitorId,
                companyId,
                null,
                null,
                "MONITOR_ACTIVATED",
                1,
                currentUsername(),
                "Attendance monitor activated. Session "
                        + sessionId
        );

        return monitorRepository.findByCompany(
                monitorId,
                companyId
        );
    }

    /*
     * ============================================================
     * DEACTIVATE
     * ============================================================
     */

    @Transactional
    public synchronized void deactivate(
            Long monitorId,
            String type
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        AttendanceMonitor monitor =
                monitorRepository.findByCompany(
                        monitorId,
                        companyId
                );

        if (monitor == null) {
            throw new IllegalArgumentException(
                    "Monitor does not belong to your company"
            );
        }

        if (!monitor.isActive()) {
            return;
        }

        String performedBy =
                currentUsername();

        Long sessionId =
                monitorRepository.getOpenSessionId(
                        monitorId,
                        companyId
                );

        if (sessionId != null) {

            monitorRepository.closeSession(
                    sessionId,
                    companyId,
                    performedBy,
                    type == null
                            ? "MANUAL"
                            : type
            );
        }

        monitorRepository.logEvent(
                monitorId,
                companyId,
                null,
                null,
                "MONITOR_DEACTIVATED",
                monitor.getQrSequence(),
                performedBy,
                "Attendance monitor deactivated"
        );

        monitorRepository.deactivate(
                monitorId,
                companyId,
                generateActivationCode()
        );
    }

    /*
 * ============================================================
 * SCHEDULED ATTENDANCE OPERATIONS
 * ============================================================
 *
 * These methods are specifically for AttendanceScheduler.
 *
 * The scheduler does NOT have an authenticated HTTP user,
 * therefore companyId is supplied from the schedule database row.
 */

/*
 * ============================================================
 * ACTIVATE SCHEDULED MONITOR
 * ============================================================
 */

@Transactional
public synchronized AttendanceMonitor
activateScheduledForCompany(
        Long companyId,
        String scheduleName
) {

    if (companyId == null) {
        throw new IllegalArgumentException(
                "Company ID is required"
        );
    }

    /*
     * Check whether this company already has
     * an active monitor.
     */
    AttendanceMonitor activeMonitor =
            monitorRepository.findActiveByCompany(
                    companyId
            );

    if (activeMonitor != null) {
        return activeMonitor;
    }

    /*
     * Find authorized monitors belonging to
     * this company.
     */
    List<AttendanceMonitor> monitors =
            monitorRepository.findAcceptedByCompany(
                    companyId
            );

    /*
     * No authorized monitor.
     *
     * Do NOT activate a random/unassigned monitor.
     */
    if (
            monitors == null
                    || monitors.isEmpty()
    ) {

        throw new IllegalStateException(
                "No authorized attendance monitor exists for company "
                        + companyId
        );
    }

    /*
     * Use the newest accepted monitor.
     */
    AttendanceMonitor monitor =
            monitors.get(
                    monitors.size() - 1
            );

    LocalDateTime now =
            LocalDateTime.now();

    String token =
            generateQrToken();

    monitorRepository.activate(
            monitor.getId(),
            companyId,
            "SCHEDULE",
            "SCHEDULE",
            token,
            1,
            now,
            now.plusSeconds(QR_SECONDS)
    );

    Long sessionId =
            monitorRepository.createSession(
                    monitor.getId(),
                    companyId,
                    "SCHEDULE",
                    "SCHEDULE"
            );

    monitorRepository.logEvent(
            monitor.getId(),
            companyId,
            null,
            null,
            "MONITOR_ACTIVATED",
            1,
            "SCHEDULE",
            "Attendance monitor automatically activated for schedule: "
                    + scheduleName
                    + ". Session ID: "
                    + sessionId
    );

    return monitorRepository.findByCompany(
            monitor.getId(),
            companyId
    );
}


/*
 * ============================================================
 * DEACTIVATE SCHEDULED MONITOR
 * ============================================================
 */

@Transactional
public synchronized void
deactivateScheduledForCompany(
        Long companyId,
        Long monitorId
) {

    if (companyId == null) {
        throw new IllegalArgumentException(
                "Company ID is required"
        );
    }

    if (monitorId == null) {
        return;
    }

    AttendanceMonitor monitor =
            monitorRepository.findByCompany(
                    monitorId,
                    companyId
            );

    /*
     * Monitor may already have been manually
     * deactivated.
     */
    if (
            monitor == null
                    || !monitor.isActive()
    ) {
        return;
    }

    Long sessionId =
            monitorRepository.getOpenSessionId(
                    monitorId,
                    companyId
            );

    if (sessionId != null) {

        monitorRepository.closeSession(
                sessionId,
                companyId,
                "SCHEDULE",
                "SCHEDULE"
        );
    }

    monitorRepository.logEvent(
            monitorId,
            companyId,
            null,
            null,
            "MONITOR_DEACTIVATED",
            monitor.getQrSequence(),
            "SCHEDULE",
            "Attendance monitor automatically deactivated because the attendance schedule ended"
    );

    /*
     * This also clears the QR and generates a
     * new activation code.
     */
    monitorRepository.deactivate(
            monitorId,
            companyId,
            generateActivationCode()
    );
}


/*
 * ============================================================
 * REFRESH SCHEDULED QR
 * ============================================================
 */

@Transactional
public synchronized void
refreshScheduledQrIfExpired(
        Long companyId,
        Long monitorId
) {

    if (companyId == null || monitorId == null) {
        return;
    }

    AttendanceMonitor monitor =
            monitorRepository.findByCompany(
                    monitorId,
                    companyId
            );

    if (
            monitor == null
                    || !monitor.isActive()
    ) {
        return;
    }

    /*
     * QR expires every 10 seconds.
     *
     * Only rotate when actually expired.
     */
    if (qrExpired(monitor)) {

        rotateQrInternal(
                monitor,
                "SCHEDULE",
                true
        );
    }
}

    /*
     * ============================================================
     * ROTATE QR
     * ============================================================
     */

    @Transactional
    public synchronized AttendanceMonitor rotateQr(
            Long monitorId
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        AttendanceMonitor monitor =
                monitorRepository.findByCompany(
                        monitorId,
                        companyId
                );

        if (monitor == null) {
            throw new IllegalArgumentException(
                    "Monitor does not belong to your company"
            );
        }

        if (!monitor.isActive()) {
            throw new IllegalStateException(
                    "Monitor is not active"
            );
        }

        return rotateQrInternal(
                monitor,
                currentUsername(),
                false
        );
    }

    private AttendanceMonitor rotateQrInternal(
            AttendanceMonitor monitor,
            String performedBy,
            boolean automatic
    ) {

        Long companyId =
                monitor.getCompanyId();

        if (companyId == null) {
            throw new IllegalStateException(
                    "Monitor is not assigned to a company"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        int sequence =
                monitor.getQrSequence() <= 0
                        ? 1
                        : monitor.getQrSequence() + 1;

        monitorRepository.rotate(
                monitor.getId(),
                companyId,
                generateQrToken(),
                sequence,
                now,
                now.plusSeconds(QR_SECONDS)
        );

        monitorRepository.logEvent(
                monitor.getId(),
                companyId,
                null,
                null,
                "QR_ROTATED",
                sequence,
                performedBy,
                automatic
                        ? "QR automatically rotated"
                        : "QR manually rotated"
        );

        return monitorRepository.findByCompany(
                monitor.getId(),
                companyId
        );
    }

    /*
     * ============================================================
     * QR SCAN
     * ============================================================
     *
     * QR contains:
     *
     * {
     *   "monitorId": 12,
     *   "token": "..."
     * }
     */

    @Transactional
    public synchronized AttendanceRecord scan(
            String employeeIdentifier,
            Long monitorId,
            String token
    ) {

        if (
                employeeIdentifier == null
                        || employeeIdentifier.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (monitorId == null) {
            throw new IllegalArgumentException(
                    "Monitor ID is required"
            );
        }

        if (
                token == null
                        || token.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "QR token is required"
            );
        }

        AttendanceMonitor monitor =
                monitorRepository.findById(
                        monitorId
                );

        if (monitor == null) {
            throw new IllegalArgumentException(
                    "Attendance monitor does not exist"
            );
        }

        if (!monitor.isAuthorized()) {
            throw new IllegalStateException(
                    "This attendance monitor has not been authorized"
            );
        }

        if (monitor.getCompanyId() == null) {
            throw new IllegalStateException(
                    "Attendance monitor is not assigned to a company"
            );
        }

        Long companyId =
                monitor.getCompanyId();

        /*
         * Employee must belong to SAME company.
         */
        Long employeeId =
                attendanceRepository
                        .resolveEmployeeId(
                                employeeIdentifier,
                                companyId
                        );

        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "Employee does not belong to this company"
            );
        }

        /*
         * Monitor must be active.
         */
        if (!monitor.isActive()) {
            throw new IllegalStateException(
                    "Attendance monitor is not active"
            );
        }

        /*
         * Token verification.
         */
        if (
                monitor.getCurrentQrToken() == null
                        || !monitor
                        .getCurrentQrToken()
                        .equals(
                                token.trim()
                        )
        ) {
            throw new IllegalArgumentException(
                    "QR code is invalid"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (
                monitor.getQrExpiresAt() == null
                        || !monitor.getQrExpiresAt()
                        .isAfter(now)
        ) {
            throw new IllegalArgumentException(
                    "QR code has expired"
            );
        }

        /*
         * Leave check is also company scoped.
         */
        if (
                attendanceRepository
                        .employeeOnApprovedLeave(
                                employeeIdentifier,
                                companyId,
                                LocalDate.now()
                        )
        ) {
            throw new IllegalStateException(
                    "Employee is on approved leave today"
            );
        }

        Long sessionId =
                monitorRepository.getOpenSessionId(
                        monitorId,
                        companyId
                );

        if (sessionId == null) {
            throw new IllegalStateException(
                    "Attendance monitor session is not available"
            );
        }

        LocalDate today =
                LocalDate.now();

        AttendanceRecord existing =
                attendanceRepository
                        .findByEmployeeAndDate(
                                employeeId,
                                companyId,
                                today
                        );

        /*
         * CHECK IN
         */
        if (existing == null) {

            Long id =
                    attendanceRepository.create(
                            employeeId,
                            companyId,
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
                                    companyId,
                                    today
                            );

            monitorRepository.logEvent(
                    monitorId,
                    companyId,
                    id,
                    employeeId,
                    "CHECK_IN",
                    monitor.getQrSequence(),
                    employeeIdentifier,
                    "Employee checked in using company QR monitor"
            );

            rotateQrInternal(
                    monitor,
                    employeeIdentifier,
                    false
            );

            return created;
        }

        /*
         * ALREADY CHECKED OUT
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
                companyId,
                now,
                "QR"
        );

        AttendanceRecord updated =
                attendanceRepository
                        .findByEmployeeAndDate(
                                employeeId,
                                companyId,
                                today
                        );

        monitorRepository.logEvent(
                monitorId,
                companyId,
                existing.getId(),
                employeeId,
                "CHECK_OUT",
                monitor.getQrSequence(),
                employeeIdentifier,
                "Employee checked out using company QR monitor"
        );

        rotateQrInternal(
                monitor,
                employeeIdentifier,
                false
        );

        return updated;
    }

    /*
     * ============================================================
     * EMPLOYEE TODAY
     * ============================================================
     */

    public AttendanceRecord today(
            String employeeIdentifier
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        Long employeeId =
                attendanceRepository
                        .resolveEmployeeId(
                                employeeIdentifier,
                                companyId
                        );

        if (employeeId == null) {
            return null;
        }

        return attendanceRepository
                .findTodayByEmployee(
                        employeeId,
                        companyId
                );
    }

    /*
     * ============================================================
     * EMPLOYEE HISTORY
     * ============================================================
     */

    public List<AttendanceRecord> history(
            String employeeIdentifier
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        Long employeeId =
                attendanceRepository
                        .resolveEmployeeId(
                                employeeIdentifier,
                                companyId
                        );

        if (employeeId == null) {
            return List.of();
        }

        return attendanceRepository
                .findByEmployee(
                        employeeId,
                        companyId
                );
    }

    /*
     * ============================================================
     * MANAGEMENT RECORDS
     * ============================================================
     */

    public List<AttendanceRecord> records(
            LocalDate date
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        return attendanceRepository
                .findByDate(
                        companyId,
                        date == null
                                ? LocalDate.now()
                                : date
                );
    }

    /*
     * ============================================================
     * CORRECTION
     * ============================================================
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

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        attendanceRepository.updateCorrection(
                id,
                companyId,
                parseDateTime(checkIn),
                parseDateTime(checkOut),
                status,
                reason
        );
    }

    /*
     * ============================================================
     * SUMMARY
     * ============================================================
     */

    public Map<String, Object> summary(
            LocalDate date
    ) {

        Long companyId =
                companyContextService
                        .getCurrentCompanyId();

        LocalDate selectedDate =
                date == null
                        ? LocalDate.now()
                        : date;

        int activeEmployees =
                attendanceRepository
                        .countActiveEmployees(
                                companyId
                        );

        int onLeave =
                attendanceRepository
                        .countEmployeesOnApprovedLeave(
                                companyId,
                                selectedDate
                        );

        int expected =
                Math.max(
                        0,
                        activeEmployees - onLeave
                );

        int attended =
                attendanceRepository
                        .countAttended(
                                companyId,
                                selectedDate
                        );

        int checkedOut =
                attendanceRepository
                        .countCheckedOut(
                                companyId,
                                selectedDate
                        );

        int working =
                attendanceRepository
                        .countCurrentlyWorking(
                                companyId,
                                selectedDate
                        );

        int late =
                attendanceRepository
                        .countLate(
                                companyId,
                                selectedDate
                        );

        int notAttended =
                Math.max(
                        0,
                        expected - attended
                );

        return Map.of(
                "date",
                selectedDate,
                "expected",
                expected,
                "attended",
                attended,
                "notAttended",
                notAttended,
                "onLeave",
                onLeave,
                "late",
                late,
                "checkedOut",
                checkedOut,
                "currentlyWorking",
                working
        );
    }

    private boolean qrExpired(
            AttendanceMonitor monitor
    ) {

        return monitor.getQrExpiresAt() == null
                || !monitor.getQrExpiresAt()
                .isAfter(
                        LocalDateTime.now()
                );
    }

    private String currentUsername() {

        org.springframework.security.core.Authentication
                authentication =
                org.springframework.security.core.context
                        .SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || authentication.getName() == null
        ) {
            return "SYSTEM";
        }

        return authentication
                .getName()
                .trim();
    }

    private LocalDateTime parseDateTime(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
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