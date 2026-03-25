package com.android.labassist.network.models;

import com.android.labassist.auth.SessionManager;
import com.google.gson.annotations.SerializedName;

public class LabRequest {
    @SerializedName("role")
    public String role;
    public LabRequest(String role) {
        this.role = role;
    }
    private LabRequest(){}
}
