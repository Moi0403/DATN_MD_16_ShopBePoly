package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code_screen);

        edtCode = findViewById(R.id.edtCode);
        btnVerify = findViewById(R.id.btnVerify);
        btn_back = findViewById(R.id.btnBack_ver);
        tvResendCode1 = findViewById(R.id.tvResendCode1);
        email = getIntent().getStringExtra("email");
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvResendCode1.setOnClickListener(v -> {
            Map<String, String> resendBody = new HashMap<>();
            resendBody.put("email", email);

            ApiClient.getApiService().sendVerificationCode(resendBody).enqueue(new Callback<Void>() {
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
        });

        btnVerify.setOnClickListener(v -> {
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
                        Intent intent = new Intent(VerifyCodeScreen.this, DoiMatKhau.class);
                        intent.putExtra("email", email);
                        intent.putExtra("isForgotPassword", true); // Truyền cờ này
                        startActivity(intent);
                        finish();

                    } else {
                        showToast("Mã không đúng. Vui lòng thử lại!");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showToast("Lỗi kết nối. Vui lòng thử lại!");
                }
            });
        });
    }

    private void showToast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace(); // in logcat nếu có lỗi Toast
        }
    }
}
