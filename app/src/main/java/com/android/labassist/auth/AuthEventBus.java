package com.android.labassist.auth;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthEventBus {
    private static AuthEventBus instance;
    private final MutableLiveData<Boolean> logoutSignal = new MutableLiveData<>();

    private AuthEventBus() {}

    public static synchronized AuthEventBus getInstance(){
        if(instance == null){
            instance = new AuthEventBus();
        }
        return instance;
    }

    public LiveData<Boolean> getLogoutSignal(){
        Log.d("Token", "AuthEventBus getLogoutSignal called");
        return logoutSignal;
    }

    public void triggerLogout(){
        Log.d("Token", "AuthEventBus triggerLogout() called");
        logoutSignal.postValue(true);
    }
    public void resetLogoutSignal() {
        logoutSignal.setValue(false);
    }
}
