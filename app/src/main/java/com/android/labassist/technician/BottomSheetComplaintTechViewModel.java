package com.android.labassist.technician;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.ComplaintEntity;

public class BottomSheetComplaintTechViewModel extends AndroidViewModel {
    private LiveData<ComplaintEntity> complaintLiveData;

    public BottomSheetComplaintTechViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(LabAssistDao dao, String complaintId) {
        if (complaintLiveData == null && complaintId != null) {
            // This runs asynchronously and safely in the background!
            complaintLiveData = dao.getLiveComplaintById(complaintId);
        }
    }

    public LiveData<ComplaintEntity> getComplaint() {
        return complaintLiveData;
    }
}
