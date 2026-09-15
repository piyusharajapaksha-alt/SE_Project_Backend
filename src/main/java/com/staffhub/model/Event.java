package com.staffhub.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private Long id;

    private String title;

    private String description;

    private String organizerId;

    private String organizer;

    private String category;

    private LocalDate date;

    private LocalTime time;

    private LocalTime endTime;

    private String location;

    private Integer capacity;

    private String status;

    private Integer registeredCount;

    private Integer availableSeats;

    private List<String> registeredIds = new ArrayList<>();

    private List<Registrant> registrantNames = new ArrayList<>();


    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }


    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }


    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Integer getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(Integer registeredCount) {
        this.registeredCount = registeredCount;
    }


    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }


    public List<String> getRegisteredIds() {
        return registeredIds;
    }

    public void setRegisteredIds(List<String> registeredIds) {
        this.registeredIds = registeredIds;
    }


    public List<Registrant> getRegistrantNames() {
        return registrantNames;
    }

    public void setRegistrantNames(
            List<Registrant> registrantNames
    ) {
        this.registrantNames = registrantNames;
    }


    // ============================================================
    // REGISTRANT
    // ============================================================

    public static class Registrant {

        private String employeeId;

        private String name;

        private String department;


        public Registrant() {
        }


        public Registrant(
                String employeeId,
                String name,
                String department
        ) {
            this.employeeId = employeeId;
            this.name = name;
            this.department = department;
        }


        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(
                String employeeId
        ) {
            this.employeeId = employeeId;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getDepartment() {
            return department;
        }

        public void setDepartment(
                String department
        ) {
            this.department = department;
        }
    }
}