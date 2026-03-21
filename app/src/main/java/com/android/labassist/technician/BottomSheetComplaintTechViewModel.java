package com.android.labassist.technician;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.RerouteComplaintRequest;
import com.android.labassist.network.models.RerouteComplaintResponse;
import com.android.labassist.network.models.UpdateComplaintStatusRequest;
import com.android.labassist.network.models.UpdateComplaintStatusResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetComplaintTechViewModel extends AndroidViewModel {
    private LiveData<ComplaintEntity> complaintLiveData;

    private LabAssistDao dao;
    private ApiController api;

    // UI State Observers
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Background thread for Room database writes
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void init(LabAssistDao dao, ApiController api, String complaintId) {
        this.dao = dao;
        this.api = api;
        if (complaintLiveData == null && complaintId != null) {
            complaintLiveData = dao.getLiveComplaintById(complaintId);
        }
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    // THE UPDATE METHOD
    public void updateComplaintStatus(String complaintId, String newStatus) {
        // 1. Tell the UI to show a loading state
        isLoading.setValue(true);

        api.getAuthApi()
                .updateComplaintStatus(new UpdateComplaintStatusRequest(complaintId, newStatus))
                .enqueue(new Callback<UpdateComplaintStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateComplaintStatusResponse> call, @NonNull Response<UpdateComplaintStatusResponse> response) {
                        isLoading.setValue(false); // Turn off loading state

                        if (response.isSuccessful()) {
                            // 3. SUCCESS! The server accepted the change.
                            // Now update the local Room database on a background thread.
                            executorService.execute(() -> {
                                dao.updateComplaintStatus(complaintId, newStatus);
                            });
                            errorMessage.setValue("Complaint updated successfully");
                        } else {
                            // The server rejected it (e.g., 400 or 500 error)
                            errorMessage.setValue("Failed to update status on server. Code: " + response.code());
                            Log.e("ViewModel", "API Error: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UpdateComplaintStatusResponse> call, @NonNull Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Network error. Please check your connection.");
                        Log.e("ViewModel", "Network Error: " + t.getMessage());
                    }
                });
    }
    public void updateComplaintStatusWithReason(String complaintId, String newStatus, String reason) {
            // 1. Tell the UI to show a loading state
            isLoading.setValue(true);

            api.getAuthApi()
                    .updateComplaintStatus(new UpdateComplaintStatusRequest(complaintId, newStatus, reason))
                    .enqueue(new Callback<UpdateComplaintStatusResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpdateComplaintStatusResponse> call, @NonNull Response<UpdateComplaintStatusResponse> response) {
                            isLoading.setValue(false); // Turn off loading state

                            if (response.isSuccessful()) {
                                // 3. SUCCESS! The server accepted the change.
                                // Now update the local Room database on a background thread.
                                executorService.execute(() -> {
                                    dao.updateComplaintStatus(complaintId, newStatus);
                                });
                                errorMessage.setValue("Complaint updated successfully");
                            } else {
                                // The server rejected it (e.g., 400 or 500 error)
                                errorMessage.setValue("Failed to update status on server. Code: " + response.code());
                                Log.e("ViewModel", "API Error: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UpdateComplaintStatusResponse> call, @NonNull Throwable t) {
                            isLoading.setValue(false);
                            errorMessage.setValue("Network error. Please check your connection.");
                            Log.e("ViewModel", "Network Error: " + t.getMessage());
                        }
                    });
        }
        public void updateComplaintStatusWithImagePath(String complaintId, String newStatus, String imagePath) {
            // 1. Tell the UI to show a loading state
            isLoading.setValue(true);

            api.getAuthApi()
                    .updateComplaintStatus(new UpdateComplaintStatusRequest(complaintId, newStatus, "", imagePath))
                    .enqueue(new Callback<UpdateComplaintStatusResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpdateComplaintStatusResponse> call, @NonNull Response<UpdateComplaintStatusResponse> response) {
                            isLoading.setValue(false); // Turn off loading state

                            if (response.isSuccessful()) {
                                // 3. SUCCESS! The server accepted the change.
                                // Now update the local Room database on a background thread.
                                executorService.execute(() -> {
                                    dao.updateComplaintStatus(complaintId, newStatus);
                                });
                                errorMessage.setValue("Complaint updated successfully");
                            } else {
                                // The server rejected it (e.g., 400 or 500 error)
                                errorMessage.setValue("Failed to update status on server. Code: " + response.code());
                                Log.e("ViewModel", "API Error: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UpdateComplaintStatusResponse> call, @NonNull Throwable t) {
                            isLoading.setValue(false);
                            errorMessage.setValue("Network error. Please check your connection.");
                            Log.e("ViewModel", "Network Error: " + t.getMessage());
                        }
                    });
        }

        public void rerouteAssignedComplaint(String complaintId, String reason){
            // 1. Tell the UI to show a loading state
            isLoading.setValue(true);

            api.getAuthApi()
                    .rerouteComplaint(new RerouteComplaintRequest(complaintId, reason))
                    .enqueue(new Callback<RerouteComplaintResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<RerouteComplaintResponse> call, @NonNull Response<RerouteComplaintResponse> response) {
                            isLoading.setValue(false);
                            if(response.isSuccessful() && response.body() != null){
                                RerouteComplaintResponse rerouteResponse = response.body();
                                if(rerouteResponse.newStatus.equals("OPEN")){
                                    executorService.execute(() -> {
                                        dao.deleteComplaintWithId(complaintId);
                                    });
                                    errorMessage.setValue("Complaint reassigned successfully");
                                }
                                else errorMessage.setValue("No technicians available. Moved to Queue!");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<RerouteComplaintResponse> call, @NonNull Throwable t) {

                        }
                    });
        }

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