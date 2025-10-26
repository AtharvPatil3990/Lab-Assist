package com.android.labassist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

            // Todo: check user validity using firebase auth
        });

    }
}