package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LabsData {
    @SerializedName("labs")
    public List<LabModel> labs;
}