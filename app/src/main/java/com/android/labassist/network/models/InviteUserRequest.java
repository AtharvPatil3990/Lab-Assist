package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class InviteUserRequest {
    @SerializedName("email")
    private String email;
    @SerializedName("role")
    private String role;
    @SerializedName("org_id")
    private String orgId;
    @SerializedName("department_id")
    private String deptId;
    @SerializedName("roll_no_or_emp_no")
    private String rollNoOrEmpNo;

    public InviteUserRequest(String email, String rollNoOrEmpNo, String deptId, String orgId, String role) {
        this.deptId = deptId;
        this.email = email;
        this.orgId = orgId;
        this.role = role;
        this.rollNoOrEmpNo = rollNoOrEmpNo;
    }
}
