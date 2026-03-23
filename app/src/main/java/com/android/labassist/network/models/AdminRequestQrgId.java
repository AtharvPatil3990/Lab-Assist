package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class AdminRequestQrgId {
    @SerializedName("organization_id")
    String organizationId;

    public AdminRequestQrgId(String organizationId) {
        this.organizationId = organizationId;
    }
}
