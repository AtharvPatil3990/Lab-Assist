package com.android.labassist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.android.labassist.repositories.ComplaintRepository;

public class MainViewModel extends AndroidViewModel {
    private final ComplaintRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        // Initialize your repository here
        repository = new ComplaintRepository(application);
    }
}
