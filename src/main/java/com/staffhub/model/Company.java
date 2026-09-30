package com.staffhub.model;

import java.time.LocalDateTime;

public class Company {
    private Long id;
    private String companyCode;
    private String companyName;
    private String email;
    private String phone;
    private String address;
    private String industry;
    private String status;
    private Long ownerEmployeeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Company() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOwnerEmployeeId() { return ownerEmployeeId; }
    public void setOwnerEmployeeId(Long ownerEmployeeId) { this.ownerEmployeeId = ownerEmployeeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
