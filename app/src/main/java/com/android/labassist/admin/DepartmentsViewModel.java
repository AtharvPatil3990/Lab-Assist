package com.android.labassist.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DepartmentEntity;
import com.android.labassist.repositories.ArchitectureRepository;

import java.util.List;

public class DepartmentsViewModel extends AndroidViewModel {

    private final LabAssistDao dao;
    private final ArchitectureRepository architectureRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }

    public DepartmentsViewModel(@NonNull Application application) {
        super(application);
        this.dao = AppDatabase.getInstance(application.getApplicationContext()).labAssistDao();
        this.architectureRepository = new ArchitectureRepository(application.getApplicationContext());
    }

    // Directly expose the Room query to the UI
    public LiveData<List<DepartmentEntity>> getAllDepartments() {
        return dao.getAllLiveDepartments();
    }

    public void createNewDepartment(String deptName, String deptCode, String deptDesc) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        architectureRepository.createDepartment(deptName, deptCode, deptDesc, new ArchitectureRepository.ApiStatusListener() {
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

    public void refreshArchitecture(){
        architectureRepository.fetchAndSaveArchitecture();
    }

    public void clearMessages(){
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        architectureRepository.cancelCalls();
    }
}
