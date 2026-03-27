package com.android.labassist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
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
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.TokenManager;
import com.android.labassist.databinding.ActivityLoginBinding;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.GoogleAuthRequest;
import com.android.labassist.network.models.LoginRequest;
import com.android.labassist.network.models.LoginResponse;
import com.android.labassist.BuildConfig;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import com.google.android.material.snackbar.Snackbar;

import android.view.inputmethod.InputMethodManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    Snackbar offlineSnackbar;
    private boolean wasOffline = false;

    private CredentialManager credentialManager;

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

        credentialManager = CredentialManager.create(this);

        binding.btnSignInWithGoogle.setOnClickListener(v ->{
            startGoogleSignIn();
        });

        binding.btnLogin.setOnClickListener(view -> {
            hideKeyboardAndClearFocus();

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

        initNetworkMonitor();
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

    private void startGoogleSignIn(){
        String WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID;
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // True if you only want them to select previously saved accounts
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true) // Automatically signs them in if they only have 1 account
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(this), // Run the callback on the Main UI thread
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignInSuccess(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e("GoogleAuth", "Sign-in failed: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Sign-in cancelled or failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSignInSuccess(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        // Check if the credential returned is actually a Google ID Token
        if (credential instanceof CustomCredential &&
                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

            try {
                // Parse the raw data into a usable Google Credential object
                GoogleIdTokenCredential googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());

                String idToken = googleIdTokenCredential.getIdToken();
                Log.d("GoogleAuth", "ID Token Received: " + idToken);

                loginWithGoogle(idToken);

            } catch (Exception e) {
                Log.e("GoogleAuth", "Received an invalid Google ID token response", e);
                Toast.makeText(this, "Authentication error.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // This catches scenarios where you eventually add Password or Passkey support
            Log.e("GoogleAuth", "Unexpected type of credential received.");
        }
    }

    private void askNotificationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void loginUser(LoginRequest request){
        binding.loginOverlay.setVisibility(View.VISIBLE);

        Call<LoginResponse> call =
                ApiController.getInstance(LoginActivity.this)
                .getPublicApi()
                .getLogin(request);

        AuthEventBus.getInstance().resetLogoutSignal();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                binding.loginOverlay.setVisibility(View.GONE);
                if(response.isSuccessful() && response.body()!=null) {
                    LoginResponse body = response.body();
                    TokenManager.getInstance(LoginActivity.this).saveTokens(body.getAccessToken(), body.getRefreshToken());
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    finish();
                }
                else{
                    Snackbar.make(binding.getRoot(), "Incorrect email or password", Snackbar.LENGTH_SHORT).show();
                    Log.d("LogErr", call.toString());
                    Log.d("LogErr", response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Snackbar.make(binding.getRoot(), "Unexpected error occurred", Snackbar.LENGTH_SHORT).show();
                Log.d("LogErr", call.toString());

                binding.loginOverlay.setVisibility(View.GONE);
            }
        });
    }

    private void loginWithGoogle(String idToken) {
        hideKeyboardAndClearFocus();

        // Show the same loading overlay
        binding.loginOverlay.setVisibility(View.VISIBLE);

        // Package the token into the request model
        GoogleAuthRequest request = new GoogleAuthRequest(idToken);

        // Make the call (Make sure "signInWithGoogle" matches your API interface method name)
        Call<LoginResponse> call =
                ApiController.getInstance(LoginActivity.this)
                        .getPublicApi()
                        .signInWithGoogle(request);

        AuthEventBus.getInstance().resetLogoutSignal();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                binding.loginOverlay.setVisibility(View.GONE);

                if(response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();

                    // Reusing your exact TokenManager logic!
                    TokenManager.getInstance(LoginActivity.this).saveTokens(body.getAccessToken(), body.getRefreshToken());

                    Toast.makeText(LoginActivity.this, "Google Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    // Updated the error message to reflect Google auth
                    Snackbar.make(binding.getRoot(), "Google authentication failed", Snackbar.LENGTH_SHORT).show();
                    Log.d("LogErr", call.toString());
                    Log.d("LogErr", response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                // Updated to log the actual Throwable 't' so you can see exactly why it failed (e.g., timeout, no wifi)
                Snackbar.make(binding.getRoot(), "Network error occurred", Snackbar.LENGTH_SHORT).show();
                Log.d("LogErr", call.toString());
                Log.d("LogErr", t.toString());

                binding.loginOverlay.setVisibility(View.GONE);
            }
        });
    }

    private void initNetworkMonitor(){
        NetworkMonitor networkMonitor = new NetworkMonitor(this);

        networkMonitor.observe(this, isConnected ->{
            if(!isConnected){
                wasOffline = true;
                binding.btnLogin.setEnabled(false);
                binding.btnLogin.setText("Waiting for connection...");
                offlineSnackbar = Snackbar.make(binding.getRoot(), "No Internet Connection, you are offline", Snackbar.LENGTH_INDEFINITE)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.cancelled_status))
                    .setTextColor(ContextCompat.getColor(this, R.color.white));

                offlineSnackbar.show();
            }
            else{
                if(offlineSnackbar != null)
                    offlineSnackbar.dismiss();

                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Login");

                if(wasOffline) {
                    Snackbar.make(binding.getRoot(), "Back online", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(ContextCompat.getColor(this, R.color.completed_status))
                            .setTextColor(ContextCompat.getColor(this, R.color.warning_text_color))
                            .show();
                }
                wasOffline = false;
            }
        });
    }

    private void hideKeyboardAndClearFocus() {
        View currentFocus = this.getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                // Safer window token retrieval
                imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}