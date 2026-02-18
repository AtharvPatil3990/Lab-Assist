package com.android.labassist.technician;

import com.android.labassist.ComplaintStatus;

public class TechComplaint {
    private String title;
    private String description;
    private ComplaintStatus status;
    private String complaintID;
    private String lab;
    private String department;
    private long assignedDate;
    private long lastUpdatedDate;
    private int notesCount;
    private TechNotes latestNote;

    public TechComplaint(long assignedDate, String complaintID, String department, String description, String lab, long lastUpdatedDate, ComplaintStatus status, String title) {
        this.assignedDate = assignedDate;
        this.complaintID = complaintID;
        this.department = department;
        this.description = description;
        this.lab = lab;
        this.lastUpdatedDate = lastUpdatedDate;
        this.status = status;
        this.title = title;
    }
    public TechComplaint(){}


    public int getNotesCount() {
        return notesCount;
    }

    public void setNotesCount(int notesCount) {
        this.notesCount = notesCount;
    }
    public TechNotes getLatestNote() {
        return latestNote;
    }

    public void setLatestNote(TechNotes latestNote) {
        notesCount++;
        this.latestNote = latestNote;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public long getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(long assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getComplaintID() {
        return complaintID;
    }

    public void setComplaintID(String complaintID) {
        this.complaintID = complaintID;
    }

    public String getLab() {
        return lab;
    }

    public void setLab(String lab) {
        this.lab = lab;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
