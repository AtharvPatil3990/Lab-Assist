package com.android.labassist.technician;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.ComplaintEntity;

import java.util.List;

public class TechComplaintsViewModel extends ViewModel {

    private LiveData<List<ComplaintEntity>> complaintsLiveData;

    // Constants for routing
    public static final int ACTION_LAB_COMPLAINTS = 1;
    public static final int ACTION_DEVICE_COMPLAINTS = 2;

    public void init(LabAssistDao dao, String labId, String deviceId, int actionType) {
        // Prevent redundant database calls on screen rotation
        if (complaintsLiveData != null) return;

        if (actionType == ACTION_LAB_COMPLAINTS && labId != null) {
            complaintsLiveData = dao.getComplaintsForLab(labId);
        }
        else if (actionType == ACTION_DEVICE_COMPLAINTS && deviceId != null) {
            complaintsLiveData = dao.getComplaintsForDevice(deviceId);
        }
        else {
            Log.e("TechComplaintsViewModel", "Invalid action type or missing IDs!");
        }
    }

    public LiveData<List<ComplaintEntity>> getComplaints() {
        return complaintsLiveData;
    }
}