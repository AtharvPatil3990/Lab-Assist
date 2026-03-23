package com.android.labassist.technician;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.repositories.ComplaintRepository;
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

    public void syncLabArchitecture() {
        repository.fetchAndCacheDepartmentArchitecture(SessionManager.ROLE_TECH);
    }

    public void refreshDashboard() {
        repository.refreshComplaintsFromServer();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cancelApiCalls();
    }
}