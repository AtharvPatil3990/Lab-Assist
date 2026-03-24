package com.android.labassist.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "departments")
public class DepartmentEntity {
    @PrimaryKey
    @NonNull
    private String id = "";
    private String name;
    private String code;
    private boolean isActive;
    private long createdAt;

    public DepartmentEntity(String code, @NonNull String id, boolean isActive, String name, long createdAt) {
        this.code = code;
        this.id = id;
        this.isActive = isActive;
        this.name = name;
        this.createdAt = createdAt;
    }

//    Setters
    public void setCode(String code) {this.code = code;}
    public void setId(@NonNull String id) {this.id = id;}
    public void setActive(boolean active) {isActive = active;}
    public void setName(String name) {this.name = name;}
    public void setCreatedAt(long createdAt) {this.createdAt = createdAt;}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public boolean isActive() { return isActive; }
    public long getCreatedAt() { return createdAt; }
}
