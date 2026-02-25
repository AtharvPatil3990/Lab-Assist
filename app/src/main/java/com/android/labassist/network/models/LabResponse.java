package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class LabResponse {
    @SerializedName("id") public String id;
    @SerializedName("lab_name") public String labName;
    @SerializedName("lab_id") public String labId; // If this is a secondary ID
    @SerializedName("lab_type") public String labType;
    @SerializedName("is_under_maintenance") public boolean isUnderMaintenance;

}