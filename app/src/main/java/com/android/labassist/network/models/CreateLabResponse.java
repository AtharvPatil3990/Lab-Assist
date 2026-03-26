package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateLabResponse {
    @SerializedName("lab_id")
    public String labId;

    @SerializedName("created_at")
    public String createdAt;

}