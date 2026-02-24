package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProfileData {
    // Common across all tables
    @SerializedName("id")
    private String id;

    @SerializedName("auth_user_id")
    private String authUserID;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("organization_name")
    private String organizationName;

    @SerializedName("department_name")
    private String departmentName;

    @SerializedName("department_id")
    private String departmentID;

    @SerializedName("organization_id")
    private String organizationID;

    public String getDepartmentID() {
        return departmentID;
    }

    public void setDepartmentID(String departmentID) {
        this.departmentID = departmentID;
    }

    public String getOrganizationID() {
        return organizationID;
    }

    public void setOrganizationID(String organizationID) {
        this.organizationID = organizationID;
    }

    // Specific to Students
    @SerializedName("roll_number")
    private String rollNumber;

    // Specific to Technicians
    @SerializedName("technician_lab_mapping")
    private List<LabMapping> labMappings;
    @SerializedName("employee_code")
    private String empCode;
    @SerializedName("level")
    private String techLevel;

//    Specific to admin

    @SerializedName("admin_level")
    private String adminLevel;

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    public String getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(String techLevel) {
        this.techLevel = techLevel;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organization) {
        this.organizationName = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LabMapping> getLabMappings() {
        return labMappings;
    }

    public void setLabMappings(List<LabMapping> labMappings) {
        this.labMappings = labMappings;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String department) {
        this.departmentName = department;
    }

    public String getAuthUserID() {
        return authUserID;
    }

    public void setAuthUserID(String authUserID) {
        this.authUserID = authUserID;
    }
}

