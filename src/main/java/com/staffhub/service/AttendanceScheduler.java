package com.staffhub.service;

import com.staffhub.model.AttendanceSchedule;
import com.staffhub.model.AttendanceMonitor;
import com.staffhub.repository.AttendanceMonitorRepository;
import com.staffhub.repository.AttendanceScheduleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    /**
     * Checks schedules every 10 seconds.
     *
     * This is intentionally the same interval as the QR lifetime.
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
                    scheduleRepository.findAll();

            AttendanceSchedule activeSchedule =
                    findMatchingSchedule(
                            schedules,
                            date,
                            time
                    );

            AttendanceMonitor monitor =
                    monitorRepository.getActiveMonitor();

            /*
             * There is a schedule active now,
             * but monitor is OFF.
             */
            if (activeSchedule != null
                    && monitor == null) {

                attendanceService.activateScheduled(
                        activeSchedule.getScheduleName()
                );

                return;
            }

            /*
             * No schedule is active,
             * but monitor is currently ON.
             */
            if (activeSchedule == null
                    && monitor != null) {

                attendanceService.deactivate(
                        "SCHEDULE",
                        "SCHEDULE"
                );

                return;
            }

            /*
             * Monitor is active and schedule is still valid.
             *
             * getMonitor() automatically refreshes an expired QR.
             */
            if (monitor != null) {
                attendanceService.getMonitor();
            }

        } catch (Exception exception) {

            /*
             * Scheduler must not stop permanently because
             * one bad schedule/database row caused an error.
             */
            System.err.println(
                    "Attendance scheduler error: "
                            + exception.getMessage()
            );
        }
    }

    private AttendanceSchedule findMatchingSchedule(
            List<AttendanceSchedule> schedules,
            LocalDate date,
            LocalTime time
    ) {

        for (AttendanceSchedule schedule : schedules) {

            if (!schedule.isEnabled()) {
                continue;
            }

            if (schedule.getStartTime() == null
                    || schedule.getEndTime() == null) {
                continue;
            }

            if (!matchesDate(schedule, date)) {
                continue;
            }

            /*
             * Start inclusive.
             * End exclusive.
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
         * One specific date.
         */
        if ("ONCE".equalsIgnoreCase(type)) {

            return schedule.getScheduleDate() != null
                    && date.equals(
                    schedule.getScheduleDate()
            );
        }

        /*
         * Every day.
         */
        if ("DAILY".equalsIgnoreCase(type)) {
            return true;
        }

        /*
         * Every selected weekday.
         */
        if ("WEEKLY".equalsIgnoreCase(type)) {

            if (schedule.getDayOfWeek() == null) {
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