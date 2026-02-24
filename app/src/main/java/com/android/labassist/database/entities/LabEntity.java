package com.android.labassist.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "labs")
public class LabEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String labName;
    public String labCode;
    public String labType;
    public boolean isUnderMaintenance;
}