package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateDeviceResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("device_id")
    public String deviceId;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("error")
    public String error;
}