package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateDepartmentRequest {
    @SerializedName("name")
    private String deptName;

    @SerializedName("department_code")
    private String deptCode;

    @SerializedName("description")
    private String deptDesc;

    public CreateDepartmentRequest(String deptCode, String deptDesc, String deptName) {
        this.deptCode = deptCode;
        this.deptDesc = deptDesc;
        this.deptName = deptName;
    }
}
