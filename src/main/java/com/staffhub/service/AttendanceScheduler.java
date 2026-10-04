package com.staffhub.service;

import com.staffhub.model.AttendanceMonitor;
import com.staffhub.model.AttendanceSchedule;
import com.staffhub.repository.AttendanceMonitorRepository;
import com.staffhub.repository.AttendanceScheduleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Component
public class AttendanceScheduler {

    private final AttendanceScheduleRepository scheduleRepository;

    private final AttendanceMonitorRepository monitorRepository;

    private final AttendanceService attendanceService;

    public AttendanceScheduler(
            AttendanceScheduleRepository scheduleRepository,
            AttendanceMonitorRepository monitorRepository,
            AttendanceService attendanceService
    ) {
        this.scheduleRepository =
                scheduleRepository;

        this.monitorRepository =
                monitorRepository;

        this.attendanceService =
                attendanceService;
    }

    /*
     * ============================================================
     * SCHEDULE PROCESSOR
     * ============================================================
     *
     * Runs every 10 seconds.
     *
     * IMPORTANT:
     *
     * This is a background task.
     *
     * There is NO logged-in HTTP user here.
     *
     * Therefore we MUST NOT use:
     *
     * CompanyContextService.getCurrentCompanyId()
     *
     * Instead, every schedule contains its own companyId.
     */

    @Scheduled(fixedRate = 10_000)
    public void processSchedules() {

        try {

            LocalDateTime now =
                    LocalDateTime.now();

            LocalDate date =
                    now.toLocalDate();

            LocalTime time =
                    now.toLocalTime();

            List<AttendanceSchedule> schedules =
                    scheduleRepository
                            .findAllForScheduler();

            /*
             * Process every company independently.
             */
            schedules.stream()
                    .map(
                            AttendanceSchedule::getCompanyId
                    )
                    .filter(
                            Objects::nonNull
                    )
                    .distinct()
                    .forEach(
                            companyId ->
                                    processCompany(
                                            companyId,
                                            schedules,
                                            date,
                                            time
                                    )
                    );

        } catch (Exception exception) {

            /*
             * Scheduler must continue running even if
             * one database/company error occurs.
             */
            System.err.println(
                    "Attendance scheduler error: "
                            + exception.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * PROCESS ONE COMPANY
     * ============================================================
     */

    private void processCompany(
            Long companyId,
            List<AttendanceSchedule> schedules,
            LocalDate date,
            LocalTime time
    ) {

        try {

            AttendanceSchedule activeSchedule =
                    findMatchingSchedule(
                            schedules,
                            companyId,
                            date,
                            time
                    );

            /*
             * Find ONLY this company's active monitor.
             */
            AttendanceMonitor monitor =
                    monitorRepository
                            .findActiveByCompany(
                                    companyId
                            );

            /*
             * ----------------------------------------------------
             * SCHEDULE ACTIVE + MONITOR OFF
             * ----------------------------------------------------
             */

            if (
                    activeSchedule != null
                            && monitor == null
            ) {

                attendanceService
                        .activateScheduledForCompany(
                                companyId,
                                activeSchedule
                                        .getScheduleName()
                        );

                return;
            }

            /*
             * ----------------------------------------------------
             * SCHEDULE FINISHED + MONITOR ON
             * ----------------------------------------------------
             */

            if (
                    activeSchedule == null
                            && monitor != null
            ) {

                attendanceService
                        .deactivateScheduledForCompany(
                                companyId,
                                monitor.getId()
                        );

                return;
            }

            /*
             * ----------------------------------------------------
             * SCHEDULE ACTIVE + MONITOR ACTIVE
             * ----------------------------------------------------
             *
             * Make sure QR is still valid.
             */

            if (
                    activeSchedule != null
                            && monitor != null
            ) {

                attendanceService
                        .refreshScheduledQrIfExpired(
                                companyId,
                                monitor.getId()
                        );
            }

        } catch (Exception exception) {

            /*
             * An error in one company must not stop
             * scheduling for the other companies.
             */
            System.err.println(
                    "Attendance scheduler error for company "
                            + companyId
                            + ": "
                            + exception.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * FIND CURRENT SCHEDULE
     * ============================================================
     */

    private AttendanceSchedule findMatchingSchedule(
            List<AttendanceSchedule> schedules,
            Long companyId,
            LocalDate date,
            LocalTime time
    ) {

        for (
                AttendanceSchedule schedule
                : schedules
        ) {

            /*
             * Never use another company's schedule.
             */
            if (
                    !companyId.equals(
                            schedule.getCompanyId()
                    )
            ) {
                continue;
            }

            if (!schedule.isEnabled()) {
                continue;
            }

            if (
                    schedule.getStartTime() == null
                            || schedule.getEndTime() == null
            ) {
                continue;
            }

            if (
                    !matchesDate(
                            schedule,
                            date
                    )
            ) {
                continue;
            }

            /*
             * Start = inclusive
             *
             * End = exclusive
             *
             * Example:
             *
             * 08:00 -> 17:00
             *
             * 08:00 = active
             * 16:59 = active
             * 17:00 = inactive
             */
            boolean insideTime =
                    !time.isBefore(
                            schedule.getStartTime()
                    )
                            &&
                            time.isBefore(
                                    schedule.getEndTime()
                            );

            if (insideTime) {
                return schedule;
            }
        }

        return null;
    }

    /*
     * ============================================================
     * DATE MATCHING
     * ============================================================
     */

    private boolean matchesDate(
            AttendanceSchedule schedule,
            LocalDate date
    ) {

        String type =
                schedule.getScheduleType();

        if (type == null) {
            return false;
        }

        /*
         * --------------------------------------------------------
         * ONCE
         * --------------------------------------------------------
         */

        if (
                "ONCE".equalsIgnoreCase(
                        type
                )
        ) {

            return schedule.getScheduleDate() != null
                    && date.equals(
                            schedule.getScheduleDate()
                    );
        }

        /*
         * --------------------------------------------------------
         * DAILY
         * --------------------------------------------------------
         */

        if (
                "DAILY".equalsIgnoreCase(
                        type
                )
        ) {

            return true;
        }

        /*
         * --------------------------------------------------------
         * WEEKLY
         * --------------------------------------------------------
         */

        if (
                "WEEKLY".equalsIgnoreCase(
                        type
                )
        ) {

            if (
                    schedule.getDayOfWeek() == null
                            || schedule.getDayOfWeek().isBlank()
            ) {
                return false;
            }

            return schedule
                    .getDayOfWeek()
                    .equalsIgnoreCase(
                            date.getDayOfWeek()
                                    .toString()
                    );
        }

        return false;
    }
}