package com.android.labassist.technician;

public class TechNotes {
    private String noteId;
    private String complaintId;      // 🔑 foreign key
    private String text;
    private String technicianId;
    private String technicianName;
    private long timestamp;

    public TechNotes(String complaintId, String noteId, String technicianId, String technicianName, String text, long timestamp) {
        this.complaintId = complaintId;
        this.noteId = noteId;
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
