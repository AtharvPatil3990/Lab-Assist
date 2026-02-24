package com.android.labassist.endUser;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.auth.SessionManager;

public class ProfileViewModel extends AndroidViewModel {

    // 1. Create a simple container class to hold all the profile data
    public static class UserProfileState {
        public final String name, email, institute, orgCode, regId, department;

        public UserProfileState(String name, String email, String institute, String orgCode, String regId, String department) {
            this.name = name;
            this.email = email;
            this.institute = institute;
            this.orgCode = orgCode;
            this.regId = regId;
            this.department = department;
        }
    }

    // 2. The LiveData stream that the Fragment will observe
    private final MutableLiveData<UserProfileState> profileData = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        loadUserProfile();
    }

    private void loadUserProfile() {
        // The ViewModel handles the SessionManager logic instead of the UI
        SessionManager session = SessionManager.getInstance(getApplication());

        if (session.getEmail() != null) {
            UserProfileState state = new UserProfileState(
                    session.getUsername(),
                    session.getEmail(),
                    session.getInstitutionName(),
                    session.getOrganisationId(),
                    session.getRegID(),
                    session.getDepartment()
            );
            // Post the data to the LiveData stream
            profileData.setValue(state);
        }
    }

    // 3. Expose the stream to the Fragment
    public LiveData<UserProfileState> getProfileData() {
        return profileData;
    }
}