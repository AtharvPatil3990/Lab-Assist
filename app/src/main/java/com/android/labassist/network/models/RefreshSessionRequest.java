package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class RefreshSessionRequest {
    @SerializedName("refresh_token")
    private String refreshToken;

    // Constructor
    public RefreshSessionRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

//    Getter and Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
