package com.android.labassist.database.entities;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class DeviceEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String labId; // Foreign reference to the Lab
    public String deviceCode;
    public String deviceName;
    public String deviceType;
}
