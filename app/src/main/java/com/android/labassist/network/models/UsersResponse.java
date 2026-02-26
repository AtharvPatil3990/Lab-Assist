package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UsersResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("students")
    public List<UserModel> students;

    @SerializedName("technicians")
    public List<UserModel> technicians;
}