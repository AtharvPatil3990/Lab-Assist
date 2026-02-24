package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserProfileResponse {
    @SerializedName("role")
    private String role; // STUDENT, TECHNICIAN, ADMIN

    @SerializedName("level")
    private String level; // Only present for ADMIN

    @SerializedName("profile")
    private ProfileData profile;

    // Getters
    public String getRole() { return role; }
    public String getLevel() { return level; }
    public ProfileData getProfile() { return profile; }
}