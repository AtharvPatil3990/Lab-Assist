package com.android.labassist.technician;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.labassist.auth.SessionManager;

public class TechnicianProfileViewModel extends AndroidViewModel {

    // 1. Container class representing a Technician's Profile
    public static class TechProfileState {
        public final String name, email, institute, orgCode, empId, department;

        public TechProfileState(String name, String email, String institute, String orgCode, String empId, String department) {
            this.name = name;
            this.email = email;
            this.institute = institute;
            this.orgCode = orgCode;
            this.empId = empId; // Technicians usually have Employee IDs/Codes
            this.department = department;
        }
    }

    private final MutableLiveData<TechProfileState> techProfileData = new MutableLiveData<>();

    public TechnicianProfileViewModel(@NonNull Application application) {
        super(application);
        loadTechProfile();
    }

    /**
     * Pulls the latest technician data from the SessionManager
     */
    private void loadTechProfile() {
        SessionManager session = SessionManager.getInstance(getApplication());

        if (session.getEmail() != null) {
            TechProfileState state = new TechProfileState(
                    session.getUsername(),
                    session.getEmail(),
                    session.getInstitutionName(),
                    session.getOrganisationId(),
                    session.getId(), // For techs, this is often their primary ID/Employee code
                    session.getDepartment()
            );
            techProfileData.setValue(state);
        }
    }

    // 2. Expose the data for the Fragment to observe
    public LiveData<TechProfileState> getTechProfileData() {
        return techProfileData;
    }

    /**
     * Call this if data is updated (e.g., after a name change)
     * to refresh the UI stream.
     */
    public void refreshProfile() {
        loadTechProfile();
    }
}