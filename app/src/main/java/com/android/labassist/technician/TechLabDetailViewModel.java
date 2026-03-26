package com.android.labassist.technician;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.TechnicianEntity;
import com.android.labassist.repositories.ArchitectureRepository;

import java.util.List;
public class TechLabDetailViewModel extends AndroidViewModel {

    LabAssistDao dao;

    ArchitectureRepository repository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }


    public TechLabDetailViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).labAssistDao();
        repository = new ArchitectureRepository(application);
    }


    List<TechnicianEntity> getTechniciansForDepartment(String deptId){
        return dao.getTechniciansByDeptId(deptId);
    }

    public void assignTechnicianToLab(String labId, String techId, boolean isPrimary){
        isLoading.setValue(true);

        repository.assignTechToLab(labId, techId, isPrimary, new ArchitectureRepository.ApiStatusListener() {
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
        repository.cancelCalls();
    }
}
