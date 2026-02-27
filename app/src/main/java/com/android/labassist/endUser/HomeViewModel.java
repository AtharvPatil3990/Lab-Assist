package com.android.labassist.endUser;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.android.labassist.ComplaintRepository;

public class HomeViewModel extends AndroidViewModel {
    ComplaintRepository repository;
    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new ComplaintRepository(application);
    }

    public void triggerLabSync(){
        repository.fetchAndCacheDepartmentArchitecture();
    }
}
