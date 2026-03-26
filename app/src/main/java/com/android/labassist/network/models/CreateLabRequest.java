package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateLabRequest {
    @SerializedName("lab_name")
    private String labName;

    @SerializedName("lab_type")
    private String labType;

    @SerializedName("lab_code")
    private String labCode;

    @SerializedName("lab_type_other")
    private String labTypeOther;

    @SerializedName("target_department_id")
    private String targetDeptId;

    public CreateLabRequest(String labCode, String labName, String labType) {
        this.labCode = labCode;
        this.labName = labName;
        this.labType = labType;
    }

    public CreateLabRequest(String labCode, String labName, String labType, String labTypeOther) {
        this.labCode = labCode;
        this.labName = labName;
        this.labType = labType;
        this.labTypeOther = labTypeOther;
    }

    public void setTargetDeptId(String deptId){
        this.targetDeptId = deptId;
    }

    public CreateLabRequest(String labName, String labCode, String labType, String labTypeOther, String targetDepartmentId) {
        this.labName = labName;
        this.labCode = labCode;
        this.labType = labType;
        this.labTypeOther = labTypeOther;
        this.targetDeptId = targetDepartmentId;
    }
}
