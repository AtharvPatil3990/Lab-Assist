package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class LabRequestStudent {
    @SerializedName("department_id")
    public String departmentId;

    public LabRequestStudent(String departmentId) {
        this.departmentId = departmentId;
    }
}
