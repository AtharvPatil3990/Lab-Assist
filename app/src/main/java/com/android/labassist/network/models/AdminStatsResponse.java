package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

public class AdminStatsResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("data")
    public StatsData data;

    // Nested Classes for the JSON structure
    public static class StatsData {
        @SerializedName("level")
        public String level;

        @SerializedName("tickets")
        public TicketsStats tickets;

        @SerializedName("labs")
        public LabsStats labs;

        @SerializedName("performance")
        public PerformanceStats performance;
    }

    public static class TicketsStats {
        @SerializedName("total") public int total;
        @SerializedName("open") public int open;
        @SerializedName("queued") public int queued;
        @SerializedName("resolved") public int resolved;
        @SerializedName("in_progress") public int inProgress;
    }

    public static class LabsStats {
        @SerializedName("total") public int total;
        @SerializedName("active") public int active;
        @SerializedName("maintenance") public int maintenance;
    }

    public static class PerformanceStats {
        @SerializedName("avg_resolution_hours") public double avgResolutionHours;
        @SerializedName("total_downtime_hours") public double totalDowntimeHours;
    }
}