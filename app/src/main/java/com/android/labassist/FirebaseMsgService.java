package com.android.labassist;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMsgService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()){
                            Log.e("Task Unsuccessful", "Exception: " + task.getException());
                            return;
                        }
                        String token = task.getResult();

                    }
                });

    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        if(token.isEmpty())
            return;

        Log.d("Firebase Token", token);

    }
}
