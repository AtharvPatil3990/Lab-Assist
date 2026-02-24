package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

// For technician
public class LabMapping {
    @SerializedName("is_primary")
    public boolean isPrimary;
    @SerializedName("labs")
    public LabInfo labs;
}
