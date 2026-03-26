package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateDepartmentResponse {
    @SerializedName("department_id")
    public String deptId;

    @SerializedName("created_at")
    public String createdAt;
}