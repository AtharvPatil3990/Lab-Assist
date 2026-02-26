package com.android.labassist.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.auth.SessionManager;

public class AdminProfileViewModel extends AndroidViewModel {

    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> instituteName = new MutableLiveData<>();
    private final MutableLiveData<String> department = new MutableLiveData<>();

    public AdminProfileViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadProfileData() {
        SessionManager session = SessionManager.getInstance(getApplication());

        name.setValue(session.getUsername());
        email.setValue(session.getEmail());
        instituteName.setValue(session.getInstitutionName());
        department.setValue(session.getDepartment());
    }

    public LiveData<String> getName() { return name; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getInstituteName() { return instituteName; }
    public LiveData<String> getDepartment() { return department; }
}