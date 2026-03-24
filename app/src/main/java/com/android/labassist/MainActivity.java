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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.ActivityMainBinding;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.ProfileData;
import com.android.labassist.network.models.UserProfileResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;
    private Snackbar offlineSnackbar;
    private boolean wasOffline = false;
    ActivityMainBinding binding;

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


        sessionManager = SessionManager.getInstance(getApplicationContext());

        binding = ActivityMainBinding.bind(findViewById(R.id.activity_main));

//    Observing Logout signal
        AuthEventBus.getInstance().getLogoutSignal().observe(this, shouldLogout -> {
            if (shouldLogout)
                navigateToLogin();
        });

        if(sessionManager.getId() == null && sessionManager.isRoleSet()){
            navigateToLogin();
        }

        createNotificationChannel();

        bottomNav = binding.bottomNavigation;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        handleRoleAndNavigate();
        initNetworkMonitor();
    }

    private void initNetworkMonitor(){
        NetworkMonitor networkMonitor = new NetworkMonitor(this);

        View rootView = findViewById(android.R.id.content);

        networkMonitor.observe(this, isConnected -> {
            if(!isConnected){
                wasOffline = true;

                if(offlineSnackbar != null) {
                    offlineSnackbar = Snackbar.make(rootView, "No internet connection. You are offline.", Snackbar.LENGTH_INDEFINITE);
                    offlineSnackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.cancelled_status));
                    offlineSnackbar.setTextColor(ContextCompat.getColor(this, R.color.warning_text_color));


                    if (bottomNav != null) {
                        offlineSnackbar.setAnchorView(bottomNav);
                    }
                }
                if (offlineSnackbar!=null && !offlineSnackbar.isShown())
                    offlineSnackbar.show();
            }
            else{
                if(offlineSnackbar != null && offlineSnackbar.isShown())
                    offlineSnackbar.dismiss();

                if(wasOffline) {
                    Snackbar onlineSnackbar = Snackbar.make(rootView, "Back Online", Snackbar.LENGTH_SHORT);
                    onlineSnackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.completed_status));
                    onlineSnackbar.setTextColor(ContextCompat.getColor(this, R.color.warning_text_color));
                    if (bottomNav != null)
                        onlineSnackbar.setAnchorView(bottomNav);


                    onlineSnackbar.show();
                }
                wasOffline = false;

            }
        });
    }

    private void createNotificationChannel(){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel("complaint_alert_channel", "TechComplaint Alert Channel", NotificationManager.IMPORTANCE_HIGH));
    }

    private void handleRoleAndNavigate(){
        if(!sessionManager.isRoleSet())
            fetchUserProfile();
        else setupNavigation();


        bottomNav.setVisibility(View.VISIBLE);
    }

    private void fetchUserProfile(){
        binding.loadingOverlay.setVisibility(View.VISIBLE);

        Call<UserProfileResponse> call = ApiController.getInstance(getApplicationContext()).getAuthApi().getUserProfile();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if(isFinishing() || isDestroyed())
                    return;

                if(response.isSuccessful() && response.body()!=null){
                    UserProfileResponse userProfile = response.body();
                    if(sessionManager != null) {
                        ProfileData userData = userProfile.getProfile();
                        String regID = getRegistrationId(userProfile.getRole(), userData);
                        String dep = userProfile.getRole().equals(SessionManager.ROLE_ADMIN) ? "": userData.getDepartmentName();
// TODO: add admin levels here
                        sessionManager.saveLogin(
                                userData.getId(),
                                userData.getOrganizationCode(),
                                userProfile.getEmail(),
                                userProfile.getRole(),
                                userData.getName(),
                                userData.getOrganizationName(),
                                userData.getOrganizationID(),
                                dep,
                                userData.getDepartmentID(),
                                regID
                        );

                        if(userProfile.getRole().equals(SessionManager.ROLE_ADMIN))
                            sessionManager.setAdminLevel(userProfile.getLevel());

                        setupNavigation();
                    }
                }
                else {
                    handleAuthFailure(response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                if(t instanceof NoNetworkException){
                    Toast.makeText(MainActivity.this, "No network connection please login again", Toast.LENGTH_SHORT).show();
                }
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
        binding.loadingOverlay.setVisibility(View.GONE);

        switch (sessionManager.getRole()){
            case SessionManager.ROLE_ADMIN:
                            navController.setGraph(R.navigation.nav_admin);
                            bottomNav.inflateMenu(R.menu.menu_admin);
                            break;

            case SessionManager.ROLE_TECH:
                            navController.setGraph(R.navigation.nav_technician);
                            bottomNav.inflateMenu(R.menu.menu_technician);
                            break;

            case SessionManager.ROLE_STUDENT:
                            navController.setGraph(R.navigation.nav_end_user);
                            bottomNav.inflateMenu(R.menu.menu_enduser);
                            break;

            default:
                Toast.makeText(this, "User role is not recognized, please login in again", Toast.LENGTH_LONG).show();
                navigateToLogin();
        }

        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    private void handleAuthFailure(String logMessage) {
        Log.e("MainError", logMessage);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}