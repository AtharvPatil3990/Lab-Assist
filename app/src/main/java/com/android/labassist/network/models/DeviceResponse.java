package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class DeviceResponse {
    @SerializedName("id") public String id;
    @SerializedName("lab_id") public String labId;
    @SerializedName("device_id") public String deviceId;
    @SerializedName("device_name") public String deviceName;
    @SerializedName("device_type") public String deviceType;
}
