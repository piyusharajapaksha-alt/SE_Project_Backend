package com.staffhub.controller;

import com.staffhub.model.Event;
import com.staffhub.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin
public class EventController {

    private final EventService eventService;


    public EventController(
            EventService eventService
    ) {
        this.eventService = eventService;
    }


    // ============================================================
    // GET /api/events
    // ============================================================

    @GetMapping
    public List<Event> getAllEvents(
            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    required = false
            )
            String category,

            @RequestParam(
                    required = false
            )
            String status,

            @RequestParam(
                    required = false
            )
            String employeeId
    ) {

        return eventService.getAllEvents(
                search,
                category,
                status,
                employeeId
        );
    }


    // ============================================================
    // GET /api/events/{id}
    // ============================================================

    @GetMapping("/{id}")
    public Event getEventById(
            @PathVariable Long id,

            @RequestParam(
                    required = false
            )
            String employeeId
    ) {

        return eventService.getEventById(
                id,
                employeeId
        );
    }


    // ============================================================
    // POST /api/events
    // ============================================================

    @PostMapping
    public Event createEvent(
            @RequestBody Event event
    ) {

        return eventService.createEvent(
                event
        );
    }


    // ============================================================
    // PUT /api/events/{id}
    // ============================================================

    @PutMapping("/{id}")
    public Event updateEvent(
            @PathVariable Long id,
            @RequestBody Event event
    ) {

        return eventService.updateEvent(
                id,
                event
        );
    }


    // ============================================================
    // DELETE /api/events/{id}
    // ============================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id
    ) {

        eventService.deleteEvent(id);

        return ResponseEntity.noContent()
                .build();
    }


    // ============================================================
    // POST /api/events/{id}/register
    // ============================================================

    @PostMapping("/{id}/register")
    public ResponseEntity<Void> register(
            @PathVariable Long id,
            @RequestBody RegistrationRequest request
    ) {

        eventService.register(
                id,
                request.employeeId()
        );

        return ResponseEntity.ok()
                .build();
    }


    // ============================================================
    // DELETE /api/events/{id}/register/{employeeId}
    // ============================================================

    @DeleteMapping(
            "/{id}/register/{employeeId}"
    )
    public ResponseEntity<Void> unregister(
            @PathVariable Long id,
            @PathVariable String employeeId
    ) {

        eventService.unregister(
                id,
                employeeId
        );

        return ResponseEntity.noContent()
                .build();
    }


    // ============================================================
    // REGISTRATION REQUEST
    // ============================================================

    public record RegistrationRequest(
            String employeeId
    ) {
    }
}