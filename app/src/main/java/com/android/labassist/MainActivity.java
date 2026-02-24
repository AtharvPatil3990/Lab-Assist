package com.android.labassist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.auth.TokenManager;
import com.android.labassist.databinding.ActivityMainBinding;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.ProfileData;
import com.android.labassist.network.models.UserProfileResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNav;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,  systemBars.bottom);
            return insets;
        });


        sessionManager = SessionManager.getInstance(MainActivity.this);

        Log.d("Token", "Outside the observe");
        ActivityMainBinding binding = ActivityMainBinding.bind(findViewById(R.id.activity_main));


//    Observing Logout signal
        AuthEventBus.getInstance().getLogoutSignal().observe(this, shouldLogout -> {
            Log.d("Token", "AuthEventBus observing called " + shouldLogout);
            if (shouldLogout)
                navigateToLogin();
        });


        TokenManager tokenManager = new TokenManager(getApplicationContext());
        if(tokenManager.getRefreshToken() == null) {
            navigateToLogin();
        }

        Log.d("Token", "Initializing binding");

        progressBar = binding.loadingProgressBar;

        createNotificationChannel();

        bottomNav = binding.bottomNavigation;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        handleRoleAndNavigate();
    }

    private void createNotificationChannel(){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel("complaint_alert_channel", "TechComplaint Alert Channel", NotificationManager.IMPORTANCE_HIGH));
    }

    private void handleRoleAndNavigate(){
        if(!sessionManager.isRoleSet())
            fetchUserProfile();
        else setupNavigation();


        progressBar.setVisibility(View.GONE);
        bottomNav.setVisibility(View.VISIBLE);
    }

    private void fetchUserProfile(){
        Call<UserProfileResponse> call = ApiController.getInstance(getApplicationContext()).getAuthApi().getUserProfile();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if(response.isSuccessful() && response.body()!=null){
                    UserProfileResponse userProfile = response.body();
                    if(sessionManager != null) {
                        ProfileData userData = userProfile.getProfile();
                        String regID = getRegistrationId(userProfile.getRole(), userData);

                        sessionManager.saveLogin(
                                userData.getId(),
                                userData.getEmail(),
                                userProfile.getRole(),
                                userData.getName(),
                                userData.getOrganizationName(),
                                userData.getOrganizationID(),
                                userData.getDepartmentName(),
                                userData.getDepartmentID(),
                                regID
                        );
                        setupNavigation();
                    }
                }
                else handleAuthFailure(response.message());
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                handleAuthFailure(t.getMessage());
            }
        });
    }

    private void navigateToLogin(){
        AuthEventBus.getInstance().resetLogoutSignal();

        sessionManager.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation(){

        switch (sessionManager.getRole()){
            case SessionManager.ROLE_ADMIN:
                            navController.setGraph(R.navigation.nav_admin);
                            bottomNav.inflateMenu(R.menu.menu_admin);
                            break;

            case SessionManager.ROLE_TECH:
                            navController.setGraph(R.navigation.nav_technician);
                            bottomNav.inflateMenu(R.menu.menu_technician);
                            break;

            default:
//                For student
                navController.setGraph(R.navigation.nav_end_user);
                bottomNav.inflateMenu(R.menu.menu_enduser);
                break;
        }

        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    private void handleAuthFailure(String logMessage) {
        Log.e("MainActivity", logMessage);
        Toast.makeText(this, "Authentication failed. Please log in again.", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private String getRegistrationId(String role, ProfileData data) {
        switch (role) {
            case SessionManager.ROLE_ADMIN: return data.getId();
            case SessionManager.ROLE_TECH:  return data.getEmpCode();
            default:                        return data.getRollNumber();
        }
    }
}