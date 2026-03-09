package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateNoteResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("note_id")
    public String noteId;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("error")
    public String error;
}