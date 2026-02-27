package com.android.labassist.endUser;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.android.labassist.ComplaintRepository;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.network.models.RaiseComplaintRequest;
import com.android.labassist.network.models.RaiseComplaintResponse;


import java.util.List;

public class RaiseComplaintViewModel extends AndroidViewModel {

    private final ComplaintRepository repository;

    // UI State for the submission (Loading/Success/Error)
    private final MutableLiveData<String> submissionStatus = new MutableLiveData<>();

    // Holds the currently selected Lab ID to trigger the Device fetch
    private final MutableLiveData<String> selectedLabId = new MutableLiveData<>();

    public RaiseComplaintViewModel(@NonNull Application application) {
        super(application);
        repository = new ComplaintRepository(application);
    }

    // --- 1. Dropdown Data Streams ---

    public LiveData<List<LabEntity>> getAvailableLabs() {
        return repository.getLabsForLabLive();
    }

    public void selectLab(String labId) {
        selectedLabId.setValue(labId);
    }

    public LiveData<List<DeviceEntity>> getAvailableDevices() {
        // switchMap automatically fetches new devices whenever selectedLabId changes
        return Transformations.switchMap(selectedLabId, repository::getDevicesForLabLive
        );
    }

    // --- 2. Submission Logic ---
    public LiveData<String> getSubmissionStatus() {
        return submissionStatus;
    }

    public void submitComplaint(RaiseComplaintRequest request) {
        submissionStatus.setValue("LOADING");

        repository.raiseComplaint(request, new ComplaintRepository.ComplaintCallback() {
            @Override
            public void onSuccess(RaiseComplaintResponse response) {
                if (response.isSuccess()) {
                    submissionStatus.postValue("SUCCESS");
                } else {
                    submissionStatus.postValue("ERROR: Server returned failure.");
                }
            }

            @Override
            public void onError(String error) {
                submissionStatus.postValue("ERROR: " + error);
            }
        });
    }

    public void resetStatus() {
        submissionStatus.setValue(null);
    }
}