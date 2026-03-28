package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class UpdateFcmTokenRequest {
    @SerializedName("fcmToken")
    String fcmToken;

    public UpdateFcmTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
