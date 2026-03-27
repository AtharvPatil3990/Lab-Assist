package com.android.labassist.admin;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DepartmentEntity;
import com.android.labassist.database.entities.StudentEntity;
import com.android.labassist.database.entities.TechnicianEntity;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.AdminRequestQrgId;
import com.android.labassist.network.models.UserModel;
import com.android.labassist.network.models.UsersResponse;
import com.android.labassist.repositories.ArchitectureRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersViewModel extends AndroidViewModel {

    // LiveData streams to hold the separate lists of users
    private final MutableLiveData<List<UserModel>> studentsList = new MutableLiveData<>();
    private final MutableLiveData<List<UserModel>> techniciansList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    private final Context context;
    private ArchitectureRepository architectureRepository;

    private LabAssistDao dao;

    public UsersViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        architectureRepository = new ArchitectureRepository(context);
        dao = AppDatabase.getInstance(context).labAssistDao();
    }

    // --- GETTERS for the Fragment to observe ---
    public LiveData<List<UserModel>> getStudentsList() { return studentsList; }
    public LiveData<List<UserModel>> getTechniciansList() { return techniciansList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }

    Call<UsersResponse> getUsersCall;

    // --- MAIN ACTION --- //
    public void fetchAllUsers() {
        isLoading.setValue(true);
        SessionManager session = SessionManager.getInstance(context);

        // 1. Fetch Technicians from the 'technicians' table
        getUsersCall = ApiController.getInstance(context).getAuthApi().getOrgUsers(new AdminRequestQrgId(session.getOrganisationId()));
        getUsersCall.enqueue(new Callback<UsersResponse>() {

                @Override
                public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
                    // 1. Hide the loading spinner
                    isLoading.setValue(false);

                    // 2. Check if the HTTP request was successful and our JSON success flag is true
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        ExecutorService executor = Executors.newSingleThreadExecutor();

                        List<UserModel> fetchedStudents = response.body().students;
                        List<UserModel> fetchedTechs = response.body().technicians;

                        List<StudentEntity> studentEntityList = new ArrayList<>();

                        // 3. Process Students: Tag them so the UI knows to make their badge Orange
                        if (fetchedStudents != null) {
                            for (UserModel student : fetchedStudents) {
                                studentEntityList.add(new StudentEntity(student.id, student.name, student.rollNumber));

                                student.role = "Student";
                            }
                            studentsList.setValue(fetchedStudents);

                            executor.execute(() -> {
                                dao.insertStudents(studentEntityList);
                            });
                        }

                        List<TechnicianEntity> technicianEntityList = new ArrayList<>();

                        // 4. Process Technicians: Tag them so the UI knows to make their badge Blue
                        if (fetchedTechs != null) {
                            for (UserModel tech : fetchedTechs) {
                                tech.role = "Technician";
                                technicianEntityList.add(new TechnicianEntity(tech.employeeCode, tech.id, tech.level, tech.name, tech.departmentId));
                            }
                            techniciansList.setValue(fetchedTechs);
                            executor.execute(() -> {
                                dao.insertTechnicians(technicianEntityList);
                            });
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
                     Log.e("UsersViewModel", "API call failed: " + t.getMessage());
                }
            });
    }

    public List<DepartmentEntity> getDepartmentsSync(String orgId) {
        return dao.getDepartmentsByOrgId();
    }

    public void sendUserInvite(String email, String rollNoOrEmpNo, String role, String orgId, String deptId){
        isLoading.setValue(true);
        architectureRepository.inviteUser(email, rollNoOrEmpNo, role, orgId, deptId, new ArchitectureRepository.ApiStatusListener(){
            @Override
            public void onSuccess(String message) {
                isLoading.setValue(false);
                successMessage.setValue(message);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void clearMessages(){
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (getUsersCall != null && !getUsersCall.isCanceled())
            getUsersCall.cancel();
    }
}