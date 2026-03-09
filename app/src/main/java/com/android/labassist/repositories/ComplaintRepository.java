package com.android.labassist.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.network.APICalls;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.ComplaintsRequest;
import com.android.labassist.network.models.ComplaintsResponse;
import com.android.labassist.network.models.LabModel;
import com.android.labassist.network.models.LabRequest;
import com.android.labassist.network.models.LabResponse;
import com.android.labassist.network.models.RaiseComplaintRequest;
import com.android.labassist.network.models.RaiseComplaintResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplaintRepository {
    private final LabAssistDao labAssistDao;
    private final APICalls authApi;

    private MutableLiveData<ComplaintsResponse.Stats> statsLiveData = new MutableLiveData<>();
    private final Context context;

    // Industrial standard: Use an ExecutorService for background database writes
    // to avoid freezing the UI thread.
    private final ExecutorService executorService;

    private final java.text.SimpleDateFormat supabaseDateFormat;
    public ComplaintRepository(Application application) {

        AppDatabase database = AppDatabase.getInstance(application);
        this.labAssistDao = database.labAssistDao();
        context = application.getApplicationContext();
        // We use the Protected API because fetching complaints requires the User Token
        this.authApi = ApiController.getInstance(application).getAuthApi();
        this.executorService = Executors.newSingleThreadExecutor();

        // Initialize exactly once
        supabaseDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
        supabaseDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    }

    public LiveData<ComplaintsResponse.Stats> getStats() { return statsLiveData; }

    public LiveData<List<ComplaintEntity>> getActiveComplaints() {
        // Trigger a background network sync every time the UI asks for data
        refreshComplaintsFromServer();
//  TODO: Move this refreshComplaintsFromServer() to master Sync
        // Return the local LiveData stream immediately
        return labAssistDao.getActiveComplaints();
    }

    public void refreshComplaintsFromServer() {
        SessionManager sessionManager = SessionManager.getInstance(context);

        Call<ComplaintsResponse> call =
                ApiController
                        .getInstance(context)
                        .getAuthApi().
                        getComplaints(new ComplaintsRequest(sessionManager.getRole()));

        call.enqueue(new Callback<ComplaintsResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComplaintsResponse> call, @NonNull Response<ComplaintsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ComplaintsResponse.Complaint> networkData = response.body().complaints;

                    // The "Clean Sync" happens here in the background thread
                    executorService.execute(() -> {
                        List<ComplaintEntity> localEntities = mapToComplaintEntity(networkData, sessionManager);
                        labAssistDao.syncComplaints(localEntities);
                        Log.d("ComplaintRepo", "Synced " + localEntities.size() + " complaints.");
                    });

                    if(sessionManager.getRole().equals(SessionManager.ROLE_TECH) && response.body().stats!=null){
                        statsLiveData.postValue(response.body().stats);
                    }

                }
                else {

                    Log.e("ComplaintRepo", "Failed to fetch complaints from server.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComplaintsResponse> call, @NonNull Throwable t) {
                Log.e("ComplaintRepo", "Offline mode active. Serving cached data.");
            }
        });

    }
    private List<ComplaintEntity> mapToComplaintEntity(List<ComplaintsResponse.Complaint> networkData, SessionManager sessionManager){
        List<ComplaintEntity> localEntities = new ArrayList<>();

        if (networkData == null) return localEntities;

        for (ComplaintsResponse.Complaint dto : networkData) {
            ComplaintEntity entity = new ComplaintEntity();

            // 1. Map direct flat fields
            entity.id = dto.id;
            entity.title = dto.title;
            entity.description = dto.description;
            entity.status = dto.status;
            entity.priority = dto.priority;

            // Convert Supabase ISO 8601 String timestamp to primitive long for Room sorting
            entity.createdAt = parseTimestamp(dto.createdAt);

            // 2. Map Nested Objects (CRITICAL: Must check for nulls!)
            // Supabase will return null if the foreign key is empty or if it wasn't requested
            if (dto.labs != null) {
                entity.labName = dto.labs.labName;
                entity.labCode = dto.labs.labCode;
                entity.labId = dto.labs.labId;
            } else {
                entity.labName = "Unknown Lab";
            }

            if (dto.devices != null) {
                entity.deviceId = dto.devices.deviceId;
                entity.deviceCode = dto.devices.deviceCode;
                entity.deviceName = dto.devices.deviceName;
            } else {
                entity.deviceName = "General/No Device";
            }

            // 3. Smart User Name Resolution
            // 👨‍🎓 RESOLVE STUDENT NAME
            if (dto.students != null && dto.students.name != null) {
                // API provided it (Tech/Admin view)
                entity.studentName = dto.students.name;
                entity.studentId = dto.students.id;
                entity.studentRollNo = dto.students.rollNumber;
            } else if (SessionManager.ROLE_STUDENT.equals(sessionManager.getRole())) {
                // API omitted it to save bandwidth, so we use the local user's name
                entity.studentName = sessionManager.getUsername();
            } else {
                entity.studentName = "Unknown Student";
            }

            // 🔧 RESOLVE TECHNICIAN NAME
            if (dto.technicians != null && dto.technicians.name != null) {
                // API provided it (Student/Admin view)
                entity.technicianName = dto.technicians.name;
                entity.technicianId = dto.technicians.id;
                entity.technicianEmpCode = dto.technicians.employeeCode;
            } else if (SessionManager.ROLE_TECH.equals(sessionManager.getRole())) {
                // API omitted it, so we use the local tech's name
                entity.technicianName = sessionManager.getUsername();
            } else {
                // If it's a brand new complaint, it might not be assigned yet!
                entity.technicianName = "Unassigned";
            }

            localEntities.add(entity);
        }

        return localEntities;
    }
    private long parseTimestamp(String isoDateString) {
        if (isoDateString == null) return System.currentTimeMillis();
        try {
            // Supabase format example: "2026-02-24T10:05:49.000Z"
            String cleanDate = isoDateString.split("\\.")[0];

            java.util.Date date = supabaseDateFormat.parse(cleanDate);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (java.text.ParseException e) {
            android.util.Log.e("DateParse", "Failed to parse date: " + isoDateString);
            return System.currentTimeMillis();
        }
    }

    public LiveData<Integer> getTotalComplaintsCount() {
        return labAssistDao.getTotalComplaintsCount();
    }

    public LiveData<Integer> getPendingComplaintsCount() {
        return labAssistDao.getPendingComplaintsCount();
    }

    public LiveData<Integer> getResolvedComplaintsCount() {
        return labAssistDao.getResolvedComplaintsCount();
    }

    // Inside your ComplaintRepository.java or a dedicated SyncRepository.java
    public void fetchAndCacheDepartmentArchitecture(String role) {
        // 1. Get the department ID from the SessionManager
        String departmentId = SessionManager.getInstance(context).getDepartmentID();

        // Safety check: Don't sync if the user isn't logged in properly
        if (departmentId == null || departmentId.isEmpty()) {
            Log.e("SyncError", "Cannot sync: Department ID is missing.");
            return;
        }

        // Supabase requires "eq." for exact matching in REST API queries
        Call<LabResponse> labReq = null;
        // 2. Fetch Labs from Supabase
        if(role.equals( SessionManager.ROLE_STUDENT))
            labReq = ApiController.getInstance(context).getAuthApi().getDepartmentArchitecture(new LabRequest(departmentId));
        else if(role.equals(SessionManager.ROLE_TECH)) {
            Log.d("TechLab", "Fetching Architecture for Tech");
            labReq = ApiController.getInstance(context).getAuthApi().getDepartmentArchitecture(new LabRequest());
        }
        if(labReq == null)
            return;

        labReq.enqueue(new Callback<LabResponse>() {
            @Override
            public void onResponse(@NonNull Call<LabResponse> call, @NonNull Response<LabResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Map and save the Labs to Room
                    Log.d("TechLab", "raw" + response.raw().body().toString());
                    Log.d("TechLab", "Response in onResponse: successful, Response body: " + response.body().data.toString());
                    saveLabsToDatabase(response.body().data.labs);

                } else {
                    Log.e("SyncError", "Failed to fetch Labs. Code: " + response.code());
                    Log.d("TechLab", "response fail, Error: " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LabResponse> call, @NonNull Throwable t) {
                Log.e("SyncError", "Failed to fetch Labs. Error: " + t.getMessage());
                Log.d("TechLab", "onFailure: " + t.getMessage());
            }
        });
    }

    private void saveLabsToDatabase(List<LabModel> networkLabs) {
        List<LabEntity> entitiesToSave = new ArrayList<>();

        for (LabModel dto : networkLabs) {
            // Create a new Room Entity
            LabEntity entity = new LabEntity();

            // Map the data over
            entity.id = dto.id;
            entity.labName = dto.labName;
            entity.labId = dto.id;
            entity.labType = dto.labType;
            entity.labCode = dto.labCode;
            entity.isUnderMaintenance = dto.isUnderMaintenance;

            entitiesToSave.add(entity);
        }
        executorService.execute(() -> {
            AppDatabase.getInstance(context).labAssistDao().insertAllLabs(entitiesToSave);
            saveDevicesToDatabase(networkLabs);
        });
    }

    private void saveDevicesToDatabase(List<LabModel> networkLabs) {
        // 1. Create an empty list to hold the translated database objects
        List<DeviceEntity> entitiesToSave = new ArrayList<>();

        for(LabModel lab: networkLabs){
            List<LabModel.DeviceModel> devicesList = lab.getDevicesList();

            for (LabModel.DeviceModel dto : devicesList) {
                // Create a blank Database Entity
                DeviceEntity entity = new DeviceEntity();

                // 3. Map the fields one by one
                entity.id = dto.id;
                entity.labId = lab.id;             // Crucial for the dropdown filtering later!
                entity.deviceId = dto.id;
                entity.deviceName = dto.deviceName;
                entity.deviceType = dto.deviceType;
                entity.deviceCode = dto.deviceCode;

                // 4. Add the finished entity to our list
                entitiesToSave.add(entity);
            }
        }

        executorService.execute(() -> {
            AppDatabase.getInstance(context).labAssistDao().insertAllDevices(entitiesToSave);
        });
    }

    public LiveData<List<LabEntity>> getLabsForLabLive() {
        return AppDatabase.getInstance(context).labAssistDao().getAllLabsForLabLive();
    }

    public LiveData<List<DeviceEntity>> getDevicesForLabLive(String labId) {
        return AppDatabase.getInstance(context).labAssistDao().getDevicesForLabLive(labId);
    }

    public void raiseComplaint(RaiseComplaintRequest request, ComplaintCallback callback) {
        ApiController.getInstance(context).getAuthApi().raiseComplaint(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RaiseComplaintResponse> call, @NonNull Response<RaiseComplaintResponse> response) {
                // 1. Check if the HTTP request was successful (Status code 200-299)
                if (response.isSuccessful() && response.body() != null) {

                    // Pass the parsed response object back to the ViewModel
                    callback.onSuccess(response.body());

                } else {
                    // 2. The server was reached, but rejected the request (e.g., 400 Bad Request)
                    callback.onError("Server error: " + response.code() + " " + response.message());
                    Log.d("RaiseCompError", "OnResponse: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RaiseComplaintResponse> call, @NonNull Throwable t) {
                callback.onError("Network failure: " + t.getLocalizedMessage());
                Log.d("RaiseCompError", "onFailure " + (t.getLocalizedMessage() + " " + t.getMessage()));

            }
        });
    }
    public LiveData<List<ComplaintEntity>> getAllComplaintsHistory() {
        // 1. Trigger a background sync so the history stays up-to-date
        refreshComplaintsFromServer();

        // 2. Return the LiveData stream from Room
        // This allows the UI to update automatically whenever the database changes
        return labAssistDao.getAllComplaintsHistory();
    }

    public static interface ComplaintCallback {
        void onSuccess(RaiseComplaintResponse response);
        void onError(String error);
    }
}