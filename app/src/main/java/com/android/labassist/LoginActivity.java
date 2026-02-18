package com.android.labassist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etUsername, etPassword, etOrgCode;
    MaterialButton btnLogin, btnForgotPassword, btnRegisterUser;
    RadioGroup rgUserRole;

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

        askNotificationPermission();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        etOrgCode = findViewById(R.id.etOrgCode);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnRegisterUser = findViewById(R.id.btnRegisterUser);
        rgUserRole = findViewById(R.id.rgUserRole);

        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String orgCode =  etOrgCode.getText().toString().trim();
            int id = rgUserRole.getCheckedRadioButtonId();
            String role = "";

            if(username.isEmpty()){
                etUsername.setError("Email address cannot be empty");
                etUsername.requestFocus();
                return;
            }
            else if(password.isEmpty()){
                etPassword.setError("Please enter password");
                etPassword.requestFocus();
                return;
            }
            else if(orgCode.isEmpty()){
                etOrgCode.setError("Please enter organisation code");
                etOrgCode.requestFocus();
                return;
            }
            if(id != -1) {
                if (id == R.id.rbEndUser)
                    role = "end_user";
                else if (id == R.id.rbAdmin)
                    role = "admin";
                else
                    role = "technician";
            }
            SessionManager.saveLogin(LoginActivity.this, username, role, orgCode);

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);

            // Todo: check user validity using firebase auth and store user data in UserInfo
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
}