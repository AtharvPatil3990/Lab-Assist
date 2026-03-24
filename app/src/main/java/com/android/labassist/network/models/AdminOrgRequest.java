package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class AdminOrgRequest {
    @SerializedName("role")
    public String role;

    @SerializedName("organization_id")
    public String orgId;

    @SerializedName("admin_level")
    public String adminLevel;

    public AdminOrgRequest(String adminLevel, String orgId, String role) {
        this.adminLevel = adminLevel;
        this.orgId = orgId;
        this.role = role;
    }
}
