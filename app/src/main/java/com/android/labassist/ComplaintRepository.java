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
import com.android.labassist.network.APICalls;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.ComplaintsResponse;

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
}