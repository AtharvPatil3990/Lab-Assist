package com.android.labassist.admin;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.AdminStatsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardViewModel extends AndroidViewModel {
    private final MutableLiveData<AdminStatsResponse.TicketsStats> ticketStats = new MutableLiveData<>();
    private final MutableLiveData<AdminStatsResponse.PerformanceStats> performanceStats = new MutableLiveData<>();
    private final MutableLiveData<AdminStatsResponse.LabsStats> labsStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final Context context;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    // --- GETTERS ---
    public LiveData<AdminStatsResponse.TicketsStats> getTicketStats() {
        return ticketStats;
    }

    public LiveData<AdminStatsResponse.PerformanceStats> getPerformanceStats() {
        return performanceStats;
    }

    public LiveData<AdminStatsResponse.LabsStats> getLabsStats() {
        return labsStats;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // --- ACTIONS ---
    public void fetchStatistics() {
        isLoading.setValue(true);

        SessionManager sessionManager = SessionManager.getInstance(context);

        // Make the API Call
        ApiController.getInstance(context).getAuthApi().getAdminStatistics(sessionManager.getOrganisationId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AdminStatsResponse> call, @NonNull Response<AdminStatsResponse> response) {
                isLoading.setValue(false);

                // Clean, single check for success
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    AdminStatsResponse.StatsData data = response.body().data;

                    // Post all data to the UI streams
                    ticketStats.setValue(data.tickets);
                    performanceStats.setValue(data.performance);
                    labsStats.setValue(data.labs);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminStatsResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                Toast.makeText(context, "Error message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Optional: Handle error (e.g., set an error LiveData string to show a Toast)
            }
        });
    }
}