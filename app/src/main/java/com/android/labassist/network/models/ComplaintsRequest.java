package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class ComplaintsRequest {
    @SerializedName("role")
    private String role;

    public ComplaintsRequest(String role){
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
