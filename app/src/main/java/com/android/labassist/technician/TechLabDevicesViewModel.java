package com.android.labassist.technician;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.repositories.ArchitectureRepository;

import java.util.List;

public class TechLabDevicesViewModel extends AndroidViewModel {

    private ArchitectureRepository architectureRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }

    LiveData<List<DeviceEntity>> devices;
    public TechLabDevicesViewModel(@NonNull Application application) {
        super(application);
        architectureRepository = new ArchitectureRepository(application);
    }

    public void init(LabAssistDao dao, String labId){
        devices = dao.getDevicesForLab(labId);
    }

    public LiveData<List<DeviceEntity>> getDevicesByLabId(){
        return devices;
    }

    public void createDevice(String currentLabId, String code, String name, String dbTypeEnum, String typeOther){
        isLoading.setValue(true);

        architectureRepository.createNewDevice(currentLabId, code, name, dbTypeEnum, typeOther, new ArchitectureRepository.ApiStatusListener() {
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
}
