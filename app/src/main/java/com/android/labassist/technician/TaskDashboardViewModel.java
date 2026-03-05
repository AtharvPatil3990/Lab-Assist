package com.android.labassist.technician;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.labassist.ComplaintRepository;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.network.models.ComplaintsResponse;

import java.util.List;

public class TaskDashboardViewModel extends AndroidViewModel {
    private final ComplaintRepository repository;

    public TaskDashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new ComplaintRepository(application);
    }

    public LiveData<List<ComplaintEntity>> getAssignedComplaints() {
        return repository.getActiveComplaints();
    }

    public LiveData<ComplaintsResponse.Stats> getStats() {
        return repository.getStats();
    }

    public void refreshDashboard() {
        repository.refreshComplaintsFromServer();
    }
}
