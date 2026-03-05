package com.android.labassist.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "complaints")
public class ComplaintEntity {
    @PrimaryKey
    @NonNull
    public String id = "";

    // Core Data
    public String title;
    public String description;
    public String status;   // OPEN, IN_PROGRESS, RESOLVED
    public String priority; // HIGH, MEDIUM, LOW
    public long createdAt;  // Store timestamps as longs for easy sorting

    // Flattened Relational Data (Saves complex SQL JOINs on the phone)
    public String labId;
    public String labName;
    public String labCode;

    public String deviceId;
    public String deviceName;
    public String deviceCode;
    public String studentName;
    public String studentId;
    public String studentRollNo;
    public String technicianName;
    public String technicianEmpCode;
    public String technicianId;

    public ComplaintEntity(){}

    public ComplaintEntity(String title, long createdAt, String description, String deviceCode, String deviceId, String deviceName, @NonNull String id, String labCode, String labId, String labName, String priority, String status, String studentId, String studentName, String studentRollNo) {
        this.createdAt = createdAt;
        this.description = description;
        this.deviceCode = deviceCode;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.id = id;
        this.labCode = labCode;
        this.labId = labId;
        this.labName = labName;
        this.priority = priority;
        this.status = status;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.title = title;
    }

    public ComplaintEntity(long createdAt, String description, String deviceCode, String deviceId, String deviceName, @NonNull String id, String labCode, String labId, String labName, String priority, String status, String technicianEmpCode, String technicianId, String technicianName, String title) {
        this.createdAt = createdAt;
        this.description = description;
        this.deviceCode = deviceCode;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.id = id;
        this.labCode = labCode;
        this.labId = labId;
        this.labName = labName;
        this.priority = priority;
        this.status = status;
        this.technicianEmpCode = technicianEmpCode;
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.title = title;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public String getLabCode() {
        return labCode;
    }

    public void setLabCode(String labCode) {
        this.labCode = labCode;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
