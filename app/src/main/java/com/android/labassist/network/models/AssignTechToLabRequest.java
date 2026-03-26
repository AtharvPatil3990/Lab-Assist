package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class AssignTechToLabRequest {
    @SerializedName("lab_id")
    public String labId;

    @SerializedName("technician_id")
    public String techId;

    @SerializedName("is_primary")
    public boolean isPrimary;

    public AssignTechToLabRequest(String techId, String labId, boolean isPrimary) {
        this.isPrimary = isPrimary;
        this.labId = labId;
        this.techId = techId;
    }
}
