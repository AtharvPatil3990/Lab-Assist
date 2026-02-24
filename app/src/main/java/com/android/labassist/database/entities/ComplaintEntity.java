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

    public String deviceId;
    public String deviceName;
    public String studentName;
    public String technicianName;
}
