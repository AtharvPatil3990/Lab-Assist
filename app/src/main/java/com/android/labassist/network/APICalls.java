package com.android.labassist.network;

import com.android.labassist.network.models.AdminStatsResponse;
import com.android.labassist.network.models.ComplaintsRequest;
import com.android.labassist.network.models.ComplaintsResponse;
import com.android.labassist.network.models.CreateNoteRequest;
import com.android.labassist.network.models.CreateNoteResponse;
import com.android.labassist.network.models.DeviceResponse;
import com.android.labassist.network.models.LabRequest;
import com.android.labassist.network.models.LabResponse;
import com.android.labassist.network.models.LoginRequest;
import com.android.labassist.network.models.LoginResponse;
import com.android.labassist.network.models.NotesRequest;
import com.android.labassist.network.models.NotesResponse;
import com.android.labassist.network.models.RaiseComplaintRequest;
import com.android.labassist.network.models.RaiseComplaintResponse;
import com.android.labassist.network.models.RefreshSessionRequest;
import com.android.labassist.network.models.UpdateComplaintStatusRequest;
import com.android.labassist.network.models.UpdateComplaintStatusResponse;
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

    @POST("functions/v1/get-complaints")
    Call<ComplaintsResponse> getComplaints(@Body ComplaintsRequest request);

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

    @POST("functions/v1/get-lab-architecture")
    Call<LabResponse> getDepartmentArchitecture(@Body LabRequest request);

    @POST("functions/v1/get-students-technicians")
    Call<UsersResponse> getOrgUsers(@Query("organization_id") String organizationId);

    @POST("functions/v1/get-notes")
    Call<NotesResponse> getNotes(@Body NotesRequest notesRequest);

    @POST("functions/v1/create-note")
    Call<CreateNoteResponse> addNote(@Body CreateNoteRequest request);


    @POST("functions/v1/update-complaint-status")
    Call<UpdateComplaintStatusResponse> updateComplaintStatus(@Body UpdateComplaintStatusRequest request);
}
