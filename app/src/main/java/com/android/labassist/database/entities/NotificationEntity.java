package com.android.labassist.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id; // Matches the UUID from Supabase

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "complaint_id")
    private String complaintId; // Used for deep linking to the bottom sheet!

    @ColumnInfo(name = "is_read")
    private boolean isRead;

    @ColumnInfo(name = "created_at")
    private long createdAt; // Stored as Unix timestamp for easy sorting

    // Constructor
    public NotificationEntity(String title, String message, String complaintId, boolean isRead, long createdAt) {
        this.title = title;
        this.message = message;
        this.complaintId = complaintId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
    public NotificationEntity(){}

    // --- Getters and Setters ---
    @NonNull
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}