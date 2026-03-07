package com.android.labassist.network.models;

import com.android.labassist.auth.SessionManager;
import com.google.gson.annotations.SerializedName;

public class LabRequest {
    @SerializedName("department_id")
    public String departmentId;

    @SerializedName("role")
    public String role;

    public LabRequest(String departmentId) {
        this.departmentId = departmentId;
        this.role = SessionManager.ROLE_STUDENT;
    }
    public LabRequest(){
        this.role = SessionManager.ROLE_TECH;
    }

}
