package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class LabResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("data")
    public DepartmentData data;

    @SerializedName("error")
    public String error;
}