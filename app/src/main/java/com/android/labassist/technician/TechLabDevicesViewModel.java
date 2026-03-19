package com.android.labassist.technician;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DeviceEntity;

import java.util.List;

public class TechLabDevicesViewModel extends AndroidViewModel {
    LiveData<List<DeviceEntity>> devices;
    public TechLabDevicesViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(LabAssistDao dao, String labId){
        devices = dao.getDevicesForLab(labId);
    }

    public LiveData<List<DeviceEntity>> getDevicesByLabId(){
        return devices;
    }
}
