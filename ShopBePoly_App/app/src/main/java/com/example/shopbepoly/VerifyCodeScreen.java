package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.User;
import com.example.shopbepoly.Screen.LoginScreen;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyCodeScreen extends AppCompatActivity {

    private EditText edtCode;
    private TextView tvResendCode1;
    private Button btnVerify;
    private String email;
    private ImageView btn_back;
    private String purpose;

    private String name, username, phone, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code_screen);

        edtCode = findViewById(R.id.edtCode);
        btnVerify = findViewById(R.id.btnVerify);
        btn_back = findViewById(R.id.btnBack_ver);
        tvResendCode1 = findViewById(R.id.tvResendCode1);

        email = getIntent().getStringExtra("email");
        purpose = getIntent().getStringExtra("purpose");

        if ("register".equals(purpose)) {
            name = getIntent().getStringExtra("name");
            username = getIntent().getStringExtra("username");
            phone = getIntent().getStringExtra("phone");
            password = getIntent().getStringExtra("password");
        }

        btn_back.setOnClickListener(v -> finish());

        tvResendCode1.setOnClickListener(v -> resendCode());

        btnVerify.setOnClickListener(v -> verifyCode());
    }

    private void resendCode() {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("purpose", purpose);

        ApiClient.getApiService().sendVerificationCode(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Mã xác minh mới đã được gửi tới email của bạn.");
                } else {
                    showToast("Không thể gửi lại mã. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Lỗi mạng. Vui lòng thử lại.");
            }
        });
    }

    private void verifyCode() {
        String code = edtCode.getText().toString().trim();
        if (code.isEmpty()) {
            edtCode.setError("Vui lòng nhập mã");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("code", code);

        ApiClient.getApiService().verifyCode(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if ("register".equals(purpose)) {
                        registerUser(); // tạo tài khoản mới
                    } else if ("forgotPassword".equals(purpose)) {
                        Intent intent = new Intent(VerifyCodeScreen.this, DoiMatKhau.class);
                        intent.putExtra("email", email);
                        intent.putExtra("isForgotPassword", true);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    showToast("Mã không đúng hoặc đã hết hạn. Vui lòng thử lại!");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Lỗi kết nối. Vui lòng thử lại!");
            }
        });
    }

    private void registerUser() {
        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone_number(phone);
        user.setPassword(password);

        ApiClient.getApiService().register(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Đăng ký thành công!");

                    Intent intent = new Intent(VerifyCodeScreen.this, LoginScreen.class);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    finish();
                } else {
                    showToast("Đăng ký thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Lỗi kết nối. Vui lòng thử lại!");
            }
        });
    }

    private void showToast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}