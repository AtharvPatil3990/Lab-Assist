package com.android.labassist;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.network.APICalls;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.ComplaintsResponse;
import com.android.labassist.network.models.LabModel;
import com.android.labassist.network.models.LabRequestStudent;
import com.android.labassist.network.models.LabResponseStudent;
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

    /**
     * The UI (ViewModel) calls this. It instantly returns whatever is currently in the local Room cache,
     * allowing the UI to load in milliseconds, even without Wi-Fi.
     */
    public LiveData<List<ComplaintEntity>> getActiveComplaints() {
        // Trigger a background network sync every time the UI asks for data
        refreshComplaintsFromServer();

        // Return the local LiveData stream immediately
        return labAssistDao.getActiveComplaints();
    }

    public void refreshComplaintsFromServer(){
        Call<List<ComplaintsResponse>> call;
        SessionManager sessionManager = SessionManager.getInstance(context);
        if (sessionManager.getRole().equals(SessionManager.ROLE_TECH)) {
            call = authApi.getTechnicianComplaints("eq." +sessionManager.getId());
        }
        else {
            call = authApi.getStudentComplaints("eq." +sessionManager.getId());
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<ComplaintsResponse>> call, @NonNull Response<List<ComplaintsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ComplaintsResponse> networkData = response.body();

                    // The "Clean Sync" happens here in the background thread
                    executorService.execute(() -> {
                        List<ComplaintEntity> localEntities = mapToComplaintEntity(networkData, sessionManager);
                        labAssistDao.syncComplaints(localEntities);
                        Log.d("ComplaintRepo", "Synced " + localEntities.size() + " complaints.");
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ComplaintsResponse>> call, @NonNull Throwable t) {
                Log.e("ComplaintRepo", "Offline mode active. Serving cached data.");
            }
        });
    }

    private List<ComplaintEntity> mapToComplaintEntity(List<ComplaintsResponse> networkData, SessionManager sessionManager){
        List<ComplaintEntity> localEntities = new ArrayList<>();

        if (networkData == null) return localEntities;

        for (ComplaintsResponse dto : networkData) {
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
            } else {
                entity.labName = "Unknown Lab";
            }

            if (dto.devices != null) {
                entity.deviceName = dto.devices.deviceName;
            } else {
                entity.deviceName = "General/No Device";
            }

            // 3. Smart User Name Resolution
            // 👨‍🎓 RESOLVE STUDENT NAME
            if (dto.students != null && dto.students.name != null) {
                // API provided it (Tech/Admin view)
                entity.studentName = dto.students.name;
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
            java.util.Date date = supabaseDateFormat.parse(isoDateString);
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
    public void fetchAndCacheDepartmentArchitecture() {
        // 1. Get the department ID from the SessionManager
        String departmentId = SessionManager.getInstance(context).getDepartmentID();

        // Safety check: Don't sync if the user isn't logged in properly
        if (departmentId == null || departmentId.isEmpty()) {
            Log.e("SyncError", "Cannot sync: Department ID is missing.");
            return;
        }

        // Supabase requires "eq." for exact matching in REST API queries

        // 2. Fetch Labs from Supabase
        ApiController.getInstance(context).getAuthApi().getDepartmentArchitecture(new LabRequestStudent(departmentId)).enqueue(new Callback<LabResponseStudent>() {
            @Override
            public void onResponse(@NonNull Call<LabResponseStudent> call, @NonNull Response<LabResponseStudent> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Map and save the Labs to Room
                    saveLabsToDatabase(response.body().data.labs);

                } else {
                    Log.e("SyncError", "Failed to fetch Labs. Code: " + response.code());

                }
            }

            @Override
            public void onFailure(@NonNull Call<LabResponseStudent> call, @NonNull Throwable t) {
                Log.d("SyncError", "onFailure " + t.toString());
                Log.d("SyncError", "onFailure " + t.getMessage());
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