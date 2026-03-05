package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

// For technician
public class LabMapping {
    @SerializedName("is_primary")
    public boolean isPrimary;
    @SerializedName("labs")
    public LabInfo labs;


    public static class LabInfo {
        @SerializedName("lab_name")
        public String name;
        @SerializedName("lab_code")
        public String code;

    }
}
