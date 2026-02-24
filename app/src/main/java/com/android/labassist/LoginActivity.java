package com.android.labassist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.auth.TokenManager;
import com.android.labassist.databinding.ActivityLoginBinding;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.LoginRequest;
import com.android.labassist.network.models.LoginResponse;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityLoginBinding.bind(findViewById(R.id.login_activity));
        setContentView(binding.getRoot());

        askNotificationPermission();

        binding.btnLogin.setOnClickListener(view -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if(email.isEmpty()){
                binding.etEmail.setError("Email address cannot be empty");
                binding.etEmail.requestFocus();
                return;
            }
            else if(password.isEmpty()){
                binding.etPassword.setError("Please enter password");
                binding.etPassword.requestFocus();
                return;
            }

            loginUser(new LoginRequest(email, password));
        });

    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(!isGranted){
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Enable Notification")
                            .setMessage("Notification is needed to alert you about complaint updates")
                            .setPositiveButton("Open Settings", (dialog, which) -> {
                                Intent openSettings = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                                startActivity(openSettings);
                            })
                            .setNegativeButton("Later", ((dialog, which) -> {
                                Snackbar.make(findViewById(R.id.login_activity), "Notification can be enabled through settings later", Snackbar.LENGTH_SHORT);
                            }))
                            .show();
                }
    });

    private void askNotificationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void loginUser(LoginRequest request){
        binding.loginProgressBar.setVisibility(View.VISIBLE);

        Call<LoginResponse> call =
                ApiController.getInstance(LoginActivity.this)
                .getPublicApi()
                .getLogin(request);

        AuthEventBus.getInstance().resetLogoutSignal();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if(response.isSuccessful() && response.body()!=null) {
                    LoginResponse body = response.body();
                    new TokenManager(LoginActivity.this).saveTokens(body.getAccessToken(), body.getRefreshToken());
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    binding.loginProgressBar.setVisibility(View.GONE);

                    finish();
                }
                else{
                    Snackbar.make(binding.getRoot(), "Login not successful", Snackbar.LENGTH_SHORT).show();
                    Log.d("LogErr", call.toString());
                    Log.d("LogErr", response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Snackbar.make(binding.getRoot(), "Unexpected error occurred", Snackbar.LENGTH_SHORT).show();

                Log.d("LogErr", call.toString());
                Log.d("LogErr", t.getMessage());
                Log.d("LogErr", t.toString());
                binding.loginProgressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}