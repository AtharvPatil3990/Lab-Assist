package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class UpdateComplaintStatusRequest {
    @SerializedName("complaint_id")
    String complaintId;
    @SerializedName("new_status")
    String newStatus;

    @SerializedName("reason")
    String reason;

    @SerializedName("after_image_path")
    String afterImagePath;

    public UpdateComplaintStatusRequest(String complaintId, String newStatus){
        this.complaintId = complaintId;
        this.newStatus = newStatus;
    }

    public UpdateComplaintStatusRequest(String complaintId, String newStatus, String reason) {
        this.complaintId = complaintId;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    public UpdateComplaintStatusRequest(String complaintId, String newStatus, String reason, String afterImagePath) {
        this.afterImagePath = afterImagePath;
        this.complaintId = complaintId;
        this.newStatus = newStatus;
        this.reason = reason;
    }
}
