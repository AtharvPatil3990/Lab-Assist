package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class ComplaintsResponse {
    @SerializedName("id") public String id;
    @SerializedName("title") public String title;
    @SerializedName("description") public String description;
    @SerializedName("status") public String status;
    @SerializedName("priority") public String priority;
    @SerializedName("created_at") public String createdAt;

    // Nested objects matching the Supabase query
    @SerializedName("labs") public LabInfo labs;
    @SerializedName("devices") public DeviceInfo devices;
    @SerializedName("students") public UserInfo students;
    @SerializedName("technicians") public UserInfo technicians;

    public static class LabInfo {
        @SerializedName("lab_name") public String labName;
        @SerializedName("lab_code") public String labCode;
    }
    public static class DeviceInfo { @SerializedName("device_name") public String deviceName; }
    public static class UserInfo { @SerializedName("name") public String name; }
}
