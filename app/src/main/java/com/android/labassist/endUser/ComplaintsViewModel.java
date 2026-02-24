package com.android.labassist.endUser;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.android.labassist.ComplaintRepository;
import com.android.labassist.ComplaintStatus;
import com.android.labassist.database.entities.ComplaintEntity;

import java.util.ArrayList;
import java.util.List;
public class ComplaintsViewModel extends AndroidViewModel{
    private final LiveData<Integer> totalCount;
    private final LiveData<Integer> pendingCount;
    private final LiveData<Integer> resolvedCount;
    private final ComplaintRepository repository;
    private final LiveData<List<ComplaintEntity>> allComplaints;

    public ComplaintsViewModel(@NonNull Application application) {
        super(application);

        repository = new ComplaintRepository(application);

        // As soon as this ViewModel is created, it grabs the LiveData from Room
        // and triggers the background network sync!
        allComplaints = repository.getActiveComplaints();

        totalCount = repository.getTotalComplaintsCount();
        pendingCount = repository.getPendingComplaintsCount();
        resolvedCount = repository.getResolvedComplaintsCount();
    }

    // Your Fragments will call this to observe the data
    public LiveData<List<ComplaintEntity>> getAllComplaints() {
        return allComplaints;
    }

    // You can call this later if you add a "Pull to Refresh" swipe layout
    public void forceRefresh() {
        repository.refreshComplaintsFromServer();
    }

    public LiveData<Integer> getTotalCount() { return totalCount; }
    public LiveData<Integer> getPendingCount() { return pendingCount; }
    public LiveData<Integer> getResolvedCount() { return resolvedCount; }

    public LiveData<List<UserComplaint>> getComplaintsForUi() {
        return Transformations.map(allComplaints, entities -> {
            List<UserComplaint> uiList = new ArrayList<>();
            if (entities != null) {
                for (ComplaintEntity entity : entities) {
                    // Map the Room Database fields to your specific UserComplaint constructor fields
                    uiList.add(new UserComplaint(
                            entity.labName,                        // Maps to: String labName
                            entity.deviceName,                     // Maps to: String pc
                            entity.title,                          // Maps to: String issue
                            mapStatusStringToEnum(entity.status),  // Maps to: ComplaintStatus status
                            entity.createdAt                       // Maps to: long reportedDate
                    ));
                }
            }
            return uiList;
        });
    }

    // 2. THE ENUM CONVERTER
    private ComplaintStatus mapStatusStringToEnum(String dbStatus) {
        if (dbStatus == null) return ComplaintStatus.Pending;
        switch (dbStatus.toUpperCase()) {
            case "RESOLVED":
                return ComplaintStatus.Resolved;
            case "IN_PROGRESS":
                return ComplaintStatus.Ongoing;
            case "OPEN":
            default:
                return ComplaintStatus.Pending;
        }
    }
}
