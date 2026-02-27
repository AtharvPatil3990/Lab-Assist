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

    @SerializedName("email")
    private String email;

    // Getters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getRole() { return role; }
    public String getLevel() { return level; }
    public ProfileData getProfile() { return profile; }
}