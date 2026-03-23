package com.android.labassist.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.NoteEntity;
import com.android.labassist.network.APICalls;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.CreateNoteRequest;
import com.android.labassist.network.models.CreateNoteResponse;
import com.android.labassist.network.models.NotesRequest;
import com.android.labassist.network.models.NotesResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoteRepository {
    private final LabAssistDao noteDao;
    private final APICalls api;
    private final ExecutorService executorService; // For running DB inserts off the main thread

    private Call<NotesResponse> labReqCall, deviceReqCall;

    public NoteRepository(Context context) {

        this.noteDao = AppDatabase.getInstance(context).labAssistDao();
        this.api = ApiController.getInstance(context).getAuthApi();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // 1. This is the only method your ViewModel calls
    public LiveData<List<NoteEntity>> getDeviceNotes(String deviceId) {
        // Trigger the background network sync
        refreshDeviceNotesFromNetwork(deviceId);

        // Instantly return whatever is currently saved on the phone
        return noteDao.getNotesForDevice(deviceId);
    }

    public LiveData<List<NoteEntity>> getLabNotes(String labId) {
        // Trigger the background network sync
        refreshLabNotesFromNetwork(labId);

        // Instantly return whatever is currently saved on the phone
        return noteDao.getNotesForLab(labId);
    }

    // 2. The Background Sync Logic for Labs
    private void refreshLabNotesFromNetwork(String labId) {
        labReqCall = api.getNotes(NotesRequest.forLab(labId));
        labReqCall.enqueue(new Callback<NotesResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotesResponse> call, @NonNull Response<NotesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {

                    // Convert Network models to Local Database entities using our existing mapper
                    List<NoteEntity> freshNotes = mapNetworkToLocal(response.body().data);

                    // Save them to Room on a background thread
                    executorService.execute(() -> {
                        noteDao.insertNotes(freshNotes);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotesResponse> call, @NonNull Throwable t) {
                Log.e("NoteRepo", "Offline or Network Error fetching lab notes: " + t.getMessage());
            }
        });
    }


    // 2. The Background Sync Logic
    private void refreshDeviceNotesFromNetwork(String deviceId) {
        deviceReqCall = api.getNotes(NotesRequest.forDevice(deviceId));
        deviceReqCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<NotesResponse> call, @NonNull Response<NotesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {

                    // Convert the Network models into Local Database entities
                    List<NoteEntity> freshNotes = mapNetworkToLocal(response.body().data);

                    // Save them to Room on a background thread
                    executorService.execute(() -> {
                        noteDao.insertNotes(freshNotes);
                    });

                    // Room will automatically notify the UI that new data was inserted.
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotesResponse> call, @NonNull Throwable t) {
                // If they are offline in a basement, this fails silently.
                // That is perfectly fine, because the UI is already showing the cached local data!
                Log.e("NoteRepo", "Offline or Network Error: " + t.getMessage());
            }
        });
    }

    // Helper to map Retrofit JSON to Room Entities
    private List<NoteEntity> mapNetworkToLocal(List<NotesResponse.Note> networkNotes) {
        List<NoteEntity> localNotes = new ArrayList<>();
        for (NotesResponse.Note netNote : networkNotes) {
            NoteEntity entity = new NoteEntity();
            entity.id = netNote.id;
            entity.noteText = netNote.noteText;
            entity.createdByRole = netNote.createdByRole;
            entity.isInternal = netNote.isInternal;
            entity.authorName = netNote.authorName;
            entity.deviceId = netNote.deviceId;
            entity.labId = netNote.labId;
            entity.complaintId = netNote.complaint.id;
            entity.createdAt = parseSupabaseDateToLong(netNote.createdAt);

            if (netNote.complaint != null) {
                entity.complaintTitle = netNote.complaint.title;
            }
            localNotes.add(entity);
        }
        return localNotes;
    }

    private long parseSupabaseDateToLong(String supabaseDate) {
        if (supabaseDate == null || supabaseDate.isEmpty()) return 0L;
        try {
            // Parses "2026-03-07T10:11:45+00:00" instantly to milliseconds
            return Instant.parse(supabaseDate).toEpochMilli();
        } catch (Exception e) {
            Log.e("DateParse", "Failed to parse date: " + supabaseDate, e);
            return 0L;
        }
    }

    public void createNote(CreateNoteRequest request, Callback<CreateNoteResponse> callback){
        api.addNote(request).enqueue(callback);
    }

    public void cancelApiCalls(){
        if(labReqCall != null && !labReqCall.isCanceled())
            labReqCall.cancel();

        if(deviceReqCall != null && !deviceReqCall.isCanceled())
            deviceReqCall.cancel();
    }
}