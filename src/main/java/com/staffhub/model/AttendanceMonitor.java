package com.staffhub.model;

import java.time.LocalDateTime;

public class AttendanceMonitor {

    private Long id;

    private String activationCode;

    private boolean active;

    private String activationType;

    private String activatedBy;

    private LocalDateTime activatedAt;
    private LocalDateTime deactivatedAt;

    private String currentQrToken;

    private int qrSequence;

    private LocalDateTime qrCreatedAt;
    private LocalDateTime qrExpiresAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getActivationType() {
        return activationType;
    }

    public void setActivationType(String activationType) {
        this.activationType = activationType;
    }

    public String getActivatedBy() {
        return activatedBy;
    }

    public void setActivatedBy(String activatedBy) {
        this.activatedBy = activatedBy;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(LocalDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public String getCurrentQrToken() {
        return currentQrToken;
    }

    public void setCurrentQrToken(String currentQrToken) {
        this.currentQrToken = currentQrToken;
    }

    public int getQrSequence() {
        return qrSequence;
    }

    public void setQrSequence(int qrSequence) {
        this.qrSequence = qrSequence;
    }

    public LocalDateTime getQrCreatedAt() {
        return qrCreatedAt;
    }

    public void setQrCreatedAt(LocalDateTime qrCreatedAt) {
        this.qrCreatedAt = qrCreatedAt;
    }

    public LocalDateTime getQrExpiresAt() {
        return qrExpiresAt;
    }

    public void setQrExpiresAt(LocalDateTime qrExpiresAt) {
        this.qrExpiresAt = qrExpiresAt;
    }
}