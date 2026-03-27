package com.android.labassist.network;

import com.android.labassist.network.models.AdminOrgResponse;
import com.android.labassist.network.models.AdminRequestQrgId;
import com.android.labassist.network.models.AdminStatsResponse;
import com.android.labassist.network.models.AssignTechToLabRequest;
import com.android.labassist.network.models.AssignTechToLabResponse;
import com.android.labassist.network.models.ComplaintsRequest;
import com.android.labassist.network.models.ComplaintsResponse;
import com.android.labassist.network.models.CreateDepartmentRequest;
import com.android.labassist.network.models.CreateDepartmentResponse;
import com.android.labassist.network.models.CreateDeviceRequest;
import com.android.labassist.network.models.CreateDeviceResponse;
import com.android.labassist.network.models.CreateLabRequest;
import com.android.labassist.network.models.CreateLabResponse;
import com.android.labassist.network.models.CreateNoteRequest;
import com.android.labassist.network.models.CreateNoteResponse;
import com.android.labassist.network.models.DeviceResponse;
import com.android.labassist.network.models.GoogleAuthRequest;
import com.android.labassist.network.models.InviteUserRequest;
import com.android.labassist.network.models.InviteUserResponse;
import com.android.labassist.network.models.LabRequest;
import com.android.labassist.network.models.LabResponse;
import com.android.labassist.network.models.LoginRequest;
import com.android.labassist.network.models.LoginResponse;
import com.android.labassist.network.models.NotesRequest;
import com.android.labassist.network.models.NotesResponse;
import com.android.labassist.network.models.RaiseComplaintRequest;
import com.android.labassist.network.models.RaiseComplaintResponse;
import com.android.labassist.network.models.RefreshSessionRequest;
import com.android.labassist.network.models.RerouteComplaintRequest;
import com.android.labassist.network.models.RerouteComplaintResponse;
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
    Call<AdminStatsResponse> getAdminStatistics(@Body AdminRequestQrgId request);

    @POST("functions/v1/get-lab-architecture")
    Call<LabResponse> getDepartmentArchitecture(@Body LabRequest request);

    @POST("functions/v1/get-lab-architecture")
    Call<AdminOrgResponse> getOrgArchitecture(@Body LabRequest request);

    @POST("functions/v1/get-students-technicians")
    Call<UsersResponse> getOrgUsers(@Body AdminRequestQrgId requestQrgId);

    @POST("functions/v1/get-notes")
    Call<NotesResponse> getNotes(@Body NotesRequest notesRequest);

    @POST("functions/v1/create-note")
    Call<CreateNoteResponse> addNote(@Body CreateNoteRequest request);

    @POST("functions/v1/update-complaint-status")
    Call<UpdateComplaintStatusResponse> updateComplaintStatus(@Body UpdateComplaintStatusRequest request);

    @POST("functions/v1/reroute-complaint")
    Call<RerouteComplaintResponse> rerouteComplaint(@Body RerouteComplaintRequest request);

    @POST("functions/v1/create-department")
    Call<CreateDepartmentResponse> createDepartment(@Body CreateDepartmentRequest request);

    @POST("functions/v1/create-lab")
    Call<CreateLabResponse> createLab(@Body CreateLabRequest request);

    @POST("functions/v1/create-device")
    Call<CreateDeviceResponse> createDevice(@Body CreateDeviceRequest request);

    @POST("functions/v1/assign-technician-to-lab")
    Call<AssignTechToLabResponse> assignTechToLab(@Body AssignTechToLabRequest request);

    @POST("auth/v1/token?grant_type=id_token")
    Call<LoginResponse> signInWithGoogle(@Body GoogleAuthRequest request);

    @POST("functions/v1/send-invite")
    Call<InviteUserResponse> inviteUser(@Body InviteUserRequest request);
}