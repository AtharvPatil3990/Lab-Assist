package com.android.labassist.technician;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.LabEntity;

import java.util.List;

public class AssignedLabsViewModel extends ViewModel {
    private LiveData<List<LabEntity>> labsLiveData;

    // Initialize with your DAO, just like you did in NotesViewModel
    public void init(LabAssistDao dao, boolean isAdmin, String deptId) {
        // Fetch the LiveData directly from Room
        if(isAdmin) this.labsLiveData = dao.getLiveLabsByDeptId(deptId);

        else this.labsLiveData = dao.getAllLabsForLabLive();
    }

    // Expose the list to the Fragment
    public LiveData<List<LabEntity>> getAssignedLabs() {
        return labsLiveData;
    }

}
