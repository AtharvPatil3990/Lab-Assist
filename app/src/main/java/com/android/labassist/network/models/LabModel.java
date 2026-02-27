package com.android.labassist.network.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LabModel {
    @SerializedName("id")
    public String id;

    @SerializedName("lab_code")
    public String labCode;

    @SerializedName("lab_name")
    public String labName;

    @SerializedName("lab_type")
    public String labType;

    @SerializedName("is_active")
    public boolean isActive;

    @SerializedName("is_under_maintenance")
    public boolean isUnderMaintenance;

    // This is the magic part: A list holding the devices!
    @SerializedName("devices")
    public List<DeviceModel> devices;

    @NonNull
    @Override
    public String toString() {
        return labName;
    }

    public List<DeviceModel> getDevicesList(){
        return devices;
    }

    public static class DeviceModel {
        @SerializedName("id")
        public String id;

        @SerializedName("is_active")
        public boolean isActive;

        @SerializedName("device_code")
        public String deviceCode;

        @SerializedName("device_name")
        public String deviceName;

        @SerializedName("device_type")
        public String deviceType;

        // Override toString so if you put this in an Android Spinner, it shows the name nicely
        @NonNull
        @Override
        public String toString() {
            return deviceName + " (" + deviceCode + ")";
        }
    }
}