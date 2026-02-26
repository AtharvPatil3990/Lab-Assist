package com.android.labassist.admin;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.UserModel;
import com.android.labassist.network.models.UsersResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersViewModel extends AndroidViewModel {

    // LiveData streams to hold the separate lists of users
    private final MutableLiveData<List<UserModel>> studentsList = new MutableLiveData<>();
    private final MutableLiveData<List<UserModel>> techniciansList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final Context context;

    public UsersViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    // --- GETTERS for the Fragment to observe ---
    public LiveData<List<UserModel>> getStudentsList() { return studentsList; }
    public LiveData<List<UserModel>> getTechniciansList() { return techniciansList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // --- MAIN ACTION ---
    public void fetchAllUsers() {
        isLoading.setValue(true);
        SessionManager session = SessionManager.getInstance(context);

        // Supabase REST API requires 'eq.' to filter by exact match
        String orgQuery = "eq." + session.getOrganisationId();

        // 1. Fetch Technicians from the 'technicians' table
        ApiController.getInstance(context).getAuthApi().getOrgUsers(session.getOrganisationId())
                .enqueue(new Callback<UsersResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
                        // 1. Hide the loading spinner
                        isLoading.setValue(false);

                        // 2. Check if the HTTP request was successful and our JSON success flag is true
                        if (response.isSuccessful() && response.body() != null && response.body().success) {

                            List<UserModel> fetchedStudents = response.body().students;
                            List<UserModel> fetchedTechs = response.body().technicians;

                            // 3. Process Students: Tag them so the UI knows to make their badge Orange
                            if (fetchedStudents != null) {
                                for (UserModel student : fetchedStudents) {
                                    student.role = "Student";
                                }
                                studentsList.setValue(fetchedStudents);
                            }

                            // 4. Process Technicians: Tag them so the UI knows to make their badge Blue
                            if (fetchedTechs != null) {
                                for (UserModel tech : fetchedTechs) {
                                    tech.role = "Technician";
                                }
                                techniciansList.setValue(fetchedTechs);
                            }
                        } else {
                            // Optional: Handle logical errors from your Edge Function (e.g., success = false)
                            // Log.e("UsersViewModel", "Failed to fetch users or success flag was false");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UsersResponse> call, @NonNull Throwable t) {
                        // 1. Hide the loading spinner
                        isLoading.setValue(false);

                        // 2. Handle network failures (no internet, server down, timeout)
                        // Log.e("UsersViewModel", "API call failed: " + t.getMessage());
                    }
                });
    }
}