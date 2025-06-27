package com.example.shopbepoly.Screen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.LoginRequest;
import com.example.shopbepoly.DTO.LoginResponse;
import com.example.shopbepoly.R;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.example.shopbepoly.nav.HomeNavScreen;
import com.google.gson.Gson;

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

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        checkboxRemember = findViewById(R.id.checkboxRemember);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);
        txtForgot = findViewById(R.id.txtForgotPassword);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadSavedCredentials();

        btnLogin.setOnClickListener(view -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            boolean remember = checkboxRemember.isChecked();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(username, password, remember);
        });

        txtRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterScreen.class))
        );

        txtForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPassword.class))
        );
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

                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("userId", loginResponse.getUser().getId());
                    editor.putString("username", loginResponse.getUser().getUsername());
                    editor.putString("name", loginResponse.getUser().getName());
                    editor.putString("email", loginResponse.getUser().getEmail());
                    editor.putInt("role", loginResponse.getUser().getRole());
                    Log.d("LoginDebug", "Saved userId = " + loginResponse.getUser().getId());
                    Log.d("LoginDebug", "User = " + new Gson().toJson(loginResponse.getUser()));

                    String userJson = new Gson().toJson(loginResponse.getUser());
                    editor.putString("currentUser", userJson);
                    Log.d("LoginScreen", "User saved to SharedPreferences as JSON: " + userJson);

                    if (remember) {
                        editor.putString("password", password);
                        editor.putBoolean("remember", true);
                    } else {
                        editor.remove("password");
                        editor.putBoolean("remember", false);
                    }

                    editor.apply(); // Quan trọng!


                    // Log kiểm tra
                    Log.d("LoginScreen", "Saved userId = " + loginResponse.getUser().getId());

                    Toast.makeText(LoginScreen.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
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
}
