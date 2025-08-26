package com.example.shopbepoly.Screen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.API.WebSocketManager;
import com.example.shopbepoly.AppStaff;
import com.example.shopbepoly.DTO.LoginRequest;
import com.example.shopbepoly.DTO.LoginResponse;
import com.example.shopbepoly.R;
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
    private ImageView btnTogglePassword;

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
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        loadSavedCredentials();

        handleRegisteredAccount();

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            int role = sharedPreferences.getInt("role", 0); // default 0
            if (role == 1) {
                startActivity(new Intent(LoginScreen.this, AppStaff.class));
            } else {
                startActivity(new Intent(LoginScreen.this, HomeNavScreen.class));
            }
            finish();
            return;
        }

        // Toggle hiển thị mật khẩu
        final boolean[] isPasswordVisible = {false};
        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_on);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
            }
            edtPassword.setSelection(edtPassword.length());
            isPasswordVisible[0] = !isPasswordVisible[0];
        });

        // Xử lý nút Login
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

        // Chuyển sang màn Register
        txtRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterScreen.class))
        );

        // Chuyển sang màn ForgotPassword
        txtForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPassword.class))
        );
    }

    private void loadSavedCredentials() {
        String savedUsername = sharedPreferences.getString("username", "");
        String savedPassword = sharedPreferences.getString("password", "");
        boolean isRemembered = sharedPreferences.getBoolean("remember", false);

        edtUsername.setText(savedUsername);
        if (isRemembered) {
            edtPassword.setText(savedPassword);
            checkboxRemember.setChecked(true);
        } else {
            edtPassword.setText("");
            checkboxRemember.setChecked(false);
        }
    }

    private void handleRegisteredAccount() {
        Intent intent = getIntent();
        if (intent != null) {
            String regUsername = intent.getStringExtra("username");
            String regPassword = intent.getStringExtra("password");

            if (regUsername != null && regPassword != null) {
                edtUsername.setText(regUsername);
                edtPassword.setText(regPassword);
                checkboxRemember.setChecked(true); // Tự đánh dấu lưu mật khẩu
            }
        }
    }

    /**
     * Gọi API login
     */
    private void loginUser(String username, String password, boolean remember) {
        ApiService apiService = ApiClient.getApiService();
        Call<LoginResponse> call = apiService.login(new LoginRequest(username, password));

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    // Lưu thông tin user
                    editor.putString("userId", loginResponse.getUser().getId());
                    editor.putString("username", loginResponse.getUser().getUsername());
                    editor.putString("name", loginResponse.getUser().getName());
                    editor.putString("email", loginResponse.getUser().getEmail());
                    editor.putInt("role", loginResponse.getUser().getRole());

                    String userJson = new Gson().toJson(loginResponse.getUser());
                    editor.putString("currentUser", userJson);

                    editor.putBoolean("isLoggedIn", true);

                    if (remember) {
                        editor.putString("password", password);
                        editor.putBoolean("remember", true);
                    } else {
                        editor.remove("password");
                        editor.putBoolean("remember", false);
                    }

                    editor.apply();

                    // Kết nối WebSocket
                    String userId = loginResponse.getUser().getId();
                    WebSocketManager.connect(userId);

                    Log.d("LoginScreen", "Saved userId = " + userId);

                    int role = loginResponse.getUser().getRole();
                    if (role == 1) {
                        Toast.makeText(LoginScreen.this, "Đăng nhập nhân viên", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginScreen.this, AppStaff.class));
                    } else {
                        startActivity(new Intent(LoginScreen.this, HomeNavScreen.class));
                        Toast.makeText(LoginScreen.this, "Chào mừng bạn đến mua sắm!" + role, Toast.LENGTH_SHORT).show();
                    }
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