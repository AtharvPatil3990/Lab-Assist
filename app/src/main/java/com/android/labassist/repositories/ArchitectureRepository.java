package com.android.labassist.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DepartmentEntity;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.network.APICalls;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.AdminOrgRequest;
import com.android.labassist.network.models.AdminOrgResponse;
import com.android.labassist.network.models.Departments;
import com.android.labassist.network.models.LabModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchitectureRepository {
    private Context context;
    private final LabAssistDao dao;
    private final APICalls apiCalls;
    private final SessionManager sessionManager;

    private final ExecutorService executor;

    private Call<AdminOrgResponse> adminOrgCall;

    public LiveData<String> errorMessage;
    public ArchitectureRepository(Context context){
        this.context = context;
        dao = AppDatabase.getInstance(context).labAssistDao();
        apiCalls = ApiController.getInstance(context).getAuthApi();
        sessionManager = SessionManager.getInstance(context);

        executor = Executors.newSingleThreadExecutor();
    }

    public interface ArchitectureFetchListener {
        void onSuccess(AdminOrgResponse response);
        void onError(String errorMessage);
    }

    // 2. The Fetch Method
    public void fetchAndSaveArchitecture(String orgId, String role, String adminLevel) {
        AdminOrgRequest request = new AdminOrgRequest(orgId, role, adminLevel);

        Log.d("ArchitectureAdmin", "Inside the admin architecture api call : beginning");
        adminOrgCall = apiCalls.getOrgArchitecture(request);
        adminOrgCall.enqueue(new Callback<AdminOrgResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminOrgResponse> call, @NonNull Response<AdminOrgResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    Log.d("ArchitectureAdmin", "Inside the admin architecture api call : success");
                    // Run your Room DB mapping method here!
                    Log.d("ArchitectureAdmin", "response code: "+ response.isSuccessful());

                    mapAndSaveArchitecture(response.body());

                } else {
                    // Extract the exact 400/500 error from Supabase
                    try {
                        String serverError = response.errorBody() != null ? response.errorBody().string() : "Unknown Server Error";
                        Log.d("ArchitectureAdmin", "Error: " + serverError);
                    } catch (Exception e) {
                        Log.d("ArchitectureAdmin", "Error Message: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminOrgResponse> call, @NonNull Throwable t) {
                // This is where GSON parsing crashes or Network Timeouts are caught!
                Log.d("ArchitectureAdmin", "Error: " + t.getMessage());
            }
        });
    }


    public void mapAndSaveArchitecture(AdminOrgResponse response) {
        Log.d("ArchitectureAdmin", "Inside the mapping function");

        if (response == null || !response.success || response.data == null || response.data.departments == null) {
            Log.d("ArchitectureAdmin", "Nothing in response");
            return;
        }

        executor.execute(() -> {
            // 1. Prepare the empty buckets for our Room Entities
            List<DepartmentEntity> departmentEntities = new ArrayList<>();
            List<LabEntity> labEntities = new ArrayList<>();
            List<DeviceEntity> deviceEntities = new ArrayList<>();

            // 2. Loop through the API Response
            for (Departments apiDept : response.data.departments) {

                // Map Department
                DepartmentEntity deptEntity = new DepartmentEntity(
                        apiDept.deptCode,
                        apiDept.deptId,
                        true, // Defaulting to true, or map from API if you add it later
                        apiDept.deptName,
                        System.currentTimeMillis() // Or parse from API
                );
                departmentEntities.add(deptEntity);

                // Check if this department has labs
                if (apiDept.labsData != null) {
                    for (LabModel apiLab : apiDept.labsData) {

                        // Map Lab
                        LabEntity labEntity = new LabEntity();
                        labEntity.id = apiLab.id;
                        labEntity.deptId = apiDept.deptId; // Linking it to the parent!
                        labEntity.labName = apiLab.labName;
                        labEntity.labCode = apiLab.labCode;
                        labEntity.labType = apiLab.labType;
                        labEntity.isUnderMaintenance = apiLab.isUnderMaintenance;

                        labEntities.add(labEntity);

                        // Check if this lab has devices
                        if (apiLab.devices != null) {
                            for (LabModel.DeviceModel apiDevice : apiLab.devices) {

                                // Map Device
                                DeviceEntity deviceEntity = new DeviceEntity();
                                deviceEntity.id = apiDevice.id;
                                deviceEntity.deviceId = apiDevice.id;
                                deviceEntity.labId = apiLab.id; // Linking it to the parent!
                                deviceEntity.deviceName = apiDevice.deviceName;
                                deviceEntity.deviceCode = apiDevice.deviceCode;
                                deviceEntity.deviceType = apiDevice.deviceType;

                                deviceEntities.add(deviceEntity);
                            }
                        }
                    }
                }
            }

            // 3. Batch Insert into Room Database (Happens on the background thread!)
            dao.insertDepartments(departmentEntities);
            dao.insertLabs(labEntities);
            dao.insertDevices(deviceEntities);
        });
    }

    public void cancelCalls(){
        if(adminOrgCall != null && !adminOrgCall.isCanceled()){
            adminOrgCall.cancel();
        }
    }

}