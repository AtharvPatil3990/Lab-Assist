package com.android.labassist.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class StudentEntity {
    @PrimaryKey
    @NonNull
    public String id = "";

    public String name;
    public String rollNumber;

    public StudentEntity(@NonNull String id, String name, String rollNumber) {
        this.id = id;
        this.name = name;
        this.rollNumber = rollNumber;
    }

    public StudentEntity(){}
}