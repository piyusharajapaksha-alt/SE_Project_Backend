package com.staffhub.service;

import com.staffhub.model.AttendanceSchedule;
import com.staffhub.repository.AttendanceScheduleRepository;
import com.staffhub.repository.AttendanceMonitorRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
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

    @Scheduled(fixedRate = 30000)
    public void processSchedules() {

        LocalDateTime now =
                LocalDateTime.now();

        LocalDate date =
                now.toLocalDate();

        LocalTime time =
                now.toLocalTime();

        List<AttendanceSchedule> schedules =
                scheduleRepository.findAll();

        boolean shouldBeActive = false;

        for (AttendanceSchedule schedule : schedules) {

            if (!schedule.isEnabled()) {
                continue;
            }

            if (!matchesDate(schedule, date)) {
                continue;
            }

            LocalTime start =
                    schedule.getStartTime();

            LocalTime end =
                    schedule.getEndTime();

            if (
                    !time.isBefore(start)
                    &&
                    time.isBefore(end)
            ) {
                shouldBeActive = true;
                break;
            }
        }

        var monitor =
                monitorRepository.getActiveMonitor();

        if (shouldBeActive && monitor == null) {

            AttendanceSchedule matched =
                    schedules.stream()
                            .filter(AttendanceSchedule::isEnabled)
                            .filter(s -> matchesDate(s, date))
                            .filter(s ->
                                    !time.isBefore(
                                            s.getStartTime()
                                    )
                                    &&
                                    time.isBefore(
                                            s.getEndTime()
                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (matched != null) {

                attendanceService.activate(
                        getActivationCode(),
                        "SCHEDULE",
                        "SCHEDULE"
                );
            }

        } else if (!shouldBeActive && monitor != null) {

            attendanceService.deactivate(
                    "SCHEDULE",
                    "SCHEDULE"
            );
        }
    }

    /*
     * Schedule activation through the normal activation
     * method requires the temporary code.
     *
     * This helper gets the current code from the monitor.
     */
    private String getActivationCode() {

        return monitorRepository
                .getMonitor()
                .getActivationCode();
    }

    private boolean matchesDate(
            AttendanceSchedule schedule,
            LocalDate date
    ) {

        String type =
                schedule.getScheduleType();

        if ("ONCE".equalsIgnoreCase(type)) {

            return date.equals(
                    schedule.getScheduleDate()
            );
        }

        if ("DAILY".equalsIgnoreCase(type)) {
            return true;
        }

        if ("WEEKLY".equalsIgnoreCase(type)) {

            if (
                    schedule.getDayOfWeek() == null
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