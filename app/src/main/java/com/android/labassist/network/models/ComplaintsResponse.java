package com.android.labassist.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ComplaintsResponse {
    @SerializedName("data")
    public List<Complaint> complaints;

    @SerializedName("stats")
    public Stats stats;

    public static class Stats{
        @SerializedName("assigned")
        public int assigned;
        @SerializedName("ongoing")
        public int ongoing;
        @SerializedName("on_hold")
        public int onHold;
        @SerializedName("high_priority")
        public int highPriority;
        @SerializedName("resolved_today")
        public int resolvedToday;
    }


    public static class Complaint{
        @SerializedName("id") public String id;
        @SerializedName("title") public String title;
        @SerializedName("description") public String description;
        @SerializedName("status") public String status;
        @SerializedName("priority") public String priority;
        @SerializedName("created_at") public String createdAt;
        @SerializedName("updated_at") public String updatedAt;

        // Nested objects matching the Supabase query
        @SerializedName("labs") public LabInfo labs;
        @SerializedName("devices") public DeviceInfo devices;
        @SerializedName("students") public UserInfo students;
        @SerializedName("technicians") public UserInfo technicians;
    }

    public static class LabInfo {
        @SerializedName("id") public String labId;
        @SerializedName("lab_name") public String labName;
        @SerializedName("lab_code") public String labCode;
        @SerializedName("department_id") public String deptId;
    }

    public static class DeviceInfo {
        @SerializedName("id") public String deviceId;
        @SerializedName("device_name") public String deviceName;
        @SerializedName("device_code") public String deviceCode;
    }

    public static class UserInfo {
        @SerializedName("name") public String name;
        @SerializedName("id") public String id;
        @SerializedName("roll_number") public String rollNumber;
        @SerializedName("employee_code") public String employeeCode;
    }
}

//{
//        "id": "e4ec318b-7df7-44e8-8bea-f4b05fbaa42c",
//        "title": "Title",
//        "description": "Dea",
//        "status": "QUEUED",
//        "priority": "LOW",
//        "created_at": "2026-02-28T06:46:47.677927+00:00",

//        "labs": {
//        "id": "a4e7dad4-b8a5-4694-9bdf-a902d468ec2f",
//        "lab_name": "Hardware Lab",
//        "department_id": "d34f47d6-0671-4f9e-b66d-c357f85ccca0"
//        },

//        "devices": {
//        "id": "a5efc0fe-00dc-484c-b204-14c64b60eb96",
//        "device_code": "PC-7",
//        "device_name": "Computer 7"
//        },

//        "students": {
//        "id": "75584fe8-98e7-4c2e-bd6e-8aed529691a0",
//        "name": "Test user stud",
//        "roll_number": "232113830478"
//        },

//        "technicians": {
//        "id": "9142625a-4e84-45de-a9e6-176d7485c3f6",
//        "name": "Test Technician",
//        "employee_code": "emp_test_123"
//        }
//}