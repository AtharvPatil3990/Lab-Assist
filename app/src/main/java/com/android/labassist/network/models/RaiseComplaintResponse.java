package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class RaiseComplaintResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("complaint_id")
    private String complaintId;

    @SerializedName("status")
    private String status;

    public RaiseComplaintResponse(String complaintId, String status, boolean success) {
        this.complaintId = complaintId;
        this.status = status;
        this.success = success;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
