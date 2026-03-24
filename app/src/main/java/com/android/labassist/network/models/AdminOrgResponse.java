package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminOrgResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("data")
    public AdminOrgData data;

    public static class AdminOrgData{
        @SerializedName("departments")
        public List<Departments> departments;
    }
}