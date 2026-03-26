package com.android.labassist.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DepartmentEntity;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.network.APICalls;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.AdminOrgResponse;
import com.android.labassist.network.models.CreateDepartmentRequest;
import com.android.labassist.network.models.CreateDepartmentResponse;
import com.android.labassist.network.models.CreateDeviceRequest;
import com.android.labassist.network.models.CreateDeviceResponse;
import com.android.labassist.network.models.CreateLabRequest;
import com.android.labassist.network.models.CreateLabResponse;
import com.android.labassist.network.models.Departments;
import com.android.labassist.network.models.LabModel;
import com.android.labassist.network.models.LabRequest;
import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchitectureRepository {
    private final LabAssistDao dao;
    private final APICalls apiCalls;
    private final SessionManager sessionManager;

    private final ExecutorService executor;

    private Call<AdminOrgResponse> adminOrgCall;
    private Call<CreateDepartmentResponse> createDeptCall;
    private Call<CreateLabResponse> createLabCall;
    private Call<CreateDeviceResponse> createDeviceCall;

    public ArchitectureRepository(Context context){
        dao = AppDatabase.getInstance(context).labAssistDao();
        apiCalls = ApiController.getInstance(context).getAuthApi();
        sessionManager = SessionManager.getInstance(context);

        executor = Executors.newSingleThreadExecutor();
    }

    public interface ApiStatusListener {
        void onSuccess(String message);
        void onError(String error);
    }

    // 2. The Fetch Method
    public void fetchAndSaveArchitecture() {
        Log.d("ArchitectureAdmin", "Inside the admin architecture api call : beginning");
        adminOrgCall = apiCalls.getOrgArchitecture(new LabRequest(sessionManager.getRole()));
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
                        parseSupabaseTime(apiDept.createdAt) // Or parse from API
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

    public void createDepartment(String deptName, String deptCode, String deptDesc, ApiStatusListener listener){
        createDeptCall = apiCalls.createDepartment(new CreateDepartmentRequest(deptCode, deptDesc, deptName));
        createDeptCall.enqueue(new Callback<CreateDepartmentResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateDepartmentResponse> call, @NonNull Response<CreateDepartmentResponse> response) {

                        if(response.isSuccessful() && response.body() != null){
                            String deptId = response.body().deptId;
                            long createdAt = parseSupabaseTime(response.body().createdAt);

                            executor.execute(() -> {
                                DepartmentEntity departmentEntity = new DepartmentEntity(deptCode, deptId, true, deptName, createdAt);
                                dao.insertDepartment(departmentEntity);
                            });
                            listener.onSuccess("Department created successfully!");
                        }
                        else if(response.code() == 23505){
                            listener.onError("Department already exists");
                        }
                        else
                            listener.onError("Failed to create department, please try again");
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreateDepartmentResponse> call, @NonNull Throwable t) {
                        Log.d("ArchitectureAdmin", "Error: " + t.getMessage());
                        listener.onError("Network error. Please check your connection.");
                    }
                });
    }

    public long parseSupabaseTime(String supabaseTime) {
        if (supabaseTime == null || supabaseTime.isEmpty()) {
            return System.currentTimeMillis(); // Safe fallback
        }
        try {
            // Instantly parses the ISO 8601 string and converts to milliseconds!
            return Instant.parse(supabaseTime).toEpochMilli();
        } catch (Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    public void createNewLab(String labName, String labCode, String labType, String labTypeOther, String targetDeptId, ApiStatusListener listener) {

        CreateLabRequest request = new CreateLabRequest(labName, labCode, labType, labTypeOther, targetDeptId);

        createLabCall = apiCalls.createLab(request);
        createLabCall.enqueue(new Callback<CreateLabResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateLabResponse> call, @NonNull Response<CreateLabResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    String newLabId = response.body().labId;
                    String localDeptId = (!sessionManager.getAdminLevel().equals(SessionManager.ADMIN_ORG)) ? targetDeptId : "";

                    // Insert into local Room DB immediately!
                    executor.execute(() -> {
                        LabEntity labEntity = new LabEntity();
                        labEntity.id = newLabId;
                        labEntity.deptId = localDeptId; // Links it to the right department list!
                        labEntity.labName = labName;
                        labEntity.labCode = labCode;
                        labEntity.labType = labType;
                        labEntity.isUnderMaintenance = false;

                        dao.insertLab(labEntity);
                        Log.d("Lab", "Lab added inside db");
                    });

                    listener.onSuccess("Lab created successfully!");
                }

                else if(response.code() == 23505){
                    listener.onError("Department already exists");
                }

                else {
                    // Grab the exact error message from the Deno function
                    listener.onError("Failed to create lab. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateLabResponse> call, @NonNull Throwable t) {
                Log.d("ArchitectureAdmin", "Create Lab Error: " + t.getMessage());
                listener.onError("Network error. Please check your connection.");
            }
        });
    }
    public void createNewDevice(String currentLabId, String code, String name, String dbTypeEnum, String typeOther, ApiStatusListener listener) {
        CreateDeviceRequest request = new CreateDeviceRequest(currentLabId, code, name, dbTypeEnum, typeOther);

        createDeviceCall = apiCalls.createDevice(request);
        createDeviceCall.enqueue(new Callback<CreateDeviceResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateDeviceResponse> call, @NonNull Response<CreateDeviceResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    String newDeviceId = response.body().deviceId;

                    // Insert into local Room DB immediately!
                    executor.execute(() -> {
                        DeviceEntity deviceEntity = new DeviceEntity();
                        deviceEntity.id = newDeviceId;
                        deviceEntity.labId = currentLabId;
                        deviceEntity.deviceCode = code;
                        deviceEntity.deviceName = name;
                        deviceEntity.deviceType = dbTypeEnum;
                        deviceEntity.isActive = true;

                        dao.insertDevice(deviceEntity);
                    });

                    listener.onSuccess("Device registered successfully!");

                }

                else if(response.code() == 23505){
                    listener.onError("Department already exists");
                }

                else {
                    // Grab the exact error message from the Deno function
                    listener.onError("Failed to add device. Please try again.");
                    Log.d("AddDevice", "Device type: " + dbTypeEnum);
                    if(response.body() != null)
                        Log.d("AddDevice", "Error msg : " + response.body().error);

                    Log.d("AddDevice", "Error msg : " + response.errorBody());
                    Log.d("AddDevice", "Response: " + response.code() + "Response msg: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateDeviceResponse> call, @NonNull Throwable t) {
                Log.d("ArchitectureAdmin", "Create Lab Error: " + t.getMessage());
                listener.onError("Network error. Please check your connection.");

            }
        });
    }

    public void cancelCalls(){
        if(adminOrgCall != null && !adminOrgCall.isCanceled())
            adminOrgCall.cancel();

        if(createDeptCall != null && !createDeptCall.isCanceled())
            createDeptCall.cancel();

        if(createLabCall != null && !createLabCall.isCanceled())
            createLabCall.cancel();

        if(createDeviceCall != null && !createDeviceCall.isCanceled())
            createDeviceCall.cancel();
    }
}