package com.android.labassist.network;

import com.android.labassist.network.models.AdminStatsResponse;
import com.android.labassist.network.models.ComplaintsResponse;
import com.android.labassist.network.models.DeviceResponse;
import com.android.labassist.network.models.LabModel;
import com.android.labassist.network.models.LabRequestStudent;
import com.android.labassist.network.models.LabResponseStudent;
import com.android.labassist.network.models.LoginRequest;
import com.android.labassist.network.models.LoginResponse;
import com.android.labassist.network.models.RaiseComplaintRequest;
import com.android.labassist.network.models.RaiseComplaintResponse;
import com.android.labassist.network.models.RefreshSessionRequest;
import com.android.labassist.network.models.UserProfileResponse;
import com.android.labassist.network.models.UsersResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APICalls {
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> getLogin(@Body LoginRequest request);

    @POST("auth/v1/token?grant_type=refresh_token")
    Call<LoginResponse> getAccessToken(@Body RefreshSessionRequest request);

    @GET("functions/v1/get-user-profile")
    Call<UserProfileResponse> getUserProfile();

    // 🎓 STUDENT CALL (Hardcoded select)
    @GET("rest/v1/complaints?select=*,labs(lab_name,lab_code),devices(device_name),technicians(name)")
    Call<List<ComplaintsResponse>> getStudentComplaints(
            @Query("student_id") String studentIdEq
    );

    // 🔧 TECHNICIAN CALL (Hardcoded select)
    @GET("rest/v1/complaints?select=*,labs(lab_name,lab_code),devices(device_name),students(name)")
    Call<List<ComplaintsResponse>> getTechnicianComplaints(
            @Query("assigned_technician_id") String techIdEq
    );

    // 🏢 ADMIN CALL (Hardcoded select)
    @GET("rest/v1/complaints?select=*,labs(lab_name,lab_code),devices(device_name),students(name),technicians(name)")
    Call<List<ComplaintsResponse>> getDepartmentComplaints(
            @Query("labs.department_id") String deptIdEq // e.g., "eq.uuid-here"
    );

    @POST("functions/v1/raise-complaint")
    Call<RaiseComplaintResponse> raiseComplaint(@Body RaiseComplaintRequest request);

    @GET("rest/v1/devices")
    Call<List<DeviceResponse>> getDevicesByDepartment(
            @Query("department_id") String departmentIdFilter, // "eq.YOUR_UUID"
            @Query("select") String select
    );

    @POST("functions/v1/get-lab-statistics")
    Call<AdminStatsResponse> getAdminStatistics(
            @Query("organization_id") String organizationId
    );

    @POST("functions/v1/get-lab-architecture-student")
    Call<LabResponseStudent> getDepartmentArchitecture(@Body LabRequestStudent request);

    @POST("functions/v1/get-students-technicians")
    Call<UsersResponse> getOrgUsers(@Query("organization_id") String organizationId);
}
