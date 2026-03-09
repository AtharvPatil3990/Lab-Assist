package com.android.labassist.technician;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.database.entities.NoteEntity;
import com.android.labassist.network.models.CreateNoteRequest;
import com.android.labassist.network.models.CreateNoteResponse;
import com.android.labassist.repositories.NoteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotesViewModel extends ViewModel {

    private NoteRepository repository;

    // The master list directly from the local database
    private LiveData<List<NoteEntity>> dbNotesLiveData;

    private final MutableLiveData<String> noteCreationStatus = new MutableLiveData<>();

    // The filtered list that the UI actually observes and displays
    private final MutableLiveData<List<NoteEntity>> uiDisplayLiveData = new MutableLiveData<>();
    private List<NoteEntity> currentMasterList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int action;
    private LabAssistDao labAssistDao;
    private String currentLabName, currentDeviceName;

    private final MediatorLiveData<String> toolbarTitleLiveData = new MediatorLiveData<>();
    public void init(NoteRepository repository, int action) {
        this.repository = repository;
        this.action = action;
    }

    // 1. The Routing Logic (Device vs Lab)
    public LiveData<List<NoteEntity>> loadNotes(String deviceId, String labId) {
        // If we already loaded the data, don't fetch it again on screen rotation
        if (dbNotesLiveData != null) return dbNotesLiveData;

        if (deviceId != null && !deviceId.isEmpty() && action == ViewTechNotesFragment.actionDeviceNotes) {
            // We are looking at a specific piece of hardware
            dbNotesLiveData = repository.getDeviceNotes(deviceId);
        } else if (labId != null && !labId.isEmpty() && action == ViewTechNotesFragment.actionLabNotes) {
            // We are looking at general lab infrastructure
            dbNotesLiveData = repository.getLabNotes(labId);
        } else {
            Log.e("IllegalState", "Must provide either a deviceId or a labId");
            dbNotesLiveData = new MutableLiveData<>(new ArrayList<>());
        }
        return dbNotesLiveData;
    }

    // 2. The Search Filtering Logic
    public void updateMasterListAndFilter(List<NoteEntity> freshList) {
        this.currentMasterList = freshList;
        applySearchFilter(this.currentSearchQuery);
    }

    public void applySearchFilter(String query) {
        this.currentSearchQuery = query == null ? "" : query.toLowerCase().trim();

        if (currentSearchQuery.isEmpty()) {
            uiDisplayLiveData.setValue(currentMasterList);
            return;
        }

        List<NoteEntity> filteredList = new ArrayList<>();
        for (NoteEntity note : currentMasterList) {
            if (note.noteText.toLowerCase().contains(currentSearchQuery) ||
                    note.authorName.toLowerCase().contains(currentSearchQuery) ||
                    (note.complaintTitle != null && note.complaintTitle.toLowerCase().contains(currentSearchQuery))) {
                filteredList.add(note);
            }
        }
        uiDisplayLiveData.setValue(filteredList);
    }

    public LiveData<List<NoteEntity>> getDisplayNotes() {
        return uiDisplayLiveData;
    }

    public void initArchitectureRepos(LabAssistDao labRepo) {
        this.labAssistDao = labRepo;
    }

    public void loadHeaderTitle(String deviceId, String labId) {
        // Reset state
        currentLabName = "";
        currentDeviceName = "";

        if (deviceId != null && !deviceId.isEmpty() && action == ViewTechNotesFragment.actionDeviceNotes) {
            // SCENARIO 1: Device Notes
            LiveData<String> labNameSource = labAssistDao.getLabNameById(labId);
            LiveData<String> deviceNameSource = labAssistDao.getDeviceNameById(deviceId);

            toolbarTitleLiveData.addSource(labNameSource, labName -> {
                currentLabName = (labName != null) ? labName : "Unknown Lab";
                updateCombinedTitle();
            });

            toolbarTitleLiveData.addSource(deviceNameSource, deviceName -> {
                currentDeviceName = (deviceName != null) ? deviceName : "Unknown Device";
                updateCombinedTitle();
            });

        } else if (labId != null && !labId.isEmpty() && action == ViewTechNotesFragment.actionLabNotes) {
            // SCENARIO 2: Lab Notes
            LiveData<String> labNameSource = labAssistDao.getLabNameById(labId);

            toolbarTitleLiveData.addSource(labNameSource, labName -> {
                // FIX: Actually update the variable so the helper method can use it!
                currentLabName = (labName != null) ? labName : "Unknown Lab";
                updateCombinedTitle();
            });

        } else {
            // PREVENT SILENT FAILURES: If IDs are missing, give the UI a default title
            Log.e("NotesViewModel", "loadHeaderTitle: Missing IDs for the given action!");
            toolbarTitleLiveData.setValue("Notes");
        }
    }

    // Helper method to piece the title together beautifully
    private void updateCombinedTitle() {
        if (action == ViewTechNotesFragment.actionDeviceNotes) {
            // Format: "Dell PC-04 • Computer Lab A"
            if (!currentLabName.isEmpty() && !currentDeviceName.isEmpty()) {
                toolbarTitleLiveData.setValue(currentDeviceName + " • " + currentLabName);
            }
        } else if (action == ViewTechNotesFragment.actionLabNotes) {
            // Format: "Lab: Computer Lab A"
            if (!currentLabName.isEmpty()) {
                toolbarTitleLiveData.setValue("Lab: " + currentLabName);
            } else {
                toolbarTitleLiveData.setValue("Lab Notes");
            }
        }
    }

    public LiveData<String> getNoteCreationStatus() {
        return noteCreationStatus;
    }

    public LiveData<String> getToolbarTitle() {
        return toolbarTitleLiveData;
    }

    // 3. The Create Note Logic
    // 3. The Create Note Logic
    public void createNewNote(Context context, String complaintId, String deviceId, String labId, String noteText, boolean isInternal) {
        // 1. Tell the UI to show the loading overlay
        noteCreationStatus.setValue("LOADING");
        SessionManager sessionManager = SessionManager.getInstance(context);

        CreateNoteRequest request = new CreateNoteRequest(complaintId, deviceId, labId, noteText, isInternal);

        // Pass the request to the repository to execute the Retrofit call.
        repository.createNote(request, new Callback<CreateNoteResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateNoteResponse> call, @NonNull Response<CreateNoteResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {

                    // 2. The network succeeded! Now safely do database work in the background.
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            NoteEntity newNote = new NoteEntity();

                            if (complaintId != null) {
                                ComplaintEntity complaint = labAssistDao.getComplaintById(complaintId);
                                if (complaint != null) {
                                    newNote.complaintTitle = complaint.title; // Attach it to the note!
                                }
                            }

                            newNote.id = response.body().noteId; // Use the UUID generated by Supabase
                            newNote.noteText = noteText;
                            newNote.isInternal = isInternal;
                            newNote.deviceId = deviceId;
                            newNote.labId = labId;
                            newNote.createdByRole = sessionManager.getRole();
                            newNote.authorName = sessionManager.getUsername();
                            newNote.complaintId = complaintId;
                            newNote.createdAt = parseSupabaseTimestamp(response.body().createdAt);

                            // If attached to a complaint, fetch it to attach the title
                            if (complaintId != null) {
                                ComplaintEntity complaint = labAssistDao.getComplaintById(complaintId);
                                if (complaint != null) {
                                    newNote.complaintTitle = complaint.title;
                                }
                            }

                            // 3. Insert into Room Database
                            labAssistDao.insertNote(newNote);

                            // 4. Update the UI. (MUST use postValue from a background thread)
                            noteCreationStatus.postValue("SUCCESS");

                        } catch (Exception e) {
                            Log.e("NotesViewModel", "Failed to insert note locally: " + e.getMessage());
                            noteCreationStatus.postValue("ERROR: Saved to server, but failed to load locally.");
                        }
                    });

                } else {
                    // API returned an error (e.g., HTTP 400 Validation Error)
                    String errorMsg = "Failed to create note";
                    if (response.body() != null && response.body().error != null) {
                        errorMsg = response.body().error;
                    }
                    noteCreationStatus.setValue("ERROR: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateNoteResponse> call, @NonNull Throwable t) {
                // Network completely dropped or timeout
                noteCreationStatus.setValue("ERROR: Network failure. Please check your connection.");
            }
        });
    }

    private long parseSupabaseTimestamp(String timestamp) {
        if (timestamp == null) return System.currentTimeMillis();
        try {
            // If the device is running Android 8.0 (API 26) or higher
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                return java.time.Instant.parse(timestamp).toEpochMilli();
            } else {
                // Fallback for older Android devices
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date date = sdf.parse(timestamp);
                return date != null ? date.getTime() : System.currentTimeMillis();
            }
        } catch (Exception e) {
            Log.e("NotesViewModel", "Failed to parse time: " + timestamp, e);
            return System.currentTimeMillis(); // Safe fallback
        }
    }
}