package com.staffhub.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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


    // ============================================================
    // Getters and Setters
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
}