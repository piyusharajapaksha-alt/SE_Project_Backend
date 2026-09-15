package com.staffhub.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingProgram {

    private Long id;

    private String title;
    private String description;

    private String trainer;
    private String category;

    private LocalDate startDate;
    private LocalDate endDate;

    private String location;

    private Integer capacity;

    private List<String> trainingFor = new ArrayList<>();

    private String status;

    /*
     * Employee assignment / registration information.
     *
     * Employee IDs use employee_number values such as:
     * EMP001, EMP002, etc.
     */
    private List<String> assignedEmployeeIds = new ArrayList<>();

    private List<String> registeredEmployeeIds = new ArrayList<>();

    /*
     * Training attendance status.
     *
     * Example:
     * EMP001 -> Present
     * EMP002 -> Absent
     */
    private Map<String, String> attendance = new HashMap<>();

    /*
     * Training completion status.
     *
     * Example:
     * EMP001 -> Completed
     * EMP002 -> Pending
     */
    private Map<String, String> completion = new HashMap<>();


    // ============================================================
    // Basic getters and setters
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


    public String getTrainer() {
        return trainer;
    }

    public void setTrainer(String trainer) {
        this.trainer = trainer;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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


    public List<String> getTrainingFor() {
        return trainingFor;
    }

    public void setTrainingFor(List<String> trainingFor) {
        this.trainingFor = trainingFor;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    // ============================================================
    // Employee assignment
    // ============================================================

    public List<String> getAssignedEmployeeIds() {
        return assignedEmployeeIds;
    }

    public void setAssignedEmployeeIds(
            List<String> assignedEmployeeIds
    ) {
        this.assignedEmployeeIds =
                assignedEmployeeIds != null
                        ? assignedEmployeeIds
                        : new ArrayList<>();
    }


    // ============================================================
    // Employee registration
    // ============================================================

    public List<String> getRegisteredEmployeeIds() {
        return registeredEmployeeIds;
    }

    public void setRegisteredEmployeeIds(
            List<String> registeredEmployeeIds
    ) {
        this.registeredEmployeeIds =
                registeredEmployeeIds != null
                        ? registeredEmployeeIds
                        : new ArrayList<>();
    }


    // ============================================================
    // Attendance
    // ============================================================

    public Map<String, String> getAttendance() {
        return attendance;
    }

    public void setAttendance(
            Map<String, String> attendance
    ) {
        this.attendance =
                attendance != null
                        ? attendance
                        : new HashMap<>();
    }


    // ============================================================
    // Completion
    // ============================================================

    public Map<String, String> getCompletion() {
        return completion;
    }

    public void setCompletion(
            Map<String, String> completion
    ) {
        this.completion =
                completion != null
                        ? completion
                        : new HashMap<>();
    }
}