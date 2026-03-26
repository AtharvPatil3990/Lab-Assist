package com.android.labassist.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "technicians")
public class TechnicianEntity {
    @PrimaryKey
    @NonNull
    public String id = "";

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "employee_code")
    public String empCode;

    @ColumnInfo(name = "level")
    public String level;

    @ColumnInfo(name = "department_id")
    public String departmentId;

    public TechnicianEntity(String empCode, @NonNull String id, String level, String name, String deptId) {
        this.departmentId = deptId;
        this.empCode = empCode;
        this.id = id;
        this.level = level;
        this.name = name;
    }

    public TechnicianEntity(){}
}
