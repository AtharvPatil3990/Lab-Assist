package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RaiseComplaintRequest {
    @SerializedName("student_id")
    private String studentId;

    @SerializedName("organization_id")
    private String organizationId;

    @SerializedName("lab_id")
    private String labId;

    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("priority")
    private String priority;

    @SerializedName("image_paths")
    private List<String> imagePaths;

    // Constructor
    public RaiseComplaintRequest(String studentId, String organizationId, String labId, String deviceId,
                            String title, String description, String priority) {
        this.studentId = studentId;
        this.organizationId = organizationId;
        this.labId = labId;
        this.deviceId = deviceId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.imagePaths = null; // Passing null as requested for now
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
