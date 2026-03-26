package com.android.labassist.technician;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.repositories.ArchitectureRepository;

import java.util.List;

public class AssignedLabsViewModel extends AndroidViewModel {
    private LiveData<List<LabEntity>> labsLiveData;

    private ArchitectureRepository architectureRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }

    private LabAssistDao dao;

    public AssignedLabsViewModel(@NonNull Application application) {
        super(application);
        architectureRepository = new ArchitectureRepository(application);
    }

    // Initialize with your DAO, just like you did in NotesViewModel
    public void init(LabAssistDao dao, boolean isAdmin, String deptId) {
        // Fetch the LiveData directly from Room
        if(isAdmin) {
            this.dao = dao;
            this.labsLiveData = dao.getLiveLabsByDeptId(deptId);
        }

        else this.labsLiveData = dao.getAllLabsForLabLive();
    }

    public void refreshLabsLiveData(LabAssistDao dao, String deptId){
        this.labsLiveData = dao.getLiveLabsByDeptId(deptId);
    }

    // Expose the list to the Fragment
    public LiveData<List<LabEntity>> getAssignedLabs() {
        return labsLiveData;
    }

    public void createNewLab(String name, String  code, String dbTypeEnum, String typeOther, String department_id){
        isLoading.setValue(true);
        architectureRepository.createNewLab(name, code, dbTypeEnum, typeOther, department_id, new ArchitectureRepository.ApiStatusListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.setValue(false);
                successMessage.setValue(message);
                refreshLabsLiveData(dao, department_id);
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
        architectureRepository.cancelCalls();
    }
}
