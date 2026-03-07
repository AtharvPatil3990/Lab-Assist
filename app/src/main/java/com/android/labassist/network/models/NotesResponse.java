package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotesResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("error")
    public String error;

    @SerializedName("data")
    public List<Note> data;

    // --- INNER STATIC CLASSES ---

    public static class Note {
        @SerializedName("id")
        public String id;

        @SerializedName("note_text")
        public String noteText;

        @SerializedName("created_by")
        public String createdBy;

        @SerializedName("created_by_role")
        public String createdByRole; // "TECHNICIAN" or "ADMIN"

        @SerializedName("is_internal")
        public boolean isInternal; // True if hidden from students

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("device_id")
        public String deviceId; // Nullable (will be null for LAB notes)

        @SerializedName("author_name")
        public String authorName; // The mapped name from our Edge Function

        // Maps the nested "complaints" object from the !inner join
        @SerializedName("complaints")
        public ComplaintContext complaint;
    }

    // This holds the context of the complaint the note was written on
    public static class ComplaintContext {
        @SerializedName("id")
        public String id;

        @SerializedName("title")
        public String title;

        @SerializedName("status")
        public String status;

        @SerializedName("lab_id")
        public String labId;
    }
}
