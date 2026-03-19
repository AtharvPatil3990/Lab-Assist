package com.android.labassist.technician;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DeviceEntity;

public class TechDeviceDetailsViewModel extends AndroidViewModel {

    LiveData<DeviceEntity> deviceLiveData;

    public void init(LabAssistDao dao, String deviceId) {
        if (deviceLiveData == null && deviceId != null) {
            deviceLiveData = dao.getLiveDeviceFromId(deviceId);
        }
    }

    public LiveData<DeviceEntity> getDevice() {
        return deviceLiveData;
    }

    public TechDeviceDetailsViewModel(@NonNull Application application) {
        super(application);
    }
}
