package com.android.labassist.endUser;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.auth.SessionManager;

public class ProfileViewModel extends AndroidViewModel {

    // 1. Create individual MutableLiveData objects for each field
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> institute = new MutableLiveData<>();
    private final MutableLiveData<String> orgCode = new MutableLiveData<>();
    private final MutableLiveData<String> regId = new MutableLiveData<>();
    private final MutableLiveData<String> department = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadUserProfile() {
        // The ViewModel handles the SessionManager logic
        SessionManager session = SessionManager.getInstance(getApplication());

        if (session.getId() != null) {
            // Post the data individually to each LiveData stream
            name.setValue(session.getUsername());
            email.setValue(session.getEmail());
            institute.setValue(session.getInstitutionName());
            orgCode.setValue(session.getOrganisationId());
            regId.setValue(session.getRegID());
            department.setValue(session.getDepartment());
        }
    }

    // 2. Expose the individual streams to the Fragment
    public LiveData<String> getName() { return name; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getInstitute() { return institute; }
    public LiveData<String> getOrgCode() { return orgCode; }
    public LiveData<String> getRegId() { return regId; }
    public LiveData<String> getDepartment() { return department; }
}