package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
//    private String access_token, refresh_token, email, id, name, msg;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;
    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}