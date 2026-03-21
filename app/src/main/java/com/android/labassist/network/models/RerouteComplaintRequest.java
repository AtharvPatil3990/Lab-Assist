package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class RerouteComplaintRequest {
    @SerializedName("complaint_id")
    private String complaint_id;

    @SerializedName("reason")
    private String reason;

    public RerouteComplaintRequest(String complaint_id, String reason) {
        this.complaint_id = complaint_id;
        this.reason = reason;
    }
}
