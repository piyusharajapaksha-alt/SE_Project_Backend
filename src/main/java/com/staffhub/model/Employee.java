package com.staffhub.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {

    private Long id;

    private Long companyId;

    private String employeeNumber;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String department;

    private String position;

    private String role;

    private String employmentStatus;

    private LocalDate hireDate;

    private String address;

    private String emergencyContact;

    private BigDecimal salary;

    private String gender;

    public Employee() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(
            String employeeNumber
    ) {
        this.employeeNumber =
                employeeNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(
            String firstName
    ) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(
            String lastName
    ) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(
            String phone
    ) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(
            String department
    ) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(
            String position
    ) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(
            String role
    ) {
        this.role = role;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(
            String employmentStatus
    ) {
        this.employmentStatus =
                employmentStatus;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(
            LocalDate hireDate
    ) {
        this.hireDate = hireDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(
            String address
    ) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(
            String emergencyContact
    ) {
        this.emergencyContact =
                emergencyContact;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(
            BigDecimal salary
    ) {
        this.salary = salary;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(
            String gender
    ) {
        this.gender = gender;
    }
}