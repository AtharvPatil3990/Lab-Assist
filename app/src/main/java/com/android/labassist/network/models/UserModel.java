package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("is_active")
    public boolean isActive;

    // --- Student specific fields ---
    @SerializedName("roll_number")
    public String rollNumber;

    // --- Technician specific fields ---
    @SerializedName("employee_code")
    public String employeeCode;

    @SerializedName("level")
    public String level;

    // --- App specific fields (Not in JSON, we set this manually in ViewModel) ---
    public String role;
}
