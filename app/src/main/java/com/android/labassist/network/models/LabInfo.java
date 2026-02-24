package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class LabInfo {
    @SerializedName("lab_name")
    public String name;
    @SerializedName("lab_code")
    public String code;

}
