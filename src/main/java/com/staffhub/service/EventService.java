package com.staffhub.service;

import com.staffhub.model.Event;
import com.staffhub.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;


    public EventService(
            EventRepository eventRepository
    ) {
        this.eventRepository = eventRepository;
    }


    // ============================================================
    // GET ALL
    // ============================================================

    public List<Event> getAllEvents(
            String search,
            String category,
            String status,
            String employeeId
    ) {

        return eventRepository.findAll(
                search,
                category,
                status,
                employeeId
        );
    }


    // ============================================================
    // GET ONE
    // ============================================================

    public Event getEventById(
            Long id,
            String employeeId
    ) {

        return eventRepository.findById(
                id,
                employeeId
        );
    }


    // ============================================================
    // CREATE
    // ============================================================

    public Event createEvent(
            Event event
    ) {

        validateEvent(event);

        return eventRepository.create(
                event
        );
    }


    // ============================================================
    // UPDATE
    // ============================================================

    public Event updateEvent(
            Long id,
            Event event
    ) {

        validateEvent(event);

        return eventRepository.update(
                id,
                event
        );
    }


    // ============================================================
    // DELETE
    // ============================================================

    public void deleteEvent(
            Long id
    ) {

        eventRepository.delete(id);
    }


    // ============================================================
    // REGISTER
    // ============================================================

    public void register(
            Long eventId,
            String employeeId
    ) {

        if (
                employeeId == null ||
                employeeId.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }


        eventRepository.register(
                eventId,
                employeeId
        );
    }


    // ============================================================
    // UNREGISTER
    // ============================================================

    public void unregister(
            Long eventId,
            String employeeId
    ) {

        if (
                employeeId == null ||
                employeeId.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }


        eventRepository.unregister(
                eventId,
                employeeId
        );
    }


    // ============================================================
    // VALIDATION
    // ============================================================

    private void validateEvent(
            Event event
    ) {

        if (event == null) {

            throw new IllegalArgumentException(
                    "Event data is required"
            );
        }


        if (
                event.getTitle() == null ||
                event.getTitle().trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Event title is required"
            );
        }


        if (
                event.getOrganizerId() == null ||
                event.getOrganizerId()
                        .trim()
                        .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Organizer is required"
            );
        }


        if (
                event.getCategory() == null ||
                event.getCategory()
                        .trim()
                        .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Event category is required"
            );
        }


        if (event.getDate() == null) {

            throw new IllegalArgumentException(
                    "Event date is required"
            );
        }


        if (event.getTime() == null) {

            throw new IllegalArgumentException(
                    "Start time is required"
            );
        }


        if (event.getEndTime() == null) {

            throw new IllegalArgumentException(
                    "End time is required"
            );
        }


        if (
                !event.getEndTime()
                        .isAfter(
                                event.getTime()
                        )
        ) {

            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }


        if (
                event.getLocation() == null ||
                event.getLocation()
                        .trim()
                        .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Event location is required"
            );
        }


        if (
                event.getCapacity() == null ||
                event.getCapacity() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Event capacity must be greater than 0"
            );
        }


        if (
                event.getStatus() == null ||
                event.getStatus()
                        .trim()
                        .isEmpty()
        ) {

            event.setStatus(
                    "Upcoming"
            );
        }


        List<String> validStatuses =
                List.of(
                        "Upcoming",
                        "Ongoing",
                        "Completed",
                        "Cancelled"
                );


        if (
                !validStatuses.contains(
                        event.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid event status"
            );
        }
    }
}