package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateNoteRequest {
    @SerializedName("complaint_id")
    private String complaintId;

    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("lab_id")
    private String labId;

    @SerializedName("note_text")
    private String noteText;

    @SerializedName("is_internal")
    private boolean isInternal;

    public CreateNoteRequest(String complaintId, String deviceId, String labId, String noteText, boolean isInternal) {
        this.complaintId = complaintId;
        this.deviceId = deviceId;
        this.labId = labId;
        this.noteText = noteText;
        this.isInternal = isInternal;
    }
}