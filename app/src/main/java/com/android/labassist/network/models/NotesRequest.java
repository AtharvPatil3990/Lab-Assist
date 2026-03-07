package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class NotesRequest {
    @SerializedName("fetch_type")
    public String fetchType; // "DEVICE" or "LAB"

    @SerializedName("device_id")
    public String deviceId;

    @SerializedName("lab_id")
    public String labId;

    // Constructor for fetching DEVICE notes
    public static NotesRequest forDevice(String deviceId) {
        NotesRequest request = new NotesRequest();
        request.fetchType = "DEVICE";
        request.deviceId = deviceId;
        return request;
    }

    // Constructor for fetching LAB notes
    public static NotesRequest forLab(String labId) {
        NotesRequest request = new NotesRequest();
        request.fetchType = "LAB";
        request.labId = labId;
        return request;
    }
}