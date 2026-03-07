package com.android.labassist.technician;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.database.entities.NoteEntity;
import com.android.labassist.repositories.NoteRepository;

import java.util.ArrayList;
import java.util.List;

public class NotesViewModel extends ViewModel {

    private NoteRepository repository;

    // The master list directly from the local database
    private LiveData<List<NoteEntity>> dbNotesLiveData;

    // The filtered list that the UI actually observes and displays
    private final MutableLiveData<List<NoteEntity>> uiDisplayLiveData = new MutableLiveData<>();
    private List<NoteEntity> currentMasterList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int action;
    private LiveData<DeviceEntity> deviceSource;
    private LiveData<LabEntity> labSource;
    private LabAssistDao labAssistDao;

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
        // Clear any previous sources just in case the ViewModel is reused
        if (deviceSource != null) toolbarTitleLiveData.removeSource(deviceSource);
        if (labSource != null) toolbarTitleLiveData.removeSource(labSource);

        if (deviceId != null && !deviceId.isEmpty()) {

            // Look up the Device from the local Room DB
            deviceSource = labAssistDao.getLiveDeviceById(deviceId);

            toolbarTitleLiveData.addSource(deviceSource, device -> {
                if (device != null && device.deviceName != null) {
                    toolbarTitleLiveData.setValue("Device: " + device.deviceName);
                } else {
                    toolbarTitleLiveData.setValue("Device Notes"); // Fallback if name is missing
                }
            });

        } else if (labId != null && !labId.isEmpty()) {

            // Look up the Lab from the local Room DB
            labSource = labAssistDao.getLiveLabById(labId);

            toolbarTitleLiveData.addSource(labSource, lab -> {
                if (lab != null && lab.labName != null) {
                    toolbarTitleLiveData.setValue("Lab: " + lab.labName);
                } else {
                    toolbarTitleLiveData.setValue("Lab Notes"); // Fallback if name is missing
                }
            });

        } else {
            toolbarTitleLiveData.setValue("Notes"); // Default fallback
        }
    }

    // 3. The function your Fragment will observe!
    public LiveData<String> getToolbarTitle() {
        return toolbarTitleLiveData;
    }

    // 3. The Create Note Logic
    public void createNewNote(String complaintId, String deviceId, String noteText, boolean isInternal) {
//        CreateNoteRequest request = new CreateNoteRequest(complaintId, deviceId, noteText, isInternal);
//
//        // Pass the request to the repository to execute the Retrofit call.
//        repository.createNote(request);
    }
}