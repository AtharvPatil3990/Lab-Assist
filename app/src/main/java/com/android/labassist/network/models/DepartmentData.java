package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DepartmentData {
    @SerializedName("labs")
    public List<LabModel> labs;
}