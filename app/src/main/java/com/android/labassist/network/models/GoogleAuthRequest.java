package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class GoogleAuthRequest {
    @SerializedName("id_token")
    public String idToken;

    @SerializedName("provider")
    public String provider = "google";

    public GoogleAuthRequest(String idToken) {
        this.idToken = idToken;
    }
}