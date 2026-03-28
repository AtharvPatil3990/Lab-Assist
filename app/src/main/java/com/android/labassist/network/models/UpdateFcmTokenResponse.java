package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class UpdateFcmTokenResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("error")
    public String error;
}
