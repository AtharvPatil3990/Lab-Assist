package com.android.labassist.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "device_notes")
public class NoteEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String noteText;
    public String createdByRole;
    public boolean isInternal;
    public String authorName;
    public String deviceId; // Used to filter
    public String labId;    // Used to filter
    public long createdAt;
    // We flatten the complaint context to keep the local DB simple
    public String complaintId;
    public String complaintTitle;
}