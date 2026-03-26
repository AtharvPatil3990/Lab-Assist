package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateDeviceRequest {

    @SerializedName("lab_id")
    public String labId;

    @SerializedName("device_code")
    public String deviceCode;

    @SerializedName("device_name")
    public String deviceName;

    @SerializedName("device_type")
    public String deviceType;

    @SerializedName("device_type_other")
    public String deviceTypeOther; // Can be null if not selecting "OTHERS"

    public CreateDeviceRequest(String labId, String deviceCode, String deviceName, String deviceType, String deviceTypeOther) {
        this.labId = labId;
        this.deviceCode = deviceCode;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.deviceTypeOther = deviceTypeOther;
    }
}