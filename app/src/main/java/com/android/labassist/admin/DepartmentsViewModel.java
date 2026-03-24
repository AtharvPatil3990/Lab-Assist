package com.android.labassist.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DepartmentEntity;

import java.util.List;

public class DepartmentsViewModel extends AndroidViewModel {

    private final LabAssistDao dao;

    public DepartmentsViewModel(@NonNull Application application) {
        super(application);
        this.dao = AppDatabase.getInstance(application.getApplicationContext()).labAssistDao();
    }

    // Directly expose the Room query to the UI
    public LiveData<List<DepartmentEntity>> getAllDepartments() {
        return dao.getAllLiveDepartments();
    }
}
