package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Departments{
    @SerializedName("id")
    public String deptId;

    @SerializedName("name")
    public String deptName;

    @SerializedName("code")
    public String deptCode;

    @SerializedName("description")
    public String description;

    @SerializedName("labs")
    public List<LabModel> labsData;

}
