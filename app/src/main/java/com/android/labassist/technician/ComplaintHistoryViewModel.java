package com.android.labassist.technician;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.ComplaintRepository;
import com.android.labassist.database.entities.ComplaintEntity;

import java.util.List;

public class ComplaintHistoryViewModel extends AndroidViewModel {
    private final ComplaintRepository repository;
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);


    public ComplaintHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new ComplaintRepository(application);
    }

    public LiveData<List<ComplaintEntity>> getFullHistory() {
        // This will observe the history query we just wrote in the DAO
        return repository.getAllComplaintsHistory();
    }

    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    public void refreshHistory() {
        isRefreshing.setValue(true);
        // Note: You would need to update your Repository's refresh method
        // to take a callback or use an EventBus to tell the ViewModel when it's done.
        repository.refreshComplaintsFromServer();

    }
}