package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class InviteUserResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("error")
    private String errorMessage;

    // Getters and setters for success and message
    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
