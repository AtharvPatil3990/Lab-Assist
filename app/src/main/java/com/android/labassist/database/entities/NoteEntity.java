package com.android.labassist.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "complaint_notes")

public class NoteEntity {
    @PrimaryKey
    @NonNull
    public String noteId = "";
    public String complaintId; // Foreign key linking back to the complaint
    public String technicianName;
    public String content;
    public long createdAt;
}
