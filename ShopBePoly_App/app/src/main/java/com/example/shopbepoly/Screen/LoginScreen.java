package com.example.shopbepoly.Screen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.LoginRequest;
import com.example.shopbepoly.DTO.LoginResponse;
import com.example.shopbepoly.R;
import com.example.shopbepoly.nav.HomeNavScreen;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginScreen extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private CheckBox checkboxRemember;
    private Button btnLogin;
    private TextView txtRegister, txtForgot;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtRegister = findViewById(R.id.txtRegister);
        txtForgot = findViewById(R.id.txtForgotPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        checkboxRemember = findViewById(R.id.checkboxRemember);
        btnLogin = findViewById(R.id.btnLogin);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadSavedCredentials();

        ImageView btnTogglePassword = findViewById(R.id.btnTogglePassword);
        final boolean[] isPasswordVisible = {false};

        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                edtPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_on);
            } else {
                edtPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
            }
            edtPassword.setSelection(edtPassword.getText().length());
            isPasswordVisible[0] = !isPasswordVisible[0];
        });

        btnLogin.setOnClickListener(view -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            boolean remember = checkboxRemember.isChecked();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginScreen.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(username, password, remember);
        });

        txtRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginScreen.this, RegisterScreen.class));
        });

        txtForgot.setOnClickListener(view -> {
            startActivity(new Intent(LoginScreen.this, ForgotPassword.class));
        });
    }

    private void loadSavedCredentials() {
        String savedUsername = sharedPreferences.getString("username", "");
        String savedPassword = sharedPreferences.getString("password", "");
        boolean isRemembered = sharedPreferences.getBoolean("remember", false);

        if (isRemembered) {
            edtUsername.setText(savedUsername);
            edtPassword.setText(savedPassword);
            checkboxRemember.setChecked(true);
        }
    }

    private void loginUser(String username, String password, boolean remember) {
        ApiService apiService = ApiClient.getApiService();
        Call<LoginResponse> call = apiService.login(new LoginRequest(username, password));

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Toast.makeText(LoginScreen.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    if (remember) {
                        saveCredentials(username, password);
                    } else {
                        clearCredentials();
                    }

                    startActivity(new Intent(LoginScreen.this, HomeNavScreen.class));
                    finish();
                } else {
                    Toast.makeText(LoginScreen.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginScreen.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCredentials(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void clearCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}