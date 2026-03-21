package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class RerouteComplaintResponse {
    @SerializedName("success")
    public boolean success;
    @SerializedName("complaint_id")
    public String complaintId;

    @SerializedName("new_status")
    public String newStatus;

    @SerializedName("assigned_to")
    public String newAssignedId;

    @SerializedName("message")
    public String message;
}
